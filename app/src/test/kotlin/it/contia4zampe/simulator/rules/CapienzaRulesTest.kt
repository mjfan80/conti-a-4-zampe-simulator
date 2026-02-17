package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CapienzaRulesTest {

    @Test
    fun `il primo cane addestrato non occupa slot di capienza`() {
        // Taglia Grande: limite 3 cani adulti
        val carta = CartaRazza("Alano", 8, 2, 9, 12, Taglia.GRANDE)
        
        // Aggiungiamo 3 adulti e 1 addestrato (Totale 4 cani fisici)
        repeat(3) { carta.cani.add(Cane.crea(StatoCane.ADULTO)) }
        carta.cani.add(Cane.crea(StatoCane.ADULTO_ADDESTRATO))

        // L'occupazione effettiva deve ignorare il primo addestrato -> Risultato: 3
        val occupazione = calcolaOccupazioneEffettiva(carta)
        assertEquals(3, occupazione, "L'addestrato non dovrebbe essere contato")
        
        val giocatore = Giocatore(1, 0, 0, PlanciaGiocatore(listOf(mutableListOf(carta), mutableListOf(), mutableListOf())))
        applicaControlloCapienza(giocatore)

        // Non deve aver venduto nulla perché 3 <= limite 3
        assertEquals(4, carta.cani.size)
    }

    @Test
    fun `vende automaticamente gli adulti in eccesso a fine giornata`() {
        val carta = CartaRazza("Labrador", 6, 2, 5, 7, Taglia.MEDIA) // Limite 4
        // Mettiamo 6 adulti
        repeat(6) { carta.cani.add(Cane.crea(StatoCane.ADULTO)) }

        val giocatore = Giocatore(1, 0, 0, PlanciaGiocatore(listOf(mutableListOf(carta), mutableListOf(), mutableListOf())))
        
        applicaControlloCapienza(giocatore)

        // Deve aver venduto 2 cani (6 - 4 = 2)
        assertEquals(4, carta.cani.size)
        // Ricavo: 6 doin * 2 = 12
        assertEquals(12, giocatore.doin)
    }
}