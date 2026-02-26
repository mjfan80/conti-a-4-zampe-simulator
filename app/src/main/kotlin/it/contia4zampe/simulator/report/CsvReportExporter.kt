/* FILE: src/main/kotlin/it/contia4zampe/simulator/report/CsvReportExporter.kt */

package it.contia4zampe.simulator.report

import it.contia4zampe.simulator.engine.PartitaResult
import java.io.File

class CsvReportExporter {

    data class ExportedFiles(
        val summaryFile: File,
        val gamesFile: File,
        val playersFile: File,
        val winnerCardsFile: File,
        val profileDecisionsFile: File,
        val profileStatsFile: File // <--- NUOVO FILE
    )

    fun export(
        report: SimulationSummaryReport,
        profileStats: List<ProfileStatRow>, // <--- NUOVO PARAMETRO
        games: List<PartitaResult>,
        outputDir: File,
        runId: String
    ): ExportedFiles {
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        val summaryFile = File(outputDir, "summary.csv")
        val gamesFile = File(outputDir, "games.csv")
        val playersFile = File(outputDir, "players.csv")
        val winnerCardsFile = File(outputDir, "winner_cards.csv")
        val profileDecisionsFile = File(outputDir, "profile_decisions.csv")
        val profileStatsFile = File(outputDir, "profile_stats.csv") // <--- NUOVO

        writeSummary(summaryFile, report, runId)
        writeGames(gamesFile, games, runId)
        writePlayers(playersFile, games, report, runId)
        writeWinnerCards(winnerCardsFile, report, runId)
        writeProfileDecisions(profileDecisionsFile, report, runId)

        // Scrittura del nuovo report statistico
        writeProfileStats(profileStatsFile, profileStats, runId)

        return ExportedFiles(
            summaryFile,
            gamesFile,
            playersFile,
            winnerCardsFile,
            profileDecisionsFile,
            profileStatsFile
        )
    }

    private fun writeProfileStats(file: File, stats: List<ProfileStatRow>, runId: String) {
        file.writeText(
            buildString {
                appendLine("run_id,profile_name,games_played,wins,win_rate_percent,avg_points,avg_doin,avg_debts,avg_cards_board,bankruptcies_count")
                stats.forEach { row ->
                    appendLine(
                        listOf(
                            runId,
                            quoted(row.profileName),
                            row.gamesPlayed.toString(),
                            row.wins.toString(),
                            formatDecimal(row.winRate),
                            formatDecimal(row.avgPoints),
                            formatDecimal(row.avgDoin),
                            formatDecimal(row.avgDebts),
                            formatDecimal(row.avgCardsInBoard),
                            row.bankruptcies.toString()
                        ).joinToString(",")
                    )
                }
            }
        )
    }

    // --- LE ALTRE FUNZIONI RIMANGONO INVARIATE (le riporto per completezza del file se serve copia-incolla) ---

    private fun writeSummary(file: File, report: SimulationSummaryReport, runId: String) {
        file.writeText(
            buildString {
                appendLine("run_id,partite_totali,giornate_min,giornate_max,giornate_media,punti_vincitori_min,punti_vincitori_max,punti_vincitori_media,punti_tutti_giocatori_media")
                appendLine(
                    listOf(
                        runId,
                        report.partiteTotali.toString(),
                        report.giornate.min.toString(),
                        report.giornate.max.toString(),
                        formatDecimal(report.giornate.media),
                        report.puntiVincitori.min.toString(),
                        report.puntiVincitori.max.toString(),
                        formatDecimal(report.puntiVincitori.media),
                        formatDecimal(report.mediaPuntiTuttiGiocatori)
                    ).joinToString(",")
                )
            }
        )
    }

