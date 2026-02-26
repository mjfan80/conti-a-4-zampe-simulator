/* FILE COMPLETO: src/main/kotlin/it/contia4zampe/simulator/player/decision/ValutatoreAzioneEconomica.kt */

package it.contia4zampe.simulator.player.decision

import it.contia4zampe.simulator.engine.StatoGiocatoreGiornata
import it.contia4zampe.simulator.engine.StatoGiornata
import it.contia4zampe.simulator.model.*
import it.contia4zampe.simulator.player.AzioneGiocatore
import it.contia4zampe.simulator.rules.*

data class EsitoValutazioneEconomica(
    val azione: AzioneGiocatore,
    val score: Double,
    val debitiAttesi: Int,
    val doinResiduiPostCosto: Int
)

object ValutatoreAzioneEconomica {

    fun valuta(
        statoGiornata: StatoGiornata,
        statoGiocatore: StatoGiocatoreGiornata,
        azione: AzioneGiocatore,
        sogliaSicurezza: Int,
        pesoRiserva: Double
    ): EsitoValutazioneEconomica {
        val giocatore = statoGiocatore.giocatore
        val evento = statoGiornata.eventoAttivo

        // 1. COSTO AZIONE
        var costoAzione = 0
        if (azione is AzioneGiocatore.GiocaCartaRazza) {
            costoAzione = costoCartaConEvento(azione.carta, evento)
        }

        // Check base: ho i soldi per comprarla?
        if (costoAzione > giocatore.doin) return EsitoValutazioneEconomica(azione, -10000.0, 99, -1)

        // 2. MONDO VIRTUALE
        val copia = clonaGiocatore(giocatore)
        var pvCarta = 0.0

        // Variabile per tracciare la rendita che la NUOVA carta produrrà a regime (con i cani sopra)
        var renditaLatenteNuovaCarta = 0.0

        if (azione is AzioneGiocatore.GiocaCartaRazza) {
            copia.doin -= costoAzione
            val cartaMessa = azione.carta.copy(cani = azione.carta.cani.map { it.copy() }.toMutableList())
            copia.plancia.righe[azione.rigaDestinazione].add(cartaMessa)
            pvCarta = cartaMessa.puntiBase.toDouble()

            // FIX RENDITA: Calcoliamo quanto varrà questa carta quando avrà i 2 cani sopra (2 cani * rendita base)
            // Questo serve perché domani mattina sarà ancora vuota, ma noi investiamo per il futuro.
            renditaLatenteNuovaCarta = (cartaMessa.rendita * 2).toDouble()
        }

        val doinDopoAcquisto = copia.doin

        // 3. SIMULAZIONE FUTURO (Stasera e Domani Mattina)
        val upkeepStasera = calcolaUpkeep(copia, evento).costoTotale
        val debitiOggi = (upkeepStasera - copia.doin).coerceAtLeast(0)

        // Pago upkeep stasera
        copia.doin = (copia.doin - upkeepStasera).coerceAtLeast(0)

        // Calcolo quanto guadagno DOMANI MATTINA (Incasso effettivo)
        val cassaPrima = copia.doin
        applicaRenditaNetta(copia)          // Rendita cani esistenti
        applicaEffettiInizioGiornata(copia) // Effetti speciali (es. Labrador +1 funziona anche senza cani)
        val cassaDopo = copia.doin

        val renditaRealeDomani = (cassaDopo - cassaPrima).toDouble()

        // Ora arrivano i cani sulla carta nuova (ma la rendita l'abbiamo già incassata prima!)
        applicaPopolamentoCarteNuove(copia)

        val upkeepDomani = calcolaUpkeep(copia).costoTotale
        val debitiDomani = (upkeepDomani - copia.doin).coerceAtLeast(0)
        val debitiTotali = debitiOggi + debitiDomani

        // 4. PESI DINAMICI (Time-Dependent)
        // FIX GIORNATE: Usiamo 16.0 fisso o maxGiornateEvento + 1
        val maxGiornate = (statoGiornata.maxGiornateEvento + 1).toDouble()
        val progressoPartita = (statoGiornata.numero.toDouble() / maxGiornate).coerceIn(0.0, 1.0)

        // Peso Rendita: Scende col tempo (Inizio: 14.0 -> Fine: 4.0)
        val pesoRenditaDinamico = 14.0 - (progressoPartita * 10.0)

        // Peso PV: Sale col tempo (Inizio: 2.0 -> Fine: 16.0)
        val pesoPVDinamico = 2.0 + (progressoPartita * 14.0)

        // 5. ALTRI FATTORI

        // Bonus ROI Cuccioli (Soldi futuri)
        val numeroCuccioli = copia.plancia.righe.flatten().sumOf { c -> c.cani.count { it.stato == StatoCane.CUCCIOLO } }
        val bonusROI = numeroCuccioli * 6.0

        // Malus Debito
        val malusDebitoEsistente = giocatore.debiti * 30.0
        val fattorePauraDebitoFuturo = if (giocatore.doin > 25) 15.0 else 35.0

        // Penalità Riserva
        var penalitaRiserva = 0.0
        if (doinDopoAcquisto < sogliaSicurezza) {
            penalitaRiserva = (sogliaSicurezza - doinDopoAcquisto) * pesoRiserva
        }

        // 6. SCORE FINALE
        // Sommiamo Rendita Reale (domani) + Rendita Latente (dopodomani, grazie ai nuovi cani)
        val renditaTotaleStimata = renditaRealeDomani + renditaLatenteNuovaCarta

        val score = (doinDopoAcquisto * 1.0) +
                (pvCarta * pesoPVDinamico) +
                (renditaTotaleStimata * pesoRenditaDinamico) + // <--- FIX APPLICATO QUI
                bonusROI +
                (16 - copia.plancia.slotOccupatiTotali()) * 0.5 -
                (debitiTotali * fattorePauraDebitoFuturo) -
                malusDebitoEsistente -
                penalitaRiserva

        return EsitoValutazioneEconomica(azione, score, debitiTotali, doinDopoAcquisto)
    }

