package it.contia4zampe.simulator.engine

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test

class PartitaEngineTest {

    @Test
    fun `avvia partita non deve lanciare eccezioni`() {
        val partitaEngine = PartitaEngine()

        assertDoesNotThrow {
            partitaEngine.avviaPartita()
        }
    }
}
