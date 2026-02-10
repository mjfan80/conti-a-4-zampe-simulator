package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CollassoTest {

    @Test
    fun `se vendo fino a rimanere con un solo adulto la carta deve collassare e vendere l'ultimo cane`() {
        // Setup: Carta con 2 adulti (il minimo per stare in piedi)
        val carta = CartaRazza("Labrador", 6, 2, 5, 7, Taglia.MEDIA)
        val cane1 = Cane.crea(StatoCane.ADULTO)
        val cane2 = Cane.crea(StatoCane.ADULTO)
        carta.cani.add(cane1)
        carta.cani.add(cane2)

        val giocatore = Giocatore(1, 0, 0, PlanciaGiocatore(listOf(mutableListOf(carta))))

        // Esecuzione: Vendiamo il cane 1. Rimane solo il cane 2.
        // La regola dice: adulti < 2 -> Collasso.
        eseguiAzioneVendita(giocatore, listOf(carta to cane1), false)

        // Verifiche
        assertTrue(carta.collassata, "La carta dovrebbe essere collassata")
        assertTrue(carta.cani.isEmpty(), "La carta non dovrebbe avere più cani")
        // Soldi: 6 (vendita volontaria) + 6 (collasso automatico) = 12
        assertEquals(12, giocatore.doin, "Il giocatore dovrebbe aver incassato anche il cane del collasso")
    }
}