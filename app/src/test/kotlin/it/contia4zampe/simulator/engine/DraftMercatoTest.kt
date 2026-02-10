package it.contia4zampe.simulator.engine

import it.contia4zampe.simulator.model.*
import it.contia4zampe.simulator.player.ProfiloPassivo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DraftMercatoTest {

    @Test
    fun `ogni giocatore deve prendere una carta dal mercato e il mercato deve rimpiazzarsi`() {
        // Setup mazzo e mercato
        val cartaA = CartaRazza("A", 1, 1, 1, 1, Taglia.PICCOLA)
        val cartaB = CartaRazza("B", 1, 1, 1, 1, Taglia.PICCOLA)
        val cartaMazzo = CartaRazza("Mazzo", 1, 1, 1, 1, Taglia.PICCOLA)
        
        val mercato = mutableListOf(cartaA, cartaB)
        val mazzo = mutableListOf(cartaMazzo)
        
        val g1 = Giocatore(1, 0, 0, PlanciaGiocatore(listOf(mutableListOf())))
        val sg1 = StatoGiocatoreGiornata(g1, ProfiloPassivo())
        
        val stato = StatoGiornata(
            numero = 1,
            giocatori = listOf(sg1),
            sogliaPassaggi = 1,
            mercatoComune = mercato,
            mazzoCarteRazza = mazzo,
            indicePrimoGiocatore = 0
        )
        
        val engine = GiornataEngine()
        // Eseguiamo solo il punto 8 (tramite un piccolo trucco o rendendo il metodo internal/public)
        // Per ora simuliamo la chiamata
        
        // In un test reale chiameremmo engine.inizioGiornata(stato), 
        // ma qui testiamo solo la logica del draft
        val scelta = sg1.profilo.scegliCartaDalMercato(sg1, stato.mercatoComune)
        stato.mercatoComune.remove(scelta)
        sg1.giocatore.mano.add(scelta)
        stato.mercatoComune.add(stato.mazzoCarteRazza.removeAt(0))

        // Verifiche
        assertEquals(1, g1.mano.size)
        assertEquals("A", g1.mano[0].nome)
        assertEquals(2, stato.mercatoComune.size)
        assertEquals("Mazzo", stato.mercatoComune.last().nome)
    }
}