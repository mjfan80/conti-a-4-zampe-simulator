package it.contia4zampe.simulator.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PlanciaGiocatoreTest {

    @Test
    fun `plancia piena solo quando tutti gli slot sono occupati`() {
        val plancia = PlanciaGiocatore(listOf(mutableListOf(), mutableListOf(), mutableListOf()))

        assertFalse(plancia.isPiena())

        repeat(6) { plancia.righe[0].add(CartaRazza("A$it", 1, 1, 1, 2, Taglia.MEDIA)) }
        repeat(6) { plancia.righe[1].add(CartaRazza("B$it", 1, 1, 1, 2, Taglia.MEDIA)) }

        assertFalse(plancia.isPiena())

        repeat(4) { plancia.righe[2].add(CartaRazza("C$it", 1, 1, 1, 2, Taglia.MEDIA)) }

        assertTrue(plancia.isPiena())
    }

    @Test
    fun `vincoli taglia per riga rispettati`() {
        val plancia = PlanciaGiocatore(listOf(mutableListOf(), mutableListOf(), mutableListOf()))

        // Riga bassa: solo MEDIA/GRANDE
        assertTrue(plancia.puoOspitareTaglia(PlanciaGiocatore.RIGA_BASSA, Taglia.MEDIA))
        assertTrue(plancia.puoOspitareTaglia(PlanciaGiocatore.RIGA_BASSA, Taglia.GRANDE))
        assertFalse(plancia.puoOspitareTaglia(PlanciaGiocatore.RIGA_BASSA, Taglia.PICCOLA))

        // Riga media: solo PICCOLA/MEDIA
        assertTrue(plancia.puoOspitareTaglia(PlanciaGiocatore.RIGA_MEDIA, Taglia.PICCOLA))
        assertTrue(plancia.puoOspitareTaglia(PlanciaGiocatore.RIGA_MEDIA, Taglia.MEDIA))
        assertFalse(plancia.puoOspitareTaglia(PlanciaGiocatore.RIGA_MEDIA, Taglia.GRANDE))

        // Riga alta: tutte
        assertTrue(plancia.puoOspitareTaglia(PlanciaGiocatore.RIGA_ALTA, Taglia.PICCOLA))
        assertTrue(plancia.puoOspitareTaglia(PlanciaGiocatore.RIGA_ALTA, Taglia.MEDIA))
        assertTrue(plancia.puoOspitareTaglia(PlanciaGiocatore.RIGA_ALTA, Taglia.GRANDE))
    }

    @Test
    fun `coppie addestramento valide secondo struttura plancia`() {
        val plancia = PlanciaGiocatore(listOf(mutableListOf(), mutableListOf(), mutableListOf()))

        // Righe bassa e media: 0-1, 2-3, 4-5
        assertTrue(plancia.coppiaAddestramentoValida(PlanciaGiocatore.RIGA_BASSA, 0))
        assertTrue(plancia.coppiaAddestramentoValida(PlanciaGiocatore.RIGA_BASSA, 2))
        assertTrue(plancia.coppiaAddestramentoValida(PlanciaGiocatore.RIGA_BASSA, 4))
        assertFalse(plancia.coppiaAddestramentoValida(PlanciaGiocatore.RIGA_BASSA, 1))

        assertTrue(plancia.coppiaAddestramentoValida(PlanciaGiocatore.RIGA_MEDIA, 0))
        assertTrue(plancia.coppiaAddestramentoValida(PlanciaGiocatore.RIGA_MEDIA, 2))
        assertTrue(plancia.coppiaAddestramentoValida(PlanciaGiocatore.RIGA_MEDIA, 4))
        assertFalse(plancia.coppiaAddestramentoValida(PlanciaGiocatore.RIGA_MEDIA, 3))

        // Riga alta: solo coppia centrale 1-2
        assertTrue(plancia.coppiaAddestramentoValida(PlanciaGiocatore.RIGA_ALTA, 1))
        assertFalse(plancia.coppiaAddestramentoValida(PlanciaGiocatore.RIGA_ALTA, 0))
        assertFalse(plancia.coppiaAddestramentoValida(PlanciaGiocatore.RIGA_ALTA, 2))

        // Slot addestrabili riga alta
        assertTrue(plancia.slotAddestrabile(PlanciaGiocatore.RIGA_ALTA, 1))
        assertTrue(plancia.slotAddestrabile(PlanciaGiocatore.RIGA_ALTA, 2))
        assertFalse(plancia.slotAddestrabile(PlanciaGiocatore.RIGA_ALTA, 0))
        assertFalse(plancia.slotAddestrabile(PlanciaGiocatore.RIGA_MEDIA, 1))
    }

    @Test
    fun `acquisto mini plancia richiede solo coppia valida anche senza carte sotto`() {
        val plancia = PlanciaGiocatore(listOf(mutableListOf(), mutableListOf(), mutableListOf()))

        assertTrue(plancia.acquistaMiniPlancia(PlanciaGiocatore.RIGA_BASSA, 0))
        assertEquals(1, plancia.miniPlanceAddestramento.size)

        // Duplicata non valida
        assertFalse(plancia.acquistaMiniPlancia(PlanciaGiocatore.RIGA_BASSA, 0))

        // Coppia non valida
        assertFalse(plancia.acquistaMiniPlancia(PlanciaGiocatore.RIGA_BASSA, 1))
    }

    @Test
    fun `slot sinistro e destro sono indipendenti nella stessa mini plancia`() {
        val cartaSinistra = CartaRazza("Beagle", 5, 2, 4, 7, Taglia.PICCOLA)
        val cartaDestra = CartaRazza("Border Collie", 6, 2, 6, 9, Taglia.MEDIA)
        val plancia = PlanciaGiocatore(
            listOf(mutableListOf(cartaSinistra, cartaDestra), mutableListOf(), mutableListOf())
        )

        assertTrue(plancia.acquistaMiniPlancia(PlanciaGiocatore.RIGA_BASSA, 0))
        val miniPlancia = plancia.miniPlanceAddestramento.first()

        miniPlancia.slotSinistroOccupato = true
        miniPlancia.slotDestroOccupato = false

        assertFalse(plancia.haSlotAddestramentoDisponibilePerCarta(cartaSinistra))
        assertTrue(plancia.haSlotAddestramentoDisponibilePerCarta(cartaDestra))
        assertTrue(plancia.occupaSlotAddestramentoPerCarta(cartaDestra))
        assertTrue(miniPlancia.slotDestroOccupato)
    }
}
