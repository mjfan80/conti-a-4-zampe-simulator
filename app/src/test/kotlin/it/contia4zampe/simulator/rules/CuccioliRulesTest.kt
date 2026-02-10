package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.*
import it.contia4zampe.simulator.engine.*
import it.contia4zampe.simulator.player.ProfiloPassivo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CuccioliRulesTest {

    @Test
    fun `cucciolo nato oggi NON deve maturare subito`() {
        val oggi = 2
        val carta = CartaRazza("Labrador", 6, 2, 5, 7, Taglia.MEDIA)
        // Cucciolo nato oggi (Giorno 2)
        val cuccioloNuovo = Cane.crea(StatoCane.CUCCIOLO, oggi)
        carta.cani.add(cuccioloNuovo)

        val giocatore = Giocatore(1, 0, 0, PlanciaGiocatore(listOf(mutableListOf(carta))))
        val statoG = StatoGiocatoreGiornata(giocatore, ProfiloPassivo())

        // Eseguiamo la maturazione
        applicaMaturazioneCuccioli(statoG, oggi)

        // Verifichiamo: deve essere ancora un cucciolo
        assertEquals(StatoCane.CUCCIOLO, cuccioloNuovo.stato)
        assertEquals(0, giocatore.doin)
    }

    @Test
    fun `cucciolo nato ieri deve maturare e poter essere venduto`() {
        val oggi = 3
        val ieri = 2
        val carta = CartaRazza("Labrador", 6, 2, 5, 7, Taglia.MEDIA)
        // Cucciolo nato ieri
        val cuccioloMaturo = Cane.crea(StatoCane.CUCCIOLO, ieri)
        carta.cani.add(cuccioloMaturo)

        // Usiamo ProfiloPassivo (che abbiamo impostato per VENDERE sempre)
        val giocatore = Giocatore(1, 0, 0, PlanciaGiocatore(listOf(mutableListOf(carta))))
        val statoG = StatoGiocatoreGiornata(giocatore, ProfiloPassivo())

        applicaMaturazioneCuccioli(statoG, oggi)

        // Verifichiamo: la lista cani deve essere vuota (venduto)
        assertTrue(carta.cani.isEmpty())
        // Soldi: Costo Labrador (6) + 3 = 9
        assertEquals(9, giocatore.doin)
    }
}