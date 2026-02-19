package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.*
import it.contia4zampe.simulator.engine.*
import it.contia4zampe.simulator.player.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class EventiRulesTest {

    @Test
    fun `evento MODIFICA_COSTO_RAZZA_TAGLIA deve cambiare il prezzo solo della taglia corretta`() {
        val chihuahua = CartaRazza("Chihuahua", 3, 1, 1, 1, Taglia.PICCOLA)
        val alano = CartaRazza("Alano", 8, 1, 1, 1, Taglia.GRANDE)
        val giocatore = Giocatore(1, 20, 0, PlanciaGiocatore(listOf(mutableListOf(), mutableListOf(), mutableListOf())))
        
        // Evento: Piccole +2, Grandi -3
        val evento = CartaEvento("Trend di Mercato", TipoEffettoEvento.MODIFICA_COSTO_RAZZA_TAGLIA, 
            variazione = 2, tagliaTarget = Taglia.PICCOLA,
            variazioneSecondaria = -3, tagliaTargetSecondaria = Taglia.GRANDE)

        // Gioco la piccola: 3 (base) + 2 (evento) = 5. Residuo: 20 - 5 = 15
        eseguiGiocaCarta(giocatore, chihuahua, 1, 0, evento)
        assertEquals(15, giocatore.doin)

        // Gioco la grande: 8 (base) - 3 (evento) = 5. Residuo: 15 - 5 = 10
        eseguiGiocaCarta(giocatore, alano, 0, 0, evento)
        assertEquals(10, giocatore.doin)
    }

    @Test
    fun `evento BLOCCO_ACQUISTO_ADDESTRAMENTO deve impedire l'azione`() {
        val giocatore = Giocatore(1, 10, 0, PlanciaGiocatore(listOf(mutableListOf(), mutableListOf(), mutableListOf())))
        val sg = StatoGiocatoreGiornata(giocatore, ProfiloPassivo())
        val evento = CartaEvento("Sciopero Edili", TipoEffettoEvento.BLOCCO_ACQUISTO_ADDESTRAMENTO)
        
        val statoGenerale = StatoGiornata(numero = 2, giocatori = listOf(sg), sogliaPassaggi = 1)
        statoGenerale.eventoAttivo = evento

        val azione = AzioneGiocatore.BloccoAzioniSecondarie(listOf(AzioneSecondaria.AcquistaMiniPlancia(0, 0)))
        
        // Esecuzione tramite processor
        eseguiAzione(azione, sg, statoGenerale)

        // Verifica: i soldi non devono essere scalati e la plancia deve essere vuota
        assertEquals(10, giocatore.doin)
        assertTrue(giocatore.plancia.miniPlanceAddestramento.isEmpty())
    }
}