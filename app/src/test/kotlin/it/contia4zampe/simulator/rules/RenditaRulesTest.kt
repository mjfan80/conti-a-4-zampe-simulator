package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RenditaRulesTest {

    @Test
    fun `rendita calcolata correttamente con cani misti`() {
        val carta = CartaRazza(
            nome = "Test",
            costo = 5,
            rendita = 1,
            puntiBase = 1,
            puntiUpgrade = 2,
            taglia = Taglia.MEDIA,
            cani = mutableListOf(
                Cane.crea(StatoCane.ADULTO),              // +1
                Cane.crea(StatoCane.ADULTO_ADDESTRATO),   // +2
                Cane.crea(StatoCane.CUCCIOLO),            // 0
                Cane.crea(StatoCane.IN_ACCOPPIAMENTO),    // 0
                Cane.crea(StatoCane.IN_ADDESTRAMENTO)     // 0
            )
        )

        val giocatore = Giocatore(
            id = 1,
            doin = 0,
            debiti = 0,
            plancia = PlanciaGiocatore(
                righe = listOf(mutableListOf(carta))
            )
        )

        val rendita = calcolaRendita(giocatore)

        assertEquals(3, rendita)
    }

    @Test
    fun `applicazione rendita aumenta i doin`() {
        val carta = CartaRazza(
            nome = "Test",
            costo = 5,
            rendita = 1,
            puntiBase = 1,
            puntiUpgrade = 2,
            taglia = Taglia.MEDIA,
            cani = mutableListOf(
                Cane.crea(StatoCane.ADULTO),
                Cane.crea(StatoCane.ADULTO)
            )
        )

        val giocatore = Giocatore(
            id = 1,
            doin = 2,
            debiti = 0,
            plancia = PlanciaGiocatore(
                righe = listOf(mutableListOf(carta))
            )
        )

        applicaRendita(giocatore)

        assertEquals(4, giocatore.doin)
    }
}
