package it.contia4zampe.simulator.player

import it.contia4zampe.simulator.engine.*
import it.contia4zampe.simulator.model.*
import it.contia4zampe.simulator.player.decision.*

class ProfiloAvventato : PlayerProfile {

    private fun sogliaScoreDinamica(numeroGiornata: Int, maxGiornate: Int): Double {
        val progresso = numeroGiornata.toDouble() / maxGiornate
        return when {
            progresso < 0.4 -> -20.0      // Early: molto aggressivo
            progresso < 0.75 -> -10.0     // Mid: ancora aggressivo ma meno
            else -> 0.0                   // Late: niente mosse negative
        }
    }

    override fun decidiAzione(statoGiornata: StatoGiornata, statoGiocatore: StatoGiocatoreGiornata): AzioneGiocatore {
        val g = statoGiocatore.giocatore

        // 1. REAZIONE AL BLOCCO: Se ho debiti e sono povero, VENDI subito per sbloccare l'economia
        if (g.debiti > 0 && g.doin < 2) {
            val vendita = trovaCanePerEmergenza(g)
            if (vendita != null) return AzioneGiocatore.VendiCani(listOf(vendita))
        }

        // 2. PAGAMENTO DEBITI: Più pragmatico, paga se ha almeno 5 doin
        if (g.debiti > 0 && g.doin >= 5) {
            return AzioneGiocatore.BloccoAzioniSecondarie(listOf(AzioneSecondaria.PagaDebito))
        }

        // 3. AZIONE PRINCIPALE: Espansione quasi forzata
        val opzioni = generaGiocatePossibili(g)
        val soglia = sogliaScoreDinamica(statoGiornata.numero, statoGiornata.maxGiornateEvento + 1)
        val migliore = ValutatoreAzioneEconomica.scegliMigliore(
            statoGiornata, statoGiocatore, opzioni,
            soglia, // alta tolleranza al rischio ma meno a partita avanzata
            sogliaSicurezza = 2,    // Gli bastano 2 doin per "sentirsi a posto"
            pesoRiserva = 0.5
        )
        if (migliore is AzioneGiocatore.GiocaCartaRazza) return migliore

        // 4. AZIONI SECONDARIE
        val blocco = mutableListOf<AzioneSecondaria>()

        val addestramento = cercaAzioneAddestramento(g)
        if (addestramento != null) blocco.add(addestramento)

        val acquisto = SelettoreMiniPlancia.suggerisciAcquisto(statoGiornata, statoGiocatore, 0)
        if (acquisto != null && blocco.size < 2) blocco.add(acquisto)

        if (blocco.isNotEmpty()) return AzioneGiocatore.BloccoAzioniSecondarie(blocco)
        return AzioneGiocatore.Passa
    }

    override fun vuoleDichiarareAccoppiamento(
        sg: StatoGiocatoreGiornata,
        carta: CartaRazza
    ): Boolean {

        val g = sg.giocatore
        val upkeepAttuale = sg.calcolaUpkeepAttuale()

        if (g.debiti >= 6 && g.doin < upkeepAttuale) return false

        return true
    }

    override fun decidiGestioneCucciolo(sg: StatoGiocatoreGiornata, cucciolo: Cane): SceltaCucciolo {
        // Se ha debiti pesanti vende, altrimenti tiene per fare PV
        if (sg.giocatore.debiti > 5) return SceltaCucciolo.VENDI
        return SceltaCucciolo.TRASFORMA_IN_ADULTO
    }

    override fun scegliCartaDalMercato(giocatore: StatoGiocatoreGiornata, mercato: List<CartaRazza>) = mercato.maxByOrNull { it.rendita } ?: mercato.first()
}