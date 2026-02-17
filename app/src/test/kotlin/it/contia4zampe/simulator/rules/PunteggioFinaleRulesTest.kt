package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PunteggioFinaleRulesTest {

    @Test
    fun `calcola punti base da carte cani e debiti`() {
        val cartaA = CartaRazza(
            nome = "Labrador",
            costo = 6,
            rendita = 2,
            puntiBase = 5,
            puntiUpgrade = 7,
            taglia = Taglia.MEDIA,
            cani = mutableListOf(
                Cane.crea(StatoCane.ADULTO),            // +1
                Cane.crea(StatoCane.ADULTO_ADDESTRATO), // +2
                Cane.crea(StatoCane.CUCCIOLO, 1)        // +0
            ),
            upgrade = true
        )

        val cartaB = CartaRazza(
            nome = "Beagle",
            costo = 4,
            rendita = 1,
            puntiBase = 3,
            puntiUpgrade = 5,
            taglia = Taglia.PICCOLA,
            cani = mutableListOf(
                Cane.crea(StatoCane.ADULTO) // +1
            ),
            upgrade = false
        )

        val giocatore = Giocatore(
            id = 1,
            doin = 10,
            debiti = 2, // -2
            plancia = PlanciaGiocatore(listOf(mutableListOf(cartaA, cartaB)))
        )

        // carte: 7 + 3 = 10
        // cani: 1 + 2 + 1 = 4
        // debiti: -2
        // totale = 12
        assertEquals(12, calcolaPuntiVittoriaBase(giocatore))
    }

@Test
    fun `il bonus 1PV per adulto deve contare anche cani in addestramento e accoppiamento`() {
        // Setup: Carta con bonus 1PV per ogni adulto
        val carta = CartaRazza("Yorkshire", 4, 1, 5, 7, Taglia.PICCOLA, effettoFine = EffettoFinePartita.BONUS_1PV_ADULTO)
        
        // Aggiungiamo 3 cani in stati diversi (tutti contano come adulti fisici)
        carta.cani.add(Cane.crea(StatoCane.ADULTO))            // +1 base, +1 bonus
        carta.cani.add(Cane.crea(StatoCane.IN_ACCOPPIAMENTO))  // +1 base, +1 bonus
        carta.cani.add(Cane.crea(StatoCane.IN_ADDESTRAMENTO))  // +1 base, +1 bonus
        
        val giocatore = Giocatore(1, 0, 0, PlanciaGiocatore(listOf(mutableListOf(carta), mutableListOf(), mutableListOf())))

        // CALCOLO ATTESO:
        // Carta: 5 PV
        // Cani (base): 1 + 1 + 1 = 3 PV
        // Bonus (1 per adulto): 3 PV
        // TOTALE = 11 PV
        val punti = calcolaPuntiVittoriaBase(giocatore)
        assertEquals(11, punti)
    }

    @Test
    fun `il bonus coppia deve ignorare il cane spaiato`() {
        // Setup: Carta con bonus 2PV per ogni COPPIA
        val carta = CartaRazza("Scottish", 4, 1, 5, 7, Taglia.PICCOLA, effettoFine = EffettoFinePartita.BONUS_2PV_COPPIA_ADULTI)
        
        // Aggiungiamo 3 cani adulti
        repeat(3) { carta.cani.add(Cane.crea(StatoCane.ADULTO)) }
        
        val giocatore = Giocatore(1, 0, 0, PlanciaGiocatore(listOf(mutableListOf(carta), mutableListOf(), mutableListOf())))

        // CALCOLO ATTESO:
        // Carta: 5 PV
        // Cani (base): 1 + 1 + 1 = 3 PV
        // Bonus Coppie: 3 cani = 1 coppia = 2 PV bonus
        // TOTALE = 10 PV
        val punti = calcolaPuntiVittoriaBase(giocatore)
        assertEquals(10, punti)
    }

    @Test
    fun `il punteggio finale deve sottrarre correttamente i debiti`() {
        val carta = CartaRazza("Base", 4, 1, 5, 7, Taglia.PICCOLA)
        val giocatore = Giocatore(1, 0, 3, PlanciaGiocatore(listOf(mutableListOf(carta), mutableListOf(), mutableListOf())))

        // 5 (carta) - 3 (debiti) = 2 PV
        assertEquals(2, calcolaPuntiVittoriaBase(giocatore))
    }


}
