package it.contia4zampe.simulator.report

import it.contia4zampe.simulator.engine.PartitaResult
import it.contia4zampe.simulator.engine.PlayerGameResult
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files

class CsvReportExporterTest {

    @Test
    fun `export genera i quattro csv attesi`() {
        val tempDir = Files.createTempDirectory("sim-report-test").toFile()

        val report = SimulationSummaryReport(
            partiteTotali = 1,
            giornate = RangeStats(3, 3, 3.0),
            puntiVincitori = RangeStats(10, 10, 10.0),
            mediaPuntiTuttiGiocatori = 9.0,
            perPlayer = listOf(
                PlayerAggregateReportRow(
                    playerId = 1,
                    maxCarteInPlancia = 2,
                    doin = RangeStats(5, 10, 7.5),
                    debiti = RangeStats(0, 2, 1.0),
                    carteGiocate = RangeStats(2, 2, 2.0)
                )
            ),
            top5CarteVincitori = listOf(WinnerCardStat("Beagle", 2, 66.67, false)),
            bottomCarteVincitori = listOf(WinnerCardStat("Carlino", 0, 0.0, true))
        )

        val games = listOf(
            PartitaResult(
                gameId = 1,
                giornateGiocate = 3,
                winnerIds = listOf(1),
                playerResults = listOf(
                    PlayerGameResult(1, 10, 8, 1, 2, 0, listOf("Beagle", "Pastore"))
                )
            )
        )

        val exported = CsvReportExporter().export(report, games, tempDir, "run-test")

        assertTrue(exported.summaryFile.exists())
        assertTrue(exported.gamesFile.exists())
        assertTrue(exported.playersFile.exists())
        assertTrue(exported.winnerCardsFile.exists())

        assertTrue(exported.summaryFile.readText().contains("run-test"))
        assertTrue(exported.gamesFile.readText().contains("winner_ids"))
        assertTrue(exported.playersFile.readText().contains("cards_in_board_final"))
        assertTrue(exported.winnerCardsFile.readText().contains("card_name"))
    }
}
