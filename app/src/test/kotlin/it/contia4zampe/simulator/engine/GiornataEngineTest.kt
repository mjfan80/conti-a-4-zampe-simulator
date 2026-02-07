package it.contia4zampe.simulator.engine

import it.contia4zampe.simulator.model.*
import it.contia4zampe.simulator.player.ProfiloPassivo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GiornataEngineTest {

    @Test
    fun `giornata eseguita passa per la fase finale`() {
        val statoGiocatore = StatoGiocatoreGiornata(
            giocatore = Giocatore(
                id = 1,
                doin = 0,
                debiti = 0,
                plancia = PlanciaGiocatore(emptyList())
            ),
            profilo = ProfiloPassivo()
        )

    val stato = StatoGiornata(
        numero = 1,
        giocatori = listOf(statoGiocatore),
        sogliaPassaggi = 1
    )

        val engine = GiornataEngine()
        engine.eseguiGiornata(stato)

        assertEquals(FaseGiornata.FINE, stato.fase)
    }
}
