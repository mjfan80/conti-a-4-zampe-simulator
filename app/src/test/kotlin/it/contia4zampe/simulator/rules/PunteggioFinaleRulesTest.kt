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
}
