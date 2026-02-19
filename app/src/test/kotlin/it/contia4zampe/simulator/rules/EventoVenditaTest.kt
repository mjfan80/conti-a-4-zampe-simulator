package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EventoVenditaTest {

    @Test
    fun `l'evento MODIFICA_VENDITA deve aumentare il ricavo di ogni cane`() {
        val carta = CartaRazza("Test", 5, 1, 1, 1, Taglia.MEDIA)
        val cane = Cane.crea(StatoCane.ADULTO)
        carta.cani.add(cane)
        val giocatore = Giocatore(1, 0, 0, PlanciaGiocatore(listOf(mutableListOf(carta))))
        
        // Evento: +2 doin per ogni vendita
        val evento = CartaEvento("Super Prezzi", TipoEffettoEvento.MODIFICA_VENDITA, variazione = 2)

        // Esecuzione
        eseguiAzioneVendita(giocatore, listOf(carta to cane), false, evento)

        // Valore base (5) + Evento (2) = 7
        assertEquals(7, giocatore.doin)
    }
}