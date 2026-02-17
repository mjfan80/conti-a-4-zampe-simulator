package it.contia4zampe.simulator.report

import it.contia4zampe.simulator.engine.PlayerSnapshot
import it.contia4zampe.simulator.engine.SimulationResult
import kotlin.math.round

class SimulationReportAggregator {

    fun aggregate(
        simulationResult: SimulationResult,
        daySnapshots: List<Triple<Int, Int, List<PlayerSnapshot>>>
    ): SimulationSummaryReport {
        val partite = simulationResult.partite
        if (partite.isEmpty()) {
            return SimulationSummaryReport(
                partiteTotali = 0,
                giornate = RangeStats(0, 0, 0.0),
                puntiVincitori = RangeStats(0, 0, 0.0),
                mediaPuntiTuttiGiocatori = 0.0,
                perPlayer = emptyList(),
                top5CarteVincitori = emptyList(),
                bottomCarteVincitori = emptyList()
            )
        }

        val giornateValues = partite.map { it.giornateGiocate }
        val winnerPoints = partite.flatMap { partita ->
            partita.playerResults.filter { it.playerId in partita.winnerIds }.map { it.puntiFinali }
        }
        val allPoints = partite.flatMap { it.playerResults.map { row -> row.puntiFinali } }

        val snapshotsByPlayerId = mutableMapOf<Int, MutableList<PlayerSnapshot>>()
        daySnapshots.forEach { (_, _, snapshots) ->
            snapshots.forEach { snap ->
                snapshotsByPlayerId.getOrPut(snap.playerId) { mutableListOf() }.add(snap)
            }
        }

        val playerIds = partite.first().playerResults.map { it.playerId }
        val perPlayerRows = playerIds.map { playerId ->
            val gameRows = partite.mapNotNull { partita -> partita.playerResults.find { it.playerId == playerId } }
            val doinSamples = snapshotsByPlayerId[playerId]?.map { it.doin }?.ifEmpty { null }
                ?: gameRows.map { it.doinFinali }
            val debitiSamples = snapshotsByPlayerId[playerId]?.map { it.debiti }?.ifEmpty { null }
                ?: gameRows.map { it.debitiFinali }
            val carteSamples = snapshotsByPlayerId[playerId]?.map { it.carteInPlancia }?.ifEmpty { null }
                ?: gameRows.map { it.carteInPlanciaFinali }

            PlayerAggregateReportRow(
                playerId = playerId,
                maxCarteInPlancia = carteSamples.maxOrNull() ?: 0,
                doin = rangeStats(doinSamples),
                debiti = rangeStats(debitiSamples),
                carteGiocate = rangeStats(gameRows.map { it.carteInPlanciaFinali })
            )
        }

        val winnerRows = partite.flatMap { partita ->
            partita.playerResults.filter { it.playerId in partita.winnerIds }
        }

        val allCardsUniverse = partite
            .flatMap { it.playerResults }
            .flatMap { it.carteInPlanciaNomi }
            .toSet()
            .sorted()

        val winnerCardCounts = winnerRows
            .flatMap { it.carteInPlanciaNomi }
            .groupingBy { it }
            .eachCount()

        val winnerTotalCards = winnerRows.sumOf { it.carteInPlanciaNomi.size }.coerceAtLeast(1)

        val cardStatsAll = allCardsUniverse.map { cardName ->
            val count = winnerCardCounts[cardName] ?: 0
            WinnerCardStat(
                cardName = cardName,
                count = count,
                percentage = round2((count.toDouble() / winnerTotalCards.toDouble()) * 100.0),
                neverPlayedByWinners = count == 0
            )
        }

        val top5 = cardStatsAll
            .sortedWith(compareByDescending<WinnerCardStat> { it.count }.thenBy { it.cardName })
            .take(5)

        val zeroCards = cardStatsAll.filter { it.count == 0 }
        val bottom = if (zeroCards.size > 5) {
            zeroCards
        } else {
            val nonZeroLeast = cardStatsAll
                .filter { it.count > 0 }
                .sortedWith(compareBy<WinnerCardStat> { it.count }.thenBy { it.cardName })
                .take(5)
            (nonZeroLeast + zeroCards).distinctBy { it.cardName }
        }

        return SimulationSummaryReport(
            partiteTotali = partite.size,
            giornate = rangeStats(giornateValues),
            puntiVincitori = rangeStats(winnerPoints),
            mediaPuntiTuttiGiocatori = round2(allPoints.average()),
            perPlayer = perPlayerRows,
            top5CarteVincitori = top5,
            bottomCarteVincitori = bottom
        )
    }

    private fun rangeStats(values: List<Int>): RangeStats {
        if (values.isEmpty()) return RangeStats(0, 0, 0.0)
        return RangeStats(
            min = values.minOrNull() ?: 0,
            max = values.maxOrNull() ?: 0,
            media = round2(values.average())
        )
    }

    private fun round2(value: Double): Double {
        return round(value * 100.0) / 100.0
    }
}
