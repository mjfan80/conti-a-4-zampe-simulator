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
        plancia.miniPlanceAddestramento[0].slotSinistroOccupato = true

        assertFalse(cartaA.upgrade)
        val giocatore = Giocatore(1, 10, 0, plancia)

        applicaRisoluzioneAddestramento(giocatore)

        assertEquals(StatoCane.ADULTO_ADDESTRATO, caneStudente.stato)
        assertTrue(cartaA.upgrade)
        assertFalse(plancia.miniPlanceAddestramento[0].slotSinistroOccupato)
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
        assertTrue(plancia.miniPlanceAddestramento[0].slotSinistroOccupato)
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

    @Test
    fun `con un lato occupato addestramento consentito solo sulla carta del lato libero`() {
        val cartaSinistra = CartaRazza("Levriero", 6, 2, 5, 8, Taglia.MEDIA)
        val cartaDestra = CartaRazza("Carlino", 4, 1, 3, 6, Taglia.PICCOLA)

        val caneSinistroA = Cane.crea(StatoCane.ADULTO)
        val caneSinistroB = Cane.crea(StatoCane.ADULTO)
        val caneSinistroC = Cane.crea(StatoCane.ADULTO)
        cartaSinistra.cani.addAll(listOf(caneSinistroA, caneSinistroB, caneSinistroC))

        val caneDestroA = Cane.crea(StatoCane.ADULTO)
        val caneDestroB = Cane.crea(StatoCane.ADULTO)
        val caneDestroC = Cane.crea(StatoCane.ADULTO)
        cartaDestra.cani.addAll(listOf(caneDestroA, caneDestroB, caneDestroC))

        val plancia = PlanciaGiocatore(listOf(mutableListOf(cartaSinistra, cartaDestra)))
        plancia.acquistaMiniPlancia(0, 0)

        val mini = plancia.miniPlanceAddestramento.first()
        mini.slotSinistroOccupato = true
        mini.slotDestroOccupato = false

        val giocatore = Giocatore(1, 10, 0, plancia)

        val esitoSinistra = provaAvviareAddestramento(giocatore, cartaSinistra, caneSinistroA)
        val esitoDestra = provaAvviareAddestramento(giocatore, cartaDestra, caneDestroA)

        assertFalse(esitoSinistra)
        assertTrue(esitoDestra)
        assertEquals(StatoCane.ADULTO, caneSinistroA.stato)
        assertEquals(StatoCane.IN_ADDESTRAMENTO, caneDestroA.stato)
        assertTrue(mini.slotSinistroOccupato)
        assertTrue(mini.slotDestroOccupato)
    }
}
