package it.contia4zampe.simulator.report

import it.contia4zampe.simulator.engine.PartitaResult
import it.contia4zampe.simulator.engine.PlayerGameResult
import it.contia4zampe.simulator.engine.PlayerSnapshot
import it.contia4zampe.simulator.engine.SimulationConfig
import it.contia4zampe.simulator.engine.SimulationResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SimulationReportAggregatorTest {

    @Test
    fun `aggregate calcola statistiche globali e top bottom carte vincitori`() {
        val result = SimulationResult(
            config = SimulationConfig(numeroPartite = 2, numeroGiocatori = 2),
            partite = listOf(
                PartitaResult(
                    gameId = 1,
                    giornateGiocate = 3,
                    winnerIds = listOf(1),
                    playerResults = listOf(
                        PlayerGameResult(1, 10, 8, 1, 2, 0, listOf("Beagle", "Pastore")),
                        PlayerGameResult(2, 8, 6, 2, 1, 0, listOf("Labrador"))
                    )
                ),
                PartitaResult(
                    gameId = 2,
                    giornateGiocate = 5,
                    winnerIds = listOf(2),
                    playerResults = listOf(
                        PlayerGameResult(1, 7, 5, 3, 1, 0, listOf("Beagle")),
                        PlayerGameResult(2, 12, 9, 0, 2, 0, listOf("Labrador", "Carlino"))
                    )
                )
            )
        )

        val snapshots = listOf(
            Triple(1, 1, listOf(PlayerSnapshot(1, 10, 0, 1, 0), PlayerSnapshot(2, 9, 1, 1, 0))),
            Triple(1, 2, listOf(PlayerSnapshot(1, 8, 1, 2, 0), PlayerSnapshot(2, 7, 2, 1, 0))),
            Triple(2, 1, listOf(PlayerSnapshot(1, 6, 2, 1, 0), PlayerSnapshot(2, 11, 0, 2, 0))),
            Triple(2, 2, listOf(PlayerSnapshot(1, 5, 3, 1, 0), PlayerSnapshot(2, 9, 0, 2, 0)))
        )

        val report = SimulationReportAggregator().aggregate(result, snapshots)

        assertEquals(2, report.partiteTotali)
        assertEquals(3, report.giornate.min)
        assertEquals(5, report.giornate.max)
        assertEquals(4.0, report.giornate.media)
        assertEquals(10, report.puntiVincitori.min)
        assertEquals(12, report.puntiVincitori.max)
        assertEquals(11.0, report.puntiVincitori.media)

        val player1 = report.perPlayer.first { it.playerId == 1 }
        assertEquals(2, player1.maxCarteInPlancia)
        assertEquals(5, player1.doin.min)
        assertEquals(10, player1.doin.max)

        assertTrue(report.top5CarteVincitori.any { it.cardName == "Beagle" })
        assertTrue(report.bottomCarteVincitori.any { it.cardName == "Carlino" || it.cardName == "Pastore" || it.cardName == "Labrador" })
    }
}
