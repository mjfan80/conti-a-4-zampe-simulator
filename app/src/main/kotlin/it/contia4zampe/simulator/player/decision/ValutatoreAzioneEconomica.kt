package it.contia4zampe.simulator.player.decision

import it.contia4zampe.simulator.engine.StatoGiocatoreGiornata
import it.contia4zampe.simulator.engine.StatoGiornata
import it.contia4zampe.simulator.model.*
import it.contia4zampe.simulator.player.AzioneGiocatore
import it.contia4zampe.simulator.rules.*

// Struttura dati per i risultati
data class EsitoValutazioneEconomica(
    val azione: AzioneGiocatore,
    val score: Double,
    val debitiAttesi: Int,
    val doinResiduiPostCosto: Int
)

object ValutatoreAzioneEconomica {

    /**
     * Valuta una singola azione e restituisce uno score numerico.
     */
    fun valuta(
        statoGiornata: StatoGiornata,
        statoGiocatore: StatoGiocatoreGiornata,
        azione: AzioneGiocatore,
        sogliaSicurezza: Int,
        pesoRiserva: Double
    ): EsitoValutazioneEconomica {
        val giocatore = statoGiocatore.giocatore
        val evento = statoGiornata.eventoAttivo

        // 1. Calcolo Costo Reale
        var costoAzione = 0
        if (azione is AzioneGiocatore.GiocaCartaRazza) {
            costoAzione = costoCartaConEvento(azione.carta, evento)
        }

        // Se non ho i soldi base per la carta, l'azione è impossibile
        if (costoAzione > giocatore.doin) {
            return EsitoValutazioneEconomica(azione, -10000.0, 99, -1)
        }

        // 2. Simulazione in un "Mondo Virtuale"
        val copia = clonaGiocatore(giocatore)
        var pvCarta = 0.0
        var renditaCarta = 0.0

        if (azione is AzioneGiocatore.GiocaCartaRazza) {
            copia.doin -= costoAzione
            val cartaMessa = azione.carta.copy(cani = azione.carta.cani.map { it.copy() }.toMutableList())
            copia.plancia.righe[azione.rigaDestinazione].add(cartaMessa)
            pvCarta = cartaMessa.puntiBase.toDouble()
            renditaCarta = cartaMessa.rendita.toDouble()
        }

        val doinDopoAcquisto = copia.doin

        // 3. Simulazione Futuro (Stasera e Domani)
        val upkeepStasera = calcolaUpkeep(copia, evento).costoTotale
        val debitiOggi = (upkeepStasera - copia.doin).coerceAtLeast(0)

        copia.doin = (copia.doin - upkeepStasera).coerceAtLeast(0)
        val renditaMattina = calcolaRenditaNetta(copia)
        applicaRenditaNetta(copia)
        applicaPopolamentoCarteNuove(copia)

        val upkeepDomani = calcolaUpkeep(copia).costoTotale
        val debitiDomani = (upkeepDomani - copia.doin).coerceAtLeast(0)
        val debitiTotaliSimulati = debitiOggi + debitiDomani

        // 4. Calcolo Pesi e Malus

        // Malus Sostenibilità: se l'upkeep è troppo alto rispetto alla rendita
        var malusSostenibilita = 0.0
        if (upkeepDomani > (renditaMattina + 2)) {
            malusSostenibilita = (upkeepDomani - renditaMattina) * 5.0
        }

        // Malus Debito: diamo peso al debito che già esiste e a quello che faremmo
        val malusDebitoEsistente = giocatore.debiti * 20.0
        val fattorePauraDebitoFuturo = if (giocatore.doin > 25) 10.0 else 20.0

        // Bonus Espansione
        val bonusEspansione = (16 - copia.plancia.slotOccupatiTotali()) * 0.5

        // 5. Score Finale
        val score = (doinDopoAcquisto * 1.0) +
                (pvCarta * 8.0) +
                (renditaCarta * 10.0) +
                bonusEspansione -
                (debitiTotaliSimulati * fattorePauraDebitoFuturo) -
                malusDebitoEsistente -
                ((sogliaSicurezza - doinDopoAcquisto).coerceAtLeast(0) * pesoRiserva) -
                malusSostenibilita

        return EsitoValutazioneEconomica(azione, score, debitiTotaliSimulati, doinDopoAcquisto)
    }

    /**
     * Cicla su tutte le azioni possibili e sceglie quella con lo score più alto.
     */
    fun scegliMigliore(
        statoGiornata: StatoGiornata,
        statoGiocatore: StatoGiocatoreGiornata,
        azioni: List<AzioneGiocatore>,
        sogliaScore: Double,
        sogliaSicurezza: Int,
        pesoRiserva: Double
    ): AzioneGiocatore {

        var miglioreAzione: AzioneGiocatore = AzioneGiocatore.Passa
        var punteggioMigliore = -20000.0

        for (azioneCorrente in azioni) {
            // CORREZIONE: passiamo gli argomenti in ordine senza nomi per evitare errori di mixing
            val esito = valuta(statoGiornata, statoGiocatore, azioneCorrente, sogliaSicurezza, pesoRiserva)

            if (esito.score > punteggioMigliore) {
                punteggioMigliore = esito.score
                miglioreAzione = azioneCorrente
            }
        }

        return if (punteggioMigliore >= sogliaScore) miglioreAzione else AzioneGiocatore.Passa
    }

    // --- FUNZIONI HELPER ---

    private fun costoCartaConEvento(carta: CartaRazza, evento: CartaEvento?): Int {
        var costo = carta.costo
        if (evento?.tipo == TipoEffettoEvento.MODIFICA_COSTO_RAZZA_TUTTE) {
            costo += evento.variazione
        } else if (evento?.tipo == TipoEffettoEvento.MODIFICA_COSTO_RAZZA_TAGLIA) {
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