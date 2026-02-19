package it.contia4zampe.simulator

import it.contia4zampe.simulator.engine.InMemorySimulationCollector
import it.contia4zampe.simulator.engine.PartitaEngine
import it.contia4zampe.simulator.engine.SimulationConfig
import it.contia4zampe.simulator.report.CsvReportExporter
import it.contia4zampe.simulator.report.SimulationReportAggregator
import it.contia4zampe.simulator.player.*
import java.io.File
import java.time.Instant

fun main(args: Array<String>) {
    val options = parseArgs(args)

    val numeroPartite = options["partite"]?.toIntOrNull() ?: 5
    val numeroGiocatori = options["giocatori"]?.toIntOrNull() ?: 3
    val maxGiornate = options["max-giornate"]?.toIntOrNull() ?: 10
    val outputDir = File(options["out-dir"] ?: "reports")

    val config = SimulationConfig(
        numeroPartite = numeroPartite,
        numeroGiocatori = numeroGiocatori,
        maxGiornateEvento = maxGiornate,
        profili = listOf(ProfiloCostruttore(), ProfiloAvventato(), ProfiloPrudenteBase())
    )

    val collector = InMemorySimulationCollector()
    val engine = PartitaEngine()
    val result = engine.simula(config, collector)

    val aggregator = SimulationReportAggregator()
    val summary = aggregator.aggregate(result, collector.dayEndSnapshots)

    val runId = "run-${Instant.now().toEpochMilli()}"
    val exporter = CsvReportExporter()
    val exported = exporter.export(summary, result.partite, outputDir, runId)

    println("Simulazione completata: ${result.partite.size} partite")
    println("Report CSV generati:")
    println("- ${exported.summaryFile.path}")
    println("- ${exported.gamesFile.path}")
    println("- ${exported.playersFile.path}")
    println("- ${exported.winnerCardsFile.path}")
}

private fun parseArgs(args: Array<String>): Map<String, String> {
    val map = mutableMapOf<String, String>()
    args.forEach { arg ->
        if (arg.startsWith("--") && arg.contains("=")) {
            val (k, v) = arg.removePrefix("--").split("=", limit = 2)
            map[k] = v
        }
    }
    return map
}
