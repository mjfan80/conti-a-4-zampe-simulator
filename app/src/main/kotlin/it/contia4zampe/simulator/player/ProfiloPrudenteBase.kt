package it.contia4zampe.simulator.player

import it.contia4zampe.simulator.engine.*
import it.contia4zampe.simulator.model.*
import it.contia4zampe.simulator.player.decision.*
import it.contia4zampe.simulator.rules.calcolaRenditaNetta
import it.contia4zampe.simulator.rules.calcolaUpkeep

class ProfiloPrudenteBase : PlayerProfile {

    private fun sogliaScoreDinamica(numero: Int, max: Int): Double {
        val progresso = numero.toDouble() / max
        return if (progresso > 0.75) 0.0 else 2.0
    }

    override fun decidiAzione(statoGiornata: StatoGiornata, statoGiocatore: StatoGiocatoreGiornata): AzioneGiocatore {
        val g = statoGiocatore.giocatore

        // 1. SOPRAVVIVENZA: Se sto per andare in debito stasera, vendi!
        val emergenza = controllaUrgenzaVendita(statoGiocatore, statoGiornata)
        if (emergenza != null) return emergenza

        // 2. RIGORE: Paga debiti se ne hai
        if (g.debiti > 0 && g.doin >= 2) {
            return AzioneGiocatore.BloccoAzioniSecondarie(listOf(AzioneSecondaria.PagaDebito))
        }

        // 3. AZIONE PRINCIPALE: Basata sul MARGINE
        val rendita = calcolaRenditaNetta(g)
        val upkeep = calcolaUpkeep(g, statoGiornata.eventoAttivo).costoTotale
        val margine = rendita - upkeep

        // Se il margine è buono (>2), il prudente abbassa la guardia sulla scorta di doin
        val sicurezzaRichiesta = if (margine >= 2) 5 else 12

        val opzioni = generaGiocatePossibili(g)
        val sogliaScore = sogliaScoreDinamica(statoGiornata.numero, statoGiornata.maxGiornateEvento + 1)
        val scelta = ValutatoreAzioneEconomica.scegliMigliore(
            statoGiornata, statoGiocatore, opzioni,
            sogliaScore, sogliaSicurezza = sicurezzaRichiesta, pesoRiserva = 4.0
        )
        if (scelta is AzioneGiocatore.GiocaCartaRazza) return scelta

        // 4. AZIONI SECONDARIE
        val blocco = mutableListOf<AzioneSecondaria>()
        val addestramento = cercaAzioneAddestramento(g)
        if (addestramento != null && g.doin > sicurezzaRichiesta) blocco.add(addestramento)

        val acquisto = SelettoreMiniPlancia.suggerisciAcquisto(statoGiornata, statoGiocatore, 4)
        if (acquisto != null && blocco.size < 2) blocco.add(acquisto)

        if (blocco.isNotEmpty()) return AzioneGiocatore.BloccoAzioniSecondarie(blocco)

        return AzioneGiocatore.Passa
    }
}