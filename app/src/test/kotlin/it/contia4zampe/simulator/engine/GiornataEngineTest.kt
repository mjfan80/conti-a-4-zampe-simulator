package it.contia4zampe.simulator.engine

import it.contia4zampe.simulator.model.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GiornataEngineTest {

    @Test
    fun `giornata eseguita passa per la fase finale`() {
        val giocatore = Giocatore(
            id = 1,
            doin = 0,
            debiti = 0,
            plancia = PlanciaGiocatore(righe = emptyList())
        )

        val stato = StatoGiornata(
            numero = 1,
            giocatori = listOf(giocatore),
            sogliaPassaggi = 1
        )

        val engine = GiornataEngine()
        engine.eseguiGiornata(stato)

        assertEquals(FaseGiornata.FINE, stato.fase)
    }
}
