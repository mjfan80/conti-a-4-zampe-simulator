package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.engine.StatoGiocatoreGiornata
import it.contia4zampe.simulator.engine.StatoGiornata
import it.contia4zampe.simulator.model.CartaRazza
import it.contia4zampe.simulator.model.Giocatore
import it.contia4zampe.simulator.model.PlanciaGiocatore
import it.contia4zampe.simulator.model.Taglia
import it.contia4zampe.simulator.player.ProfiloPassivo
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FinePartitaRulesTest {

    @Test
    fun `partita termina quando mazzo eventi e esaurito`() {
        val giocatore = Giocatore(1, 0, 0, PlanciaGiocatore(listOf(mutableListOf(), mutableListOf(), mutableListOf())))
        val stato = StatoGiornata(
            numero = 15,
            giocatori = listOf(StatoGiocatoreGiornata(giocatore, ProfiloPassivo())),
            sogliaPassaggi = 1,
            maxGiornateEvento = 15
        )

        assertTrue(condizioneMazzoEventiEsaurito(stato))
        assertTrue(deveTerminarePartita(stato))
    }

    @Test
    fun `partita termina quando una plancia e piena`() {
        val rigaPiena = mutableListOf(
            CartaRazza("A", 1, 1, 1, 2, Taglia.MEDIA),
            CartaRazza("B", 1, 1, 1, 2, Taglia.MEDIA),
            CartaRazza("C", 1, 1, 1, 2, Taglia.MEDIA),
            CartaRazza("D", 1, 1, 1, 2, Taglia.MEDIA)
        )

        val giocatore = Giocatore(
            1,
            0,
            0,
            PlanciaGiocatore(listOf(rigaPiena, mutableListOf(), mutableListOf()))
        )

        val stato = StatoGiornata(
            numero = 3,
            giocatori = listOf(StatoGiocatoreGiornata(giocatore, ProfiloPassivo())),
            sogliaPassaggi = 1,
            maxGiornateEvento = 15
        )

        assertFalse(condizioneMazzoEventiEsaurito(stato))
        assertFalse(condizionePlanciaPiena(stato), "Una singola riga piena non basta: serve plancia piena")

        // Completiamo tutte le righe secondo la capienza 6/6/4
        repeat(2) { giocatore.plancia.righe[0].add(CartaRazza("A2$it", 1, 1, 1, 2, Taglia.MEDIA)) }
        repeat(6) { giocatore.plancia.righe[1].add(CartaRazza("X$it", 1, 1, 1, 2, Taglia.MEDIA)) }
        repeat(4) { giocatore.plancia.righe[2].add(CartaRazza("Y$it", 1, 1, 1, 2, Taglia.MEDIA)) }

        assertTrue(condizionePlanciaPiena(stato))
        assertTrue(deveTerminarePartita(stato))
    }
}
