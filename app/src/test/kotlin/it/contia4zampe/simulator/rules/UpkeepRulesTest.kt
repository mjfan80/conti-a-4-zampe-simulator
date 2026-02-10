package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals


class UpkeepRulesTest {

    @Test
    fun `giocatore con doin sufficienti paga tutto e non genera debiti`() {
        val carta = CartaRazza(
            nome = "Test",
            costo = 5,
            rendita = 1,
            puntiBase = 1,
            puntiUpgrade = 2,
            taglia = Taglia.MEDIA,
            cani = mutableListOf(
                Cane.crea(StatoCane.ADULTO),
                Cane.crea(StatoCane.CUCCIOLO),
                Cane.crea(StatoCane.IN_ADDESTRAMENTO)
            )
        )

        val giocatore = Giocatore(
            id = 1,
            doin = 10,
            debiti = 0,
            plancia = PlanciaGiocatore(
                righe = listOf(mutableListOf(carta))
            )
        )

        applicaUpkeep(giocatore)

        assertEquals(7, giocatore.doin)
        assertEquals(0, giocatore.debiti)
    }

    @Test
    fun `giocatore senza doin sufficienti genera debiti`() {
        val carta = CartaRazza(
            nome = "Test",
            costo = 5,
            rendita = 1,
            puntiBase = 1,
            puntiUpgrade = 2,
            taglia = Taglia.MEDIA,
            cani = mutableListOf(
                Cane.crea(StatoCane.ADULTO),
                Cane.crea(StatoCane.ADULTO),
                Cane.crea(StatoCane.CUCCIOLO),
                Cane.crea(StatoCane.IN_ACCOPPIAMENTO)
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

        applicaUpkeep(giocatore)

        assertEquals(0, giocatore.doin)
        assertEquals(2, giocatore.debiti)
    }
}
