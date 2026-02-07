package it.contia4zampe.simulator.engine

import it.contia4zampe.simulator.model.Giocatore
import it.contia4zampe.simulator.model.PlanciaGiocatore
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PassaggiGiornataTest {

    @Test
    fun `raggiunta soglia passaggi attiva la chiusura`() {
        val giocatori = listOf(
            StatoGiocatoreGiornata(
                Giocatore(1, 0, 0, PlanciaGiocatore(emptyList()))
            ),
            StatoGiocatoreGiornata(
                Giocatore(2, 0, 0, PlanciaGiocatore(emptyList()))
            )
        )

        val stato = StatoGiornata(
            numero = 1,
            giocatori = giocatori,
            sogliaPassaggi = 1
        )

        val engine = GiornataEngine()
        engine.eseguiGiornata(stato)

        assertTrue(stato.inChiusura)
    }
}
