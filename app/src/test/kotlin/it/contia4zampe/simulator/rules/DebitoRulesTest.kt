package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.Giocatore
import it.contia4zampe.simulator.model.PlanciaGiocatore
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DebitoRulesTest {

    @Test
    fun `nessuna riduzione con 0 o 1 debiti`() {
        val giocatore = Giocatore(
            id = 1,
            doin = 0,
            debiti = 1,
            plancia = PlanciaGiocatore(righe = emptyList())
        )

        val rendita = applicaRiduzioneDaDebito(5, giocatore)

        assertEquals(5, rendita)
    }

    @Test
    fun `riduzione corretta con debiti pari`() {
        val giocatore = Giocatore(
            id = 1,
            doin = 0,
            debiti = 4,
            plancia = PlanciaGiocatore(righe = emptyList())
        )

        val rendita = applicaRiduzioneDaDebito(6, giocatore)

        assertEquals(5, rendita)
    }

    @Test
    fun `rendita non scende sotto zero`() {
        val giocatore = Giocatore(
            id = 1,
            doin = 0,
            debiti = 10,
            plancia = PlanciaGiocatore(righe = emptyList())
        )

        val rendita = applicaRiduzioneDaDebito(3, giocatore)

        assertEquals(0, rendita)
    }
}
