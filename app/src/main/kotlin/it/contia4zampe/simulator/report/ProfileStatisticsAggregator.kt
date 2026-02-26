/* FILE: src/main/kotlin/it/contia4zampe/simulator/report/ProfileStatisticsAggregator.kt */

package it.contia4zampe.simulator.report

import it.contia4zampe.simulator.engine.SimulationResult
import kotlin.math.round

/**
 * Dati statistici aggregati per un tipo di profilo.
 */
data class ProfileStatRow(
    val profileName: String,
    val gamesPlayed: Int,
    val wins: Int,
    val winRate: Double,
    val avgPoints: Double,
    val avgDoin: Double,
    val avgDebts: Double,
    val avgCardsInBoard: Double,
    val bankruptcies: Int // Contiamo come "bancarotta" chi finisce con > 20 debiti
)

class ProfileStatisticsAggregator {

    fun aggregate(simulationResult: SimulationResult): List<ProfileStatRow> {
        val config = simulationResult.config
        val partite = simulationResult.partite

        // 1. Mappatura ID Giocatore -> Nome Profilo
        // Nel motore, i profili vengono assegnati ciclicamente:
        // Giocatore 1 = Profilo[0], Giocatore 2 = Profilo[1], ecc.
        val playerProfileMap = mutableMapOf<Int, String>()
        for (id in 1..config.numeroGiocatori) {
            val profileIndex = (id - 1) % config.profili.size
            val profileName = config.profili[profileIndex]::class.simpleName ?: "Unknown"
            playerProfileMap[id] = profileName
        }

        // 2. Raggruppamento risultati per Profilo
        // Creiamo una mappa: NomeProfilo -> Lista di Risultati di quel profilo in tutte le partite
        val resultsByProfile = mutableMapOf<String, MutableList<GameStatData>>()

        for (partita in partite) {
            for (playerRes in partita.playerResults) {
                val pName = playerProfileMap[playerRes.playerId] ?: "Unknown"
                val isWinner = playerRes.playerId in partita.winnerIds

                val data = GameStatData(
                    isWinner = isWinner,
                    points = playerRes.puntiFinali,
                    doin = playerRes.doinFinali,
                    debts = playerRes.debitiFinali,
                    cards = playerRes.carteInPlanciaFinali
                )

                resultsByProfile.getOrPut(pName) { mutableListOf() }.add(data)
            }
        }

        // 3. Calcolo Statistiche
        val rows = mutableListOf<ProfileStatRow>()

        for ((name, dataList) in resultsByProfile) {
            val totalGames = dataList.size
            val wins = dataList.count { it.isWinner }
            val winRate = if (totalGames > 0) (wins.toDouble() / totalGames.toDouble()) * 100.0 else 0.0

            // Definiamo "Bancarotta tecnica" chi finisce con più di 20 debiti (rendita azzerata da tempo)
            val bankruptcies = dataList.count { it.debts > 20 }

            rows.add(
                ProfileStatRow(
                    profileName = name,
                    gamesPlayed = totalGames,
                    wins = wins,
                    winRate = round2(winRate),
                    avgPoints = round2(dataList.map { it.points }.average()),
                    avgDoin = round2(dataList.map { it.doin }.average()),
                    avgDebts = round2(dataList.map { it.debts }.average()),
                    avgCardsInBoard = round2(dataList.map { it.cards }.average()),
                    bankruptcies = bankruptcies
                )
            )
        }

        // Ordiniamo per Win Rate decrescente
        return rows.sortedByDescending { it.winRate }
    }

    private fun round2(value: Double): Double {
        if (value.isNaN()) return 0.0
        return round(value * 100.0) / 100.0
    }

    // Classe di supporto interna per facilitare i calcoli
    private data class GameStatData(
        val isWinner: Boolean,
        val points: Int,
        val doin: Int,
        val debts: Int,
        val cards: Int
    )
}