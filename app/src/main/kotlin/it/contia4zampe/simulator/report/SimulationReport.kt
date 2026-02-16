package it.contia4zampe.simulator.report

data class RangeStats(
    val min: Int,
    val max: Int,
    val media: Double
)

data class PlayerAggregateReportRow(
    val playerId: Int,
    val maxCarteInPlancia: Int,
    val doin: RangeStats,
    val debiti: RangeStats,
    val carteGiocate: RangeStats
)

data class WinnerCardStat(
    val cardName: String,
    val count: Int,
    val percentage: Double,
    val neverPlayedByWinners: Boolean
)

data class SimulationSummaryReport(
    val partiteTotali: Int,
    val giornate: RangeStats,
    val puntiVincitori: RangeStats,
    val mediaPuntiTuttiGiocatori: Double,
    val perPlayer: List<PlayerAggregateReportRow>,
    val top5CarteVincitori: List<WinnerCardStat>,
    val bottomCarteVincitori: List<WinnerCardStat>
)
