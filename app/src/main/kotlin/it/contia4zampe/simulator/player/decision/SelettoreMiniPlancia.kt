package it.contia4zampe.simulator.player.decision

import it.contia4zampe.simulator.engine.StatoGiocatoreGiornata
import it.contia4zampe.simulator.engine.StatoGiornata
import it.contia4zampe.simulator.model.CartaRazza
import it.contia4zampe.simulator.model.StatoCane
import it.contia4zampe.simulator.player.AzioneSecondaria
import it.contia4zampe.simulator.rules.calcolaUpkeep

data class ConfigSelettoreMiniPlancia(
    val carteMinimeCoperte: Int = 2,
    val adultiMinimiSullaCoppia: Int = 4,
    val scoreMinimoPosizione: Int = 7
)

object SelettoreMiniPlancia {

    private val POSIZIONI_CANDIDATE = listOf(
        0 to 0,
        0 to 2,
        0 to 4,
        1 to 0,
        1 to 2,
        1 to 4,
        2 to 1
    )

    fun suggerisciAcquisto(
        stato: StatoGiornata,
        sg: StatoGiocatoreGiornata,
        marginePostAcquisto: Int,
        costoMiniPlancia: Int = 5,
        config: ConfigSelettoreMiniPlancia = ConfigSelettoreMiniPlancia()
    ): AzioneSecondaria.AcquistaMiniPlancia? {
        val g = sg.giocatore
        val upkeepCorrente = calcolaUpkeep(g, stato.eventoAttivo).costoTotale
        val doinResidui = g.doin - costoMiniPlancia
        if (doinResidui < 0) return null
        if (doinResidui < upkeepCorrente + marginePostAcquisto) return null

        val scelta = POSIZIONI_CANDIDATE
            .asSequence()
            .filter { (riga, slotSinistro) -> g.plancia.puòAcquistareMiniPlancia(riga, slotSinistro) }
            .mapNotNull { (riga, slotSinistro) ->
                val valutazione = valutaPosizione(g.plancia.righe[riga], slotSinistro)
                if (!valutazione.soddisfaGate(config)) return@mapNotNull null
                ValutazionePosizione(riga, slotSinistro, valutazione.score)
            }
            .sortedWith(compareByDescending<ValutazionePosizione> { it.score }.thenBy { it.indiceRiga }.thenBy { it.slotSinistro })
            .firstOrNull()

        return scelta?.let { AzioneSecondaria.AcquistaMiniPlancia(it.indiceRiga, it.slotSinistro) }
    }

    private fun valutaPosizione(riga: List<CartaRazza>, slotSinistro: Int): ValutazioneGrezza {
        val leftCard = riga.getOrNull(slotSinistro)
        val rightCard = riga.getOrNull(slotSinistro + 1)

        val carteCoperte = listOfNotNull(leftCard, rightCard).size
        val adultiTotali = listOfNotNull(leftCard, rightCard)
            .sumOf { carta -> carta.cani.count { it.stato == StatoCane.ADULTO || it.stato == StatoCane.ADULTO_ADDESTRATO } }

        var score = 0
        if (leftCard != null) score += 3
        if (rightCard != null) score += 3
        if (leftCard != null && rightCard != null) score += 2
        score += adultiTotali

        return ValutazioneGrezza(carteCoperte = carteCoperte, adultiTotali = adultiTotali, score = score)
    }

    private data class ValutazioneGrezza(
        val carteCoperte: Int,
        val adultiTotali: Int,
        val score: Int
    ) {
        fun soddisfaGate(config: ConfigSelettoreMiniPlancia): Boolean {
            if (carteCoperte < config.carteMinimeCoperte) return false
            if (adultiTotali < config.adultiMinimiSullaCoppia) return false
            return score >= config.scoreMinimoPosizione
        }
    }

    private data class ValutazionePosizione(
        val indiceRiga: Int,
        val slotSinistro: Int,
        val score: Int
    )
}
