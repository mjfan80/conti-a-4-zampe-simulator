package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GiocaCartaRulesTest {

    @Test
    fun `giocare una carta la sposta dalla mano alla plancia e scala i doin`() {
        // Setup delle carte
        val carta1 = CartaRazza(
            nome = "Test1",
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

        // Setup giocatore con plancia a 3 righe vuote
        val giocatore = Giocatore(
            id = 1,
            doin = 10,
            debiti = 0,
            plancia = PlanciaGiocatore(
                righe = listOf(
                    mutableListOf(), // riga 0
                    mutableListOf(), // riga 1
                    mutableListOf()  // riga 2
                )
            ),
            mano = mutableListOf(carta1, carta2)
        )

        // ESECUZIONE: Chiamiamo la funzione corretta 'eseguiGiocaCarta'
        // Proviamo a giocare la carta2 (costo 6) nella riga centrale (indice 1)
        val esito = eseguiGiocaCarta(
            giocatore = giocatore,
            carta = carta2,
            indiceRiga = 1,
            indiceSlot = 0
        )

        // VERIFICHE
        assertEquals(true, esito, "L'azione dovrebbe essere riuscita")
        assertEquals(4, giocatore.doin, "I doin dovrebbero essere 10 - 6 = 4")
        assertEquals(1, giocatore.mano.size, "Dovrebbe essere rimasta solo una carta in mano")
        assertEquals(1, giocatore.plancia.righe[1].size, "La riga 1 dovrebbe contenere una carta")
        assertEquals("Test2", giocatore.plancia.righe[1][0].nome)
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
            plancia = PlanciaGiocatore(listOf(mutableListOf(carta), mutableListOf(), mutableListOf()))
        )

        applicaPopolamentoCarteNuove(giocatore)

        assertEquals(2, carta.cani.size)
        assertEquals(StatoCane.ADULTO, carta.cani[0].stato)
    }

    @Test
    fun `non deve giocare taglia piccola nella riga bassa`() {
        val cartaPiccola = CartaRazza("Piccola", 2, 1, 1, 2, Taglia.PICCOLA)
        val giocatore = Giocatore(
            id = 1,
            doin = 10,
            debiti = 0,
            plancia = PlanciaGiocatore(listOf(mutableListOf(), mutableListOf(), mutableListOf())),
            mano = mutableListOf(cartaPiccola)
        )

        val esito = eseguiGiocaCarta(giocatore, cartaPiccola, indiceRiga = PlanciaGiocatore.RIGA_BASSA, indiceSlot = 0)

        assertEquals(false, esito)
        assertEquals(10, giocatore.doin)
        assertEquals(1, giocatore.mano.size)
        assertEquals(0, giocatore.plancia.righe[PlanciaGiocatore.RIGA_BASSA].size)
    }

    @Test
    fun `riga alta accetta massimo quattro carte`() {
        val rigaAlta = mutableListOf(
            CartaRazza("A", 1, 1, 1, 2, Taglia.MEDIA),
            CartaRazza("B", 1, 1, 1, 2, Taglia.MEDIA),
            CartaRazza("C", 1, 1, 1, 2, Taglia.MEDIA),
            CartaRazza("D", 1, 1, 1, 2, Taglia.MEDIA)
        )

        val cartaNuova = CartaRazza("Nuova", 1, 1, 1, 2, Taglia.GRANDE)
        val giocatore = Giocatore(
            id = 1,
            doin = 10,
            debiti = 0,
            plancia = PlanciaGiocatore(listOf(mutableListOf(), mutableListOf(), rigaAlta)),
            mano = mutableListOf(cartaNuova)
        )

        val esito = eseguiGiocaCarta(giocatore, cartaNuova, indiceRiga = PlanciaGiocatore.RIGA_ALTA, indiceSlot = 4)

        assertEquals(false, esito)
        assertEquals(4, giocatore.plancia.righe[PlanciaGiocatore.RIGA_ALTA].size)
    }
}
