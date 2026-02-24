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

        // 1. COSTO REALE
        var costoAzione = 0
        if (azione is AzioneGiocatore.GiocaCartaRazza) {
            costoAzione = costoCartaConEvento(azione.carta, evento)
        }
        if (costoAzione > giocatore.doin) {
            return EsitoValutazioneEconomica(azione, -2000.0, 99, -1)
        }

        // 2. MONDO VIRTUALE
        val copia = clonaGiocatore(giocatore)
        var pvCarta = 0.0
        var renditaFuturaCarta = 0.0

        if (azione is AzioneGiocatore.GiocaCartaRazza) {
            copia.doin -= costoAzione
            val cartaMessa = azione.carta.copy(cani = azione.carta.cani.map { it.copy() }.toMutableList())
            copia.plancia.righe[azione.rigaDestinazione].add(cartaMessa)
            pvCarta = cartaMessa.puntiBase.toDouble()
            renditaFuturaCarta = cartaMessa.rendita.toDouble()
        }

        val doinDopoAcquisto = copia.doin

        // 3. SIMULAZIONE FUTURO (Cruciale per non fallire)
        val upkeepStasera = calcolaUpkeep(copia, evento).costoTotale
        val debitiOggi = (upkeepStasera - copia.doin).coerceAtLeast(0)
        
        copia.doin = (copia.doin - upkeepStasera).coerceAtLeast(0)
        val renditaMattina = calcolaRenditaNetta(copia) 
        applicaRenditaNetta(copia)
        applicaPopolamentoCarteNuove(copia)
        
        val upkeepDomani = calcolaUpkeep(copia).costoTotale
        val debitiDomani = (upkeepDomani - copia.doin).coerceAtLeast(0)
        
        val debitiTotali = debitiOggi + debitiDomani

        // 4. CALCOLO SCORE CON NUOVI BILANCIAMENTI
        
        // Penalità povertà (tua logica)
        var penalitaRiserva = 0.0
        if (doinDopoAcquisto < sogliaSicurezza) {
            penalitaRiserva = (sogliaSicurezza - doinDopoAcquisto) * pesoRiserva
        }

        // MALUS SOSTENIBILITÀ (Nuovo): se l'upkeep mangia tutta la rendita, è male
        var malusSostenibilita = 0.0
        if (upkeepDomani > (renditaMattina + 2)) { 
            malusSostenibilita = (upkeepDomani - renditaMattina) * 15.0 
        }

        // Pesi ricalibrati
        // PV: abbassati a 5 (erano 10) per non essere troppo ingordi
        // Debiti: alzati a 25 (erano 12) per averne TERRORE
        // Espansione: abbassata a 0.5 (era 2) per rallentare l'inizio
        val bonusEspansione = (16 - copia.plancia.slotOccupatiTotali()) * 0.5
        val fattorePauraDebito = if (giocatore.doin > 25) 15.0 else 30.0

        val score = (doinDopoAcquisto * 1.0) + 
                    (pvCarta * 5.0) + 
                    (renditaFuturaCarta * 8.0) + 
                    bonusEspansione - 
                    (debitiTotali * fattorePauraDebito) - 
                    penalitaRiserva -
                    malusSostenibilita

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
        var punteggioMax = -9999.0

        for (azione in azioni) {
            val esito = valuta(statoGiornata, statoGiocatore, azione, sogliaSicurezza, pesoRiserva)
            if (esito.score > punteggioMax) {
                punteggioMax = esito.score
                migliore = azione
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