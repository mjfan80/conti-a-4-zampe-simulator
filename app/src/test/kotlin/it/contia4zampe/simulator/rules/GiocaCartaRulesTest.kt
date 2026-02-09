package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GiocaCartaRulesTest {

    @Test
    fun `giocare una carta la sposta dalla mano alla plancia e crea due adulti`() {
        val carta1 = CartaRazza(
            nome = "Test",
            costo = 3,
            rendita = 1,
            puntiBase = 1,
            puntiUpgrade = 2,
            taglia = Taglia.MEDIA,
            cani = mutableListOf()
        )

        val carta2 = CartaRazza(
            nome = "Test2",
            costo = 6,
            rendita = 2,
            puntiBase = 1,
            puntiUpgrade = 2,
            taglia = Taglia.MEDIA,
            cani = mutableListOf()
        )

        val giocatore = Giocatore(
            id = 1,
            doin = 10,
            debiti = 0,
            plancia = PlanciaGiocatore(listOf(mutableListOf())),
            mano = mutableListOf(carta1)
        )
        giocatore.mano.add(carta2)

        giocaCartaRazza(giocatore, carta2)

        //assertEquals(2, giocatore.plancia.righe.first().first().cani.size)
        assertEquals(4, giocatore.doin)
        assertEquals(1, giocatore.mano.size)
        assertEquals(1, giocatore.plancia.righe.first().size)
    }


    @Test
fun `a inizio giornata le carte nuove ricevono due cani adulti`() {
    val carta = CartaRazza(
        nome = "Test",
        costo = 3,
        rendita = 1,
        puntiBase = 1,
        puntiUpgrade = 2,
        taglia = Taglia.MEDIA,
        cani = mutableListOf()
    )

    val giocatore = Giocatore(
        id = 1,
        doin = 0,
        debiti = 0,
        plancia = PlanciaGiocatore(listOf(mutableListOf(carta)))
    )

    applicaPopolamentoCarteNuove(giocatore)

    assertEquals(2, carta.cani.size)
}
}
