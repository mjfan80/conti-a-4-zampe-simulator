package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AddestramentoRulesTest {

    @Test
    fun `un cane in addestramento deve diventare addestrato e attivare l'upgrade della carta`() {
        // Setup: Una carta con un cane che sta studiando
        val carta = CartaRazza("Pastore Tedesco", 7, 2, 7, 10, Taglia.GRANDE)
        val caneStudente = Cane.crea(StatoCane.IN_ADDESTRAMENTO)
        carta.cani.add(caneStudente)

        assertFalse(carta.upgrade) // All'inizio non è in upgrade

        val giocatore = Giocatore(1, 10, 0, PlanciaGiocatore(listOf(mutableListOf(carta))))

        // Esecuzione
        applicaRisoluzioneAddestramento(giocatore)

        // Verifica
        assertEquals(StatoCane.ADULTO_ADDESTRATO, caneStudente.stato)
        assertTrue(carta.upgrade) // Ora la carta deve essere in upgrade
    }

    @Test
    fun `con un solo lato libero l'addestramento e consentito solo sulla carta del lato libero`() {
        val cartaSinistra = CartaRazza("Levriero", 6, 2, 5, 8, Taglia.MEDIA)
        val cartaDestra = CartaRazza("Carlino", 4, 1, 3, 6, Taglia.PICCOLA)

        val miniPlancia = MiniPlanciaAddestramento(
            carte = listOf(cartaSinistra, cartaDestra),
            slotSinistro = 0,
            slotSinistroOccupato = true,
            slotDestroOccupato = false
        )

        assertFalse(miniPlancia.haSlotAddestramentoDisponibilePerCarta(cartaSinistra))
        assertTrue(miniPlancia.haSlotAddestramentoDisponibilePerCarta(cartaDestra))

        assertFalse(miniPlancia.occupaSlotAddestramentoPerCarta(cartaSinistra))
        assertTrue(miniPlancia.occupaSlotAddestramentoPerCarta(cartaDestra))
    }
}
