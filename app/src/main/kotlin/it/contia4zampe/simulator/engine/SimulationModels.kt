package it.contia4zampe.simulator.engine

import it.contia4zampe.simulator.player.PlayerProfile

/**
 * Configurazione di un batch di simulazioni.
 */
data class SimulationConfig(
    val numeroPartite: Int = 1,
    val numeroGiocatori: Int = 2,
    val doinIniziali: Int = 25,
    val debitiIniziali: Int = 0,
    val maxGiornateEvento: Int = 15,
    val sogliaPassaggiOverride: Int? = null,
    val profili: List<PlayerProfile> = emptyList()
) {
    init {
        require(numeroPartite > 0) { "numeroPartite deve essere > 0" }
        require(numeroGiocatori > 0) { "numeroGiocatori deve essere > 0" }
        require(maxGiornateEvento > 0) { "maxGiornateEvento deve essere > 0" }
    }

    fun sogliaPassaggi(): Int {
        return sogliaPassaggiOverride ?: when {
            numeroGiocatori <= 2 -> 1
            numeroGiocatori <= 4 -> 2
            else -> 3
        }
    }
}

data class PlayerGameResult(
    val playerId: Int,
    val puntiFinali: Int,
    val doinFinali: Int,
    val debitiFinali: Int,
    val carteInPlanciaFinali: Int,
    val carteInManoFinali: Int,
    val carteInPlanciaNomi: List<String>
)

data class PartitaResult(
    val gameId: Int,
    val giornateGiocate: Int,
    val playerResults: List<PlayerGameResult>,
    val winnerIds: List<Int>
)

data class SimulationResult(
    val config: SimulationConfig,
    val partite: List<PartitaResult>
)

data class PlayerSnapshot(
    val playerId: Int,
    val doin: Int,
    val debiti: Int,
    val carteInPlancia: Int,
    val carteInMano: Int
)

data class DecisionEvent(
    val gameId: Int,
    val dayNumber: Int,
    val playerId: Int,
    val profileName: String,
    val actionType: String,
    val actionName: String
)
