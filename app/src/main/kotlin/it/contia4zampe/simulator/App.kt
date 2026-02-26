package it.contia4zampe.simulator

import it.contia4zampe.simulator.engine.InMemorySimulationCollector
import it.contia4zampe.simulator.engine.PartitaEngine
import it.contia4zampe.simulator.engine.SimulationConfig
import it.contia4zampe.simulator.report.CsvReportExporter
import it.contia4zampe.simulator.report.SimulationReportAggregator
import it.contia4zampe.simulator.report.ProfileStatisticsAggregator // <--- IMPORT
import it.contia4zampe.simulator.player.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.io.PrintStream
import java.time.Instant

/**
 * Classe di supporto per sdoppiare l'output.
 * Tutto ciò che viene scritto qui viene inviato a due stream contemporaneamente.
 */
class DualOutputStream(
    private val consoleOut: OutputStream,
    private val fileOut: OutputStream
) : OutputStream() {
    override fun write(b: Int) {
        consoleOut.write(b)
        fileOut.write(b)
    }

    override fun write(b: ByteArray) {
        consoleOut.write(b)
        fileOut.write(b)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        consoleOut.write(b, off, len)
        fileOut.write(b, off, len)
    }

    override fun flush() {
        consoleOut.flush()
        fileOut.flush()
    }

    override fun close() {
        consoleOut.flush()
        fileOut.close() // Chiudiamo solo il file, la console resta aperta
    }
}

fun main(args: Array<String>) {
    // --- 1. SETUP OUTPUT SDOPPIATO (Console + File) ---
    val outputFile = File("output.txt")
    val fileOut = FileOutputStream(outputFile)
    val dualOut = DualOutputStream(System.out, fileOut)
    val dualPrintStream = PrintStream(dualOut, true) // L'opzione 'true' fa l'auto-flush
    System.setOut(dualPrintStream)
    // --------------------------------------------------

    val options = parseArgs(args)

    val numeroPartite = options["partite"]?.toIntOrNull() ?: 100
    val numeroGiocatori = options["giocatori"]?.toIntOrNull() ?: 3
    val maxGiornate = options["max-giornate"]?.toIntOrNull() ?: 16
    val outputDir = File(options["out-dir"] ?: "reports")

    val config = SimulationConfig(
        numeroPartite = numeroPartite,
        numeroGiocatori = numeroGiocatori,
        maxGiornateEvento = maxGiornate,
        profili = listOf(ProfiloCostruttore(), ProfiloAvventato(), ProfiloPrudenteBase())
    )

    val collector = InMemorySimulationCollector()
    val engine = PartitaEngine()

    println("Avvio simulazione: $numeroPartite partite con $numeroGiocatori giocatori...")
    val result = engine.simula(config, collector)

    // 1. Aggregazione Standard
    val aggregator = SimulationReportAggregator()
    val summary = aggregator.aggregate(result, collector.dayEndSnapshots, collector.decisionEvents)

    // 2. Aggregazione per Profilo (NUOVA)
    val profileAggregator = ProfileStatisticsAggregator()
    val profileStats = profileAggregator.aggregate(result)

    // 3. Esportazione CSV
    val runId = "run-${Instant.now().toEpochMilli()}"
    val exporter = CsvReportExporter()

    // Passiamo anche profileStats all'exporter
    val exported = exporter.export(summary, profileStats, result.partite, outputDir, runId)

    println("\nSimulazione completata.")
    println("Report CSV generati in: ${outputDir.absolutePath}")
    println("- ${exported.profileStatsFile.name} (Statistiche per Profilo)")


    println("Simulazione completata.")
    println("Report CSV generati in: ${outputDir.absolutePath}")
    println("- ${exported.profileStatsFile.name} (Statistiche per Profilo)")

    // Piccola anteprima in console
    println("\n--- RISULTATI PER PROFILO ---")
    profileStats.forEach {
        println("${it.profileName.padEnd(20)} | WinRate: ${it.winRate}% | Avg PV: ${it.avgPoints} | Avg Debiti: ${it.avgDebts}")
    }
    // Piccola anteprima in console
    println("\n--- RISULTATI PER PROFILO ---")
    profileStats.forEach {
        println("${it.profileName.padEnd(20)} | WinRate: ${it.winRate}% | Avg PV: ${it.avgPoints} | Avg Debiti: ${it.avgDebts}")
    }

    // Chiudiamo gentilmente il file di output alla fine del programma
    dualPrintStream.close()
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