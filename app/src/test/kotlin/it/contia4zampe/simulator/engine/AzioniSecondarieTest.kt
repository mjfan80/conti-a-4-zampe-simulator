package it.contia4zampe.simulator.engine

import it.contia4zampe.simulator.model.*
import it.contia4zampe.simulator.player.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AzioniSecondarieTest {

    @Test
    fun `un blocco di due azioni secondarie Paga Debito deve funzionare correttamente`() {
        val giocatore = Giocatore(1, 10, 5, PlanciaGiocatore(listOf(mutableListOf())))
        val sg = StatoGiocatoreGiornata(giocatore, ProfiloPassivo())
        val statoGenerale = StatoGiornata(1, listOf(sg), sogliaPassaggi = 1)

        // Creiamo il blocco con 2 azioni secondarie
        val azione = AzioneGiocatore.BloccoAzioniSecondarie(
            listOf(AzioneSecondaria.PagaDebito, AzioneSecondaria.PagaDebito)
        )

        eseguiAzione(azione, sg, statoGenerale)

        // Verifica: 5 debiti - 2 = 3. 10 doin - (2*2) = 6.
        assertEquals(3, giocatore.debiti)
        assertEquals(6, giocatore.doin)
    }
}