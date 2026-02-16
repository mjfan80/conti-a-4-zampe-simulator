package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.engine.StatoGiocatoreGiornata
import it.contia4zampe.simulator.model.*
import it.contia4zampe.simulator.player.ProfiloCostruttore
import it.contia4zampe.simulator.player.ProfiloPassivo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AccoppiamentiRulesTest {

    @Test
    fun `prova dichiarazione accoppiamento deve mettere due adulti in accoppiamento`() {
        val carta = CartaRazza("Labrador", 6, 2, 5, 7, Taglia.MEDIA)
        val cane1 = Cane.crea(StatoCane.ADULTO)
        val cane2 = Cane.crea(StatoCane.ADULTO_ADDESTRATO)
        carta.cani.add(cane1)
        carta.cani.add(cane2)

        val esito = provaDichiarareAccoppiamento(carta)

        assertTrue(esito)
        assertEquals(StatoCane.IN_ACCOPPIAMENTO, cane1.stato)
        assertEquals(StatoCane.IN_ACCOPPIAMENTO, cane2.stato)
        assertEquals(StatoCane.ADULTO, cane1.statoPrecedente)
        assertEquals(StatoCane.ADULTO_ADDESTRATO, cane2.statoPrecedente)
    }

    @Test
    fun `prova dichiarazione accoppiamento deve fallire con meno di due adulti`() {
        val carta = CartaRazza("Beagle", 4, 1, 3, 5, Taglia.PICCOLA)
        carta.cani.add(Cane.crea(StatoCane.ADULTO))
        carta.cani.add(Cane.crea(StatoCane.CUCCIOLO, 1))

        val esito = provaDichiarareAccoppiamento(carta)

        assertFalse(esito)
        assertEquals(StatoCane.ADULTO, carta.cani[0].stato)
    }

    @Test
    fun `dichiarazione accoppiamenti segue la scelta del profilo`() {
        val cartaPassivo = CartaRazza("C1", 5, 1, 1, 2, Taglia.MEDIA)
        cartaPassivo.cani.add(Cane.crea(StatoCane.ADULTO))
        cartaPassivo.cani.add(Cane.crea(StatoCane.ADULTO))

        val cartaCostruttore = CartaRazza("C2", 5, 1, 1, 2, Taglia.MEDIA)
        cartaCostruttore.cani.add(Cane.crea(StatoCane.ADULTO))
        cartaCostruttore.cani.add(Cane.crea(StatoCane.ADULTO))

        val giocatorePassivo = Giocatore(1, 0, 0, PlanciaGiocatore(listOf(mutableListOf(cartaPassivo))))
        val giocatoreCostruttore = Giocatore(2, 0, 0, PlanciaGiocatore(listOf(mutableListOf(cartaCostruttore))))

        val statoPassivo = StatoGiocatoreGiornata(giocatorePassivo, ProfiloPassivo())
        val statoCostruttore = StatoGiocatoreGiornata(giocatoreCostruttore, ProfiloCostruttore())

        applicaDichiarazioneAccoppiamenti(statoPassivo)
        applicaDichiarazioneAccoppiamenti(statoCostruttore)

        assertEquals(0, cartaPassivo.cani.count { it.stato == StatoCane.IN_ACCOPPIAMENTO })
        assertEquals(2, cartaCostruttore.cani.count { it.stato == StatoCane.IN_ACCOPPIAMENTO })
    }
}