    fun scegliMigliore(
        statoGiornata: StatoGiornata,
        statoGiocatore: StatoGiocatoreGiornata,
        azioni: List<AzioneGiocatore>,
        sogliaScore: Double,
        sogliaSicurezza: Int,
        pesoRiserva: Double
    ): AzioneGiocatore {
        var migliore: AzioneGiocatore = AzioneGiocatore.Passa
        var punteggioMax = -20000.0
        for (azioneCorrente in azioni) {
            val esito = valuta(statoGiornata, statoGiocatore, azioneCorrente, sogliaSicurezza, pesoRiserva)
            if (esito.score > punteggioMax) {
                punteggioMax = esito.score
                migliore = azioneCorrente
            }
        }
        return if (punteggioMax >= sogliaScore) migliore else AzioneGiocatore.Passa
    }

    private fun costoCartaConEvento(carta: CartaRazza, evento: CartaEvento?): Int {
        var costo = carta.costo
        if (evento?.tipo == TipoEffettoEvento.MODIFICA_COSTO_RAZZA_TUTTE) costo += evento.variazione
        else if (evento?.tipo == TipoEffettoEvento.MODIFICA_COSTO_RAZZA_TAGLIA) {
            if (carta.taglia == evento.tagliaTarget) costo += evento.variazione
            else if (carta.taglia == evento.tagliaTargetSecondaria) costo += evento.variazioneSecondaria
        }
        return if (costo < 0) 0 else costo
    }

    private fun clonaGiocatore(originale: Giocatore): Giocatore {
        val righeCopia = originale.plancia.righe.map { riga ->
            riga.map { it.copy(cani = it.cani.map { c -> c.copy() }.toMutableList()) }.toMutableList()
        }
        return Giocatore(originale.id, originale.doin, originale.debiti, PlanciaGiocatore(righeCopia), originale.mano.toMutableList())
    }
}