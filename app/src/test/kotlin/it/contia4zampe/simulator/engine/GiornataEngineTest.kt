package it.contia4zampe.simulator.engine

import it.contia4zampe.simulator.model.*
import it.contia4zampe.simulator.player.ProfiloCostruttore
import it.contia4zampe.simulator.player.ProfiloPassivo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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

    @Test
    fun `inizio giornata ruota il primo giocatore ad ogni esecuzione`() {
        val giocatori = listOf(
            StatoGiocatoreGiornata(
                giocatore = Giocatore(1, 0, 0, PlanciaGiocatore(emptyList())),
                profilo = ProfiloPassivo()
            ),
            StatoGiocatoreGiornata(
                giocatore = Giocatore(2, 0, 0, PlanciaGiocatore(emptyList())),
                profilo = ProfiloPassivo()
            ),
            StatoGiocatoreGiornata(
                giocatore = Giocatore(3, 0, 0, PlanciaGiocatore(emptyList())),
                profilo = ProfiloPassivo()
            )
        )

        val stato = StatoGiornata(
            numero = 1,
            giocatori = giocatori,
            sogliaPassaggi = 2,
            indicePrimoGiocatore = 0
        )

        val engine = GiornataEngine()

        engine.eseguiGiornata(stato)
        assertEquals(0, stato.indicePrimoGiocatore)

        stato.numero = 2
        engine.eseguiGiornata(stato)
        assertEquals(1, stato.indicePrimoGiocatore)

        stato.numero = 3
        engine.eseguiGiornata(stato)
        assertEquals(2, stato.indicePrimoGiocatore)
    }

    @Test
    fun `fine giornata deve dichiarare accoppiamento prima dell'upkeep`() {
        val carta = CartaRazza("Labrador", 6, 2, 5, 7, Taglia.MEDIA)
        carta.cani.add(Cane.crea(StatoCane.ADULTO))
        carta.cani.add(Cane.crea(StatoCane.ADULTO))

        val giocatore = Giocatore(
            id = 1,
            doin = 10,
            debiti = 0,
            plancia = PlanciaGiocatore(listOf(mutableListOf(carta)))
        )

        val stato = StatoGiornata(
            numero = 1,
            giocatori = listOf(
                StatoGiocatoreGiornata(giocatore, ProfiloCostruttore())
            ),
            sogliaPassaggi = 1,
            indicePrimoGiocatore = 0
        )

        val engine = GiornataEngine()
        engine.eseguiGiornata(stato)

        assertEquals(FaseGiornata.FINE, stato.fase)
        assertEquals(2, carta.cani.count { it.stato == StatoCane.IN_ACCOPPIAMENTO })
        assertTrue(carta.cani.all { it.statoPrecedente == StatoCane.ADULTO })
    }
}
