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

data class ProfileDecisionReportRow(
    val profileName: String,
    val azioniPrincipali: Int,
    val azioniSecondarie: Int,
    val passaggi: Int,
    val acquistiMiniPlancia: Int,
    val addestramenti: Int,
    val pagamentiDebito: Int
)

data class SimulationSummaryReport(
    val partiteTotali: Int,
    val giornate: RangeStats,
    val puntiVincitori: RangeStats,
    val mediaPuntiTuttiGiocatori: Double,
    val perPlayer: List<PlayerAggregateReportRow>,
    val top5CarteVincitori: List<WinnerCardStat>,
    val bottomCarteVincitori: List<WinnerCardStat>,
    val profileDecisions: List<ProfileDecisionReportRow>
)
