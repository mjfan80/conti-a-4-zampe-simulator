package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EffettiInizioGiornataRulesTest {

    @Test
    fun `l'effetto DOIN1_RENDITA deve aggiungere esattamente 1 doin`() {
        // Setup: Una carta con l'effetto rendita base
        val carta = CartaRazza("Test", 5, 1, 1, 1, Taglia.MEDIA, effettoInizio = EffettoInizioGiornata.DOIN1_RENDITA)
        val giocatore = Giocatore(1, 10, 0, PlanciaGiocatore(listOf(mutableListOf(carta))))

        applicaEffettiInizioGiornata(giocatore)

        // Aveva 10, deve avere 11
        assertEquals(11, giocatore.doin)
    }

    @Test
    fun `l'effetto UNICORIGA deve aggiungere doin solo se la riga ha una sola carta`() {
        // Caso A: Unica carta nella riga
        val cartaUnica = CartaRazza("Unica", 5, 1, 1, 1, Taglia.MEDIA, effettoInizio = EffettoInizioGiornata.DOIN1_RENDITA_UNICORIGA)
        val giocatoreA = Giocatore(1, 10, 0, PlanciaGiocatore(listOf(mutableListOf(cartaUnica))))

        applicaEffettiInizioGiornata(giocatoreA)
        assertEquals(11, giocatoreA.doin) // Bonus applicato

        // Caso B: Due carte nella stessa riga (il bonus non deve scattare)
        val carta1 = CartaRazza("C1", 5, 1, 1, 1, Taglia.MEDIA, effettoInizio = EffettoInizioGiornata.DOIN1_RENDITA_UNICORIGA)
        val carta2 = CartaRazza("C2", 5, 1, 1, 1, Taglia.MEDIA) // Carta normale
        val giocatoreB = Giocatore(2, 10, 0, PlanciaGiocatore(listOf(mutableListOf(carta1, carta2))))

        applicaEffettiInizioGiornata(giocatoreB)
        assertEquals(10, giocatoreB.doin) // Bonus NON applicato
    }
}