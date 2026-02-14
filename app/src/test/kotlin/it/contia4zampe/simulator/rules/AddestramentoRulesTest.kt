package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AddestramentoRulesTest {

    @Test
    fun `un cane in addestramento deve diventare addestrato e attivare l'upgrade della carta`() {
        val cartaA = CartaRazza("Pastore Tedesco", 7, 2, 7, 10, Taglia.GRANDE)
        val cartaB = CartaRazza("Labrador", 6, 2, 5, 7, Taglia.MEDIA)
        val caneStudente = Cane.crea(StatoCane.IN_ADDESTRAMENTO)
        cartaA.cani.add(caneStudente)

        val plancia = PlanciaGiocatore(listOf(mutableListOf(cartaA, cartaB)))
        plancia.acquistaMiniPlancia(0, 0)
        plancia.miniPlanceAddestramento[0].slotOccupati = 1

        assertFalse(cartaA.upgrade)
        val giocatore = Giocatore(1, 10, 0, plancia)

        applicaRisoluzioneAddestramento(giocatore)

        assertEquals(StatoCane.ADULTO_ADDESTRATO, caneStudente.stato)
        assertTrue(cartaA.upgrade)
        assertEquals(0, plancia.miniPlanceAddestramento[0].slotOccupati)
    }

    @Test
    fun `avvio addestramento riuscito con tre adulti cassa e mini plancia`() {
        val cartaA = CartaRazza("Golden", 6, 2, 5, 7, Taglia.MEDIA)
        val cartaB = CartaRazza("Beagle", 4, 1, 3, 5, Taglia.PICCOLA)
        val c1 = Cane.crea(StatoCane.ADULTO)
        val c2 = Cane.crea(StatoCane.ADULTO)
        val c3 = Cane.crea(StatoCane.ADULTO)
        cartaA.cani.addAll(listOf(c1, c2, c3))

        val plancia = PlanciaGiocatore(listOf(mutableListOf(cartaA, cartaB)))
        plancia.acquistaMiniPlancia(0, 0)

        val giocatore = Giocatore(1, 5, 0, plancia)

        val esito = provaAvviareAddestramento(giocatore, cartaA, c1)

        assertTrue(esito)
        assertEquals(3, giocatore.doin)
        assertEquals(StatoCane.IN_ADDESTRAMENTO, c1.stato)
        assertEquals(1, plancia.miniPlanceAddestramento[0].slotOccupati)
    }

    @Test
    fun `avvio addestramento fallisce senza mini plancia sopra carta`() {
        val carta = CartaRazza("Beagle", 4, 1, 3, 5, Taglia.PICCOLA)
        val c1 = Cane.crea(StatoCane.ADULTO)
        val c2 = Cane.crea(StatoCane.ADULTO)
        val c3 = Cane.crea(StatoCane.ADULTO)
        carta.cani.addAll(listOf(c1, c2, c3))

        val giocatore = Giocatore(1, 10, 0, PlanciaGiocatore(listOf(mutableListOf(carta))))

        val esito = provaAvviareAddestramento(giocatore, carta, c1)

        assertFalse(esito)
        assertEquals(10, giocatore.doin)
        assertEquals(StatoCane.ADULTO, c1.stato)
    }
}
