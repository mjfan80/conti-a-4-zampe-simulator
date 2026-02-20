package it.contia4zampe.simulator.engine

interface SimulationCollector {
    fun onSimulationStarted(config: SimulationConfig) {}

    fun onGameStarted(gameId: Int, statoIniziale: StatoGiornata) {}

    fun onDayStarted(gameId: Int, dayNumber: Int, snapshots: List<PlayerSnapshot>) {}

    fun onDayEnded(gameId: Int, dayNumber: Int, snapshots: List<PlayerSnapshot>) {}

    fun onDecisionTaken(event: DecisionEvent) {}

    fun onGameEnded(result: PartitaResult) {}

    fun onSimulationEnded(result: SimulationResult) {}
}

object NoOpSimulationCollector : SimulationCollector

/**
 * Collector in-memory utile per test e debug.
 */
class InMemorySimulationCollector : SimulationCollector {
    val dayStartSnapshots: MutableList<Triple<Int, Int, List<PlayerSnapshot>>> = mutableListOf()
    val dayEndSnapshots: MutableList<Triple<Int, Int, List<PlayerSnapshot>>> = mutableListOf()
    val gameResults: MutableList<PartitaResult> = mutableListOf()
    val decisionEvents: MutableList<DecisionEvent> = mutableListOf()

    override fun onDayStarted(gameId: Int, dayNumber: Int, snapshots: List<PlayerSnapshot>) {
        dayStartSnapshots.add(Triple(gameId, dayNumber, snapshots))
    }

    override fun onDayEnded(gameId: Int, dayNumber: Int, snapshots: List<PlayerSnapshot>) {
        dayEndSnapshots.add(Triple(gameId, dayNumber, snapshots))
    }

    override fun onGameEnded(result: PartitaResult) {
        gameResults.add(result)
    }

    override fun onDecisionTaken(event: DecisionEvent) {
        decisionEvents.add(event)
    }
}
