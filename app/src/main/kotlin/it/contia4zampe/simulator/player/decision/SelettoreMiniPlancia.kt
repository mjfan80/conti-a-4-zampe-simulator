package it.contia4zampe.simulator.player.decision

import it.contia4zampe.simulator.engine.StatoGiocatoreGiornata
import it.contia4zampe.simulator.engine.StatoGiornata
import it.contia4zampe.simulator.player.AzioneSecondaria
import it.contia4zampe.simulator.rules.calcolaUpkeep

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
        costoMiniPlancia: Int = 5
    ): AzioneSecondaria.AcquistaMiniPlancia? {
        val g = sg.giocatore
        val upkeepCorrente = calcolaUpkeep(g, stato.eventoAttivo).costoTotale
        val doinResidui = g.doin - costoMiniPlancia
        if (doinResidui < 0) return null
        if (doinResidui < upkeepCorrente + marginePostAcquisto) return null

        val scelta = POSIZIONI_CANDIDATE
            .asSequence()
            .filter { (riga, slotSinistro) -> g.plancia.puòAcquistareMiniPlancia(riga, slotSinistro) }
            .map { (riga, slotSinistro) ->
                ValutazionePosizione(
                    indiceRiga = riga,
                    slotSinistro = slotSinistro,
                    score = scorePosizione(g.plancia.righe[riga], slotSinistro)
                )
            }
            .sortedWith(compareByDescending<ValutazionePosizione> { it.score }.thenBy { it.indiceRiga }.thenBy { it.slotSinistro })
            .firstOrNull()

        return scelta?.let { AzioneSecondaria.AcquistaMiniPlancia(it.indiceRiga, it.slotSinistro) }
    }

    private fun scorePosizione(riga: List<*>, slotSinistro: Int): Int {
        val hasLeftCard = slotSinistro < riga.size
        val hasRightCard = slotSinistro + 1 < riga.size

        var score = 0
        if (hasLeftCard) score += 3
        if (hasRightCard) score += 3
        if (hasLeftCard && hasRightCard) score += 2
        return score
    }

    private data class ValutazionePosizione(
        val indiceRiga: Int,
        val slotSinistro: Int,
        val score: Int
    )
}

