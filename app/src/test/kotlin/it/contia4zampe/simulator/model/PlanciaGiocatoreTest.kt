package it.contia4zampe.simulator.model

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PlanciaGiocatoreTest {

    @Test
    fun `slot sinistro occupato non blocca addestramento sulla carta del lato destro`() {
        val cartaSinistra = CartaRazza("Beagle", 5, 2, 4, 7, Taglia.PICCOLA)
        val cartaDestra = CartaRazza("Border Collie", 6, 2, 6, 9, Taglia.MEDIA)

        val miniPlancia = MiniPlanciaAddestramento(
            carte = listOf(cartaSinistra, cartaDestra),
            slotSinistro = 0,
            slotSinistroOccupato = true,
            slotDestroOccupato = false
        )

        assertFalse(miniPlancia.haSlotAddestramentoDisponibilePerCarta(cartaSinistra))
        assertTrue(miniPlancia.haSlotAddestramentoDisponibilePerCarta(cartaDestra))
        assertTrue(miniPlancia.occupaSlotAddestramentoPerCarta(cartaDestra))
        assertTrue(miniPlancia.slotDestroOccupato)
    }

    @Test
    fun `slot destro occupato non blocca addestramento sulla carta del lato sinistro`() {
        val cartaSinistra = CartaRazza("Maremmano", 7, 3, 8, 11, Taglia.GRANDE)
        val cartaDestra = CartaRazza("Pinscher", 4, 1, 3, 5, Taglia.PICCOLA)

        val miniPlancia = MiniPlanciaAddestramento(
            carte = listOf(cartaSinistra, cartaDestra),
            slotSinistro = 0,
            slotSinistroOccupato = false,
            slotDestroOccupato = true
        )

        assertTrue(miniPlancia.haSlotAddestramentoDisponibilePerCarta(cartaSinistra))
        assertFalse(miniPlancia.haSlotAddestramentoDisponibilePerCarta(cartaDestra))
        assertTrue(miniPlancia.occupaSlotAddestramentoPerCarta(cartaSinistra))
        assertTrue(miniPlancia.slotSinistroOccupato)
    }
}
