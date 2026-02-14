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

        val azione = AzioneGiocatore.BloccoAzioniSecondarie(
            listOf(AzioneSecondaria.PagaDebito, AzioneSecondaria.PagaDebito)
        )

        eseguiAzione(azione, sg, statoGenerale)

        assertEquals(3, giocatore.debiti)
        assertEquals(6, giocatore.doin)
    }

    @Test
    fun `acquisto mini plancia scala doin e registra la struttura`() {
        val carta1 = CartaRazza("A", 2, 1, 1, 2, Taglia.MEDIA)
        val carta2 = CartaRazza("B", 2, 1, 1, 2, Taglia.MEDIA)
        val giocatore = Giocatore(
            id = 1,
            doin = 10,
            debiti = 0,
            plancia = PlanciaGiocatore(listOf(mutableListOf(carta1, carta2), mutableListOf(), mutableListOf()))
        )

        val sg = StatoGiocatoreGiornata(giocatore, ProfiloPassivo())
        val statoGenerale = StatoGiornata(1, listOf(sg), sogliaPassaggi = 1)

        val azione = AzioneGiocatore.BloccoAzioniSecondarie(
            listOf(AzioneSecondaria.AcquistaMiniPlancia(indiceRiga = 0, slotSinistro = 0))
        )

        eseguiAzione(azione, sg, statoGenerale)

        assertEquals(5, giocatore.doin)
        assertEquals(1, giocatore.plancia.miniPlanceAddestramento.size)
    }

    @Test
    fun `azione secondaria addestra cane avvia addestramento solo con mini plancia`() {
        val carta = CartaRazza("Labrador", 6, 2, 5, 7, Taglia.MEDIA)
        val supporto = CartaRazza("Supporto", 2, 1, 1, 2, Taglia.MEDIA)
        val c1 = Cane.crea(StatoCane.ADULTO)
        val c2 = Cane.crea(StatoCane.ADULTO)
        val c3 = Cane.crea(StatoCane.ADULTO)
        carta.cani.addAll(listOf(c1, c2, c3))

        val giocatore = Giocatore(
            id = 1,
            doin = 10,
            debiti = 0,
            plancia = PlanciaGiocatore(listOf(mutableListOf(carta, supporto), mutableListOf(), mutableListOf()))
        )

        val sg = StatoGiocatoreGiornata(giocatore, ProfiloPassivo())
        val statoGenerale = StatoGiornata(1, listOf(sg), sogliaPassaggi = 1)

        // Prima senza mini-plancia non deve riuscire
        val azioneSoloAddestra = AzioneGiocatore.BloccoAzioniSecondarie(
            listOf(AzioneSecondaria.AddestraCane(carta, c1))
        )
        eseguiAzione(azioneSoloAddestra, sg, statoGenerale)
        assertEquals(StatoCane.ADULTO, c1.stato)

        // Acquisto mini-plancia e poi addestramento
        val azioneCompra = AzioneGiocatore.BloccoAzioniSecondarie(
            listOf(AzioneSecondaria.AcquistaMiniPlancia(0, 0))
        )
        eseguiAzione(azioneCompra, sg, statoGenerale)

        val azioneAddestra = AzioneGiocatore.BloccoAzioniSecondarie(
            listOf(AzioneSecondaria.AddestraCane(carta, c1))
        )
        eseguiAzione(azioneAddestra, sg, statoGenerale)

        assertEquals(StatoCane.IN_ADDESTRAMENTO, c1.stato)
        assertEquals(3, giocatore.doin) // 10 -5 acquisto -2 addestramento
    }
}
