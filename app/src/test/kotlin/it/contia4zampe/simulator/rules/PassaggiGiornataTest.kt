package it.contia4zampe.simulator.engine

import it.contia4zampe.simulator.model.Giocatore
import it.contia4zampe.simulator.model.PlanciaGiocatore
import it.contia4zampe.simulator.player.ProfiloPassivo
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PassaggiGiornataTest {

    @Test
    fun `raggiunta soglia passaggi attiva la chiusura`() {
        val giocatori = listOf(
            StatoGiocatoreGiornata(
                giocatore = Giocatore(
                    id = 1,
                    doin = 0,
                    debiti = 0,
                    plancia = PlanciaGiocatore(emptyList())
                ),
                profilo = ProfiloPassivo()
            ),
            StatoGiocatoreGiornata(
                giocatore = Giocatore(
                    id = 2,
                    doin = 0,
                    debiti = 0,
                    plancia = PlanciaGiocatore(emptyList())
                ),
                profilo = ProfiloPassivo()
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
