package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.*
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CollassoRulesTest {

    @Test
    fun `carta con almeno due adulti non collassa`() {
        val carta = CartaRazza(
            nome = "Test",
            costo = 5,
            rendita = 1,
            puntiBase = 1,
            puntiUpgrade = 2,
            taglia = Taglia.MEDIA,
            cani = mutableListOf(
                Cane.crea(StatoCane.ADULTO),
                Cane.crea(StatoCane.ADULTO)
            ),
            collassata = false
        )

        applicaCollasso(carta)

        assertFalse(carta.collassata)
    }

    @Test
    fun `carta con un solo adulto collassa`() {
        val carta = CartaRazza(
            nome = "Test",
            costo = 5,
            rendita = 1,
            puntiBase = 1,
            puntiUpgrade = 2,
            taglia = Taglia.MEDIA,
            cani = mutableListOf(
                Cane.crea(StatoCane.ADULTO)
            ),
            collassata = false
        )

        applicaCollasso(carta)

        assertTrue(carta.collassata)
    }

    @Test
    fun `carta con zero adulti collassa`() {
        val carta = CartaRazza(
            nome = "Test",
            costo = 5,
            rendita = 1,
            puntiBase = 1,
            puntiUpgrade = 2,
            taglia = Taglia.MEDIA,
            cani = mutableListOf(
                Cane.crea(StatoCane.ADULTO),
                Cane.crea(StatoCane.IN_ACCOPPIAMENTO)
            ),
            collassata = false
        )

        applicaCollasso(carta)

        assertTrue(carta.collassata)
    }
}