    private fun writeGames(file: File, games: List<PartitaResult>, runId: String) {
        file.writeText(
            buildString {
                appendLine("run_id,game_id,giornate_giocate,winner_ids,winner_points,players_count")
                games.forEach { game ->
                    val winnerPoints = game.playerResults
                        .filter { it.playerId in game.winnerIds }
                        .maxOfOrNull { it.puntiFinali } ?: 0

                    appendLine(
                        listOf(
                            runId,
                            game.gameId.toString(),
                            game.giornateGiocate.toString(),
                            quoted(game.winnerIds.joinToString("|")),
                            winnerPoints.toString(),
                            game.playerResults.size.toString()
                        ).joinToString(",")
                    )
                }
            }
        )
    }

    private fun writePlayers(file: File, games: List<PartitaResult>, report: SimulationSummaryReport, runId: String) {
        val perPlayerById = report.perPlayer.associateBy { it.playerId }

        file.writeText(
            buildString {
                appendLine("run_id,game_id,player_id,is_winner,points_final,cards_in_board_final,doin_final,debiti_final,max_cards_in_board,doin_min,doin_max,doin_avg,debiti_min,debiti_max,debiti_avg,cards_played_avg")
                games.forEach { game ->
                    game.playerResults.forEach { row ->
                        val agg = perPlayerById[row.playerId]
                        appendLine(
                            listOf(
                                runId,
                                game.gameId.toString(),
                                row.playerId.toString(),
                                if (row.playerId in game.winnerIds) "1" else "0",
                                row.puntiFinali.toString(),
                                row.carteInPlanciaFinali.toString(),
                                row.doinFinali.toString(),
                                row.debitiFinali.toString(),
                                agg?.maxCarteInPlancia?.toString() ?: "0",
                                agg?.doin?.min?.toString() ?: "0",
                                agg?.doin?.max?.toString() ?: "0",
                                formatDecimal(agg?.doin?.media ?: 0.0),
                                agg?.debiti?.min?.toString() ?: "0",
                                agg?.debiti?.max?.toString() ?: "0",
                                formatDecimal(agg?.debiti?.media ?: 0.0),
                                formatDecimal(agg?.carteGiocate?.media ?: 0.0)
                            ).joinToString(",")
                        )
                    }
                }
            }
        )
    }

    private fun writeWinnerCards(file: File, report: SimulationSummaryReport, runId: String) {
        val topSet = report.top5CarteVincitori.map { it.cardName }.toSet()
        val bottomSet = report.bottomCarteVincitori.map { it.cardName }.toSet()
        val all = (report.top5CarteVincitori + report.bottomCarteVincitori)
            .distinctBy { it.cardName }
            .sortedWith(compareByDescending<WinnerCardStat> { it.count }.thenBy { it.cardName })

        file.writeText(
            buildString {
                appendLine("run_id,card_name,winner_play_count,percentage,is_top5,is_bottom,never_played_by_winners")
                all.forEach { card ->
                    appendLine(
                        listOf(
                            runId,
                            quoted(card.cardName),
                            card.count.toString(),
                            formatDecimal(card.percentage),
                            if (card.cardName in topSet) "1" else "0",
                            if (card.cardName in bottomSet) "1" else "0",
                            if (card.neverPlayedByWinners) "1" else "0"
                        ).joinToString(",")
                    )
                }
            }
        )
    }

    private fun writeProfileDecisions(file: File, report: SimulationSummaryReport, runId: String) {
        file.writeText(
            buildString {
                appendLine("run_id,profile_name,azioni_principali,azioni_secondarie,passaggi,acquisti_mini_plancia,addestramenti,pagamenti_debito")
                report.profileDecisions.forEach { row ->
                    appendLine(
                        listOf(
                            runId,
                            quoted(row.profileName),
                            row.azioniPrincipali.toString(),
                            row.azioniSecondarie.toString(),
                            row.passaggi.toString(),
                            row.acquistiMiniPlancia.toString(),
                            row.addestramenti.toString(),
                            row.pagamentiDebito.toString()
                        ).joinToString(",")
                    )
                }
            }
        )
    }

    private fun quoted(value: String): String = "\"${value.replace("\"", "\"\"")}\""
    private fun formatDecimal(value: Double): String = String.format(java.util.Locale.US, "%.2f", value)
}