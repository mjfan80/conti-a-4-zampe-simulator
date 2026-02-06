package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RenditaNettaTest {

    @Test
    fun `rendita netta tiene conto dei debiti`() {
        val carta = CartaRazza(
            nome = "Test",
            costo = 5,
            rendita = 1,
            puntiBase = 1,
            puntiUpgrade = 2,
            taglia = Taglia.MEDIA,
            cani = mutableListOf(
                Cane(StatoCane.ADULTO),              // +1
                Cane(StatoCane.ADULTO_ADDESTRATO)   // +2
            )
        )

        val giocatore = Giocatore(
            id = 1,
            doin = 0,
            debiti = 3, // riduzione = 1
            plancia = PlanciaGiocatore(
                righe = listOf(mutableListOf(carta))
            )
        )

        val renditaNetta = calcolaRenditaNetta(giocatore)

        assertEquals(2, renditaNetta)
    }
}
