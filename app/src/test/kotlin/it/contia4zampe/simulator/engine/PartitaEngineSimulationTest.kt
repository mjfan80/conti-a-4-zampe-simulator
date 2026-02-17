package it.contia4zampe.simulator.engine

import it.contia4zampe.simulator.player.ProfiloPassivo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PartitaEngineSimulationTest {

    @Test
    fun `simula restituisce risultato strutturato per batch di partite`() {
        val engine = PartitaEngine()
        val config = SimulationConfig(
            numeroPartite = 2,
            numeroGiocatori = 2,
            maxGiornateEvento = 3,
            profili = listOf(ProfiloPassivo())
        )

        val result = engine.simula(config)

        assertEquals(2, result.partite.size)
        assertTrue(result.partite.all { it.giornateGiocate == 3 })
        assertTrue(result.partite.all { it.playerResults.size == 2 })
        assertTrue(result.partite.all { it.winnerIds.isNotEmpty() })
    }

    @Test
    fun `collector in-memory raccoglie snapshot giornalieri e risultati finali`() {
        val engine = PartitaEngine()
        val collector = InMemorySimulationCollector()

        val config = SimulationConfig(
            numeroPartite = 1,
            numeroGiocatori = 1,
            maxGiornateEvento = 2,
            profili = listOf(ProfiloPassivo())
        )

        val result = engine.simula(config, collector)

        assertEquals(1, collector.gameResults.size)
        assertFalse(collector.dayStartSnapshots.isEmpty())
        assertFalse(collector.dayEndSnapshots.isEmpty())

        val collectedGameResult = collector.gameResults.first()
        val returnedGameResult = result.partite.first()

        assertEquals(returnedGameResult.gameId, collectedGameResult.gameId)
        assertEquals(returnedGameResult.giornateGiocate, collectedGameResult.giornateGiocate)
    }
}
