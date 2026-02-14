package it.contia4zampe.simulator.player

import it.contia4zampe.simulator.engine.StatoGiocatoreGiornata
import it.contia4zampe.simulator.engine.StatoGiornata
import it.contia4zampe.simulator.model.*
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ProfiliEconomiciTest {

    @Test
    fun `profilo molto attento evita accoppiamento se rischio debiti oltre soglia`() {
        val carta = CartaRazza("Labrador", 6, 2, 5, 7, Taglia.MEDIA)
        carta.cani.add(Cane.crea(StatoCane.ADULTO))
        carta.cani.add(Cane.crea(StatoCane.ADULTO))
        // Tanti cuccioli gia presenti: upkeep alto, rischio debiti elevato
        repeat(8) { carta.cani.add(Cane.crea(StatoCane.CUCCIOLO, 1)) }

        val giocatore = Giocatore(
            id = 1,
            doin = 0,
            debiti = 0,
            plancia = PlanciaGiocatore(listOf(mutableListOf(carta), mutableListOf(), mutableListOf()))
        )

        val profilo = ProfiloMoltoAttentoDueTurni(sogliaDebitiMassima = 1)
        val stato = StatoGiocatoreGiornata(giocatore, profilo)

        assertFalse(profilo.vuoleDichiarareAccoppiamento(stato, carta))
    }

    @Test
    fun `profilo molto attento accetta accoppiamento in scenario sostenibile`() {
        val carta = CartaRazza("Beagle", 4, 1, 3, 5, Taglia.MEDIA)
        carta.cani.add(Cane.crea(StatoCane.ADULTO))
        carta.cani.add(Cane.crea(StatoCane.ADULTO))

        val giocatore = Giocatore(
            id = 1,
            doin = 20,
            debiti = 0,
            plancia = PlanciaGiocatore(listOf(mutableListOf(carta), mutableListOf(), mutableListOf()))
        )

        val profilo = ProfiloMoltoAttentoDueTurni(sogliaDebitiMassima = 1)
        val stato = StatoGiocatoreGiornata(giocatore, profilo)

        assertTrue(profilo.vuoleDichiarareAccoppiamento(stato, carta))
    }

    @Test
    fun `profilo prudente base evita giocata se svuota troppo la cassa`() {
        val cartaCostosa = CartaRazza("Costosa", 9, 1, 1, 2, Taglia.MEDIA)
        val giocatore = Giocatore(
            id = 1,
            doin = 10,
            debiti = 0,
            plancia = PlanciaGiocatore(listOf(mutableListOf(), mutableListOf(), mutableListOf())),
            mano = mutableListOf(cartaCostosa)
        )

        // Upkeep corrente alto per simulare pressione economica
        giocatore.plancia.righe[0].add(CartaRazza("C1", 1, 1, 1, 1, Taglia.MEDIA, cani = mutableListOf(Cane.crea(StatoCane.ADULTO))))
        giocatore.plancia.righe[0].add(CartaRazza("C2", 1, 1, 1, 1, Taglia.MEDIA, cani = mutableListOf(Cane.crea(StatoCane.ADULTO))))

        val profilo = ProfiloPrudenteBase()
        val statoGiocatore = StatoGiocatoreGiornata(giocatore, profilo)
        val stato = StatoGiornata(
            numero = 1,
            giocatori = listOf(statoGiocatore),
            sogliaPassaggi = 1
        )

        val azione = profilo.decidiAzione(
            statoGiornata = stato,
            statoGiocatore = statoGiocatore
        )

        assertTrue(azione is AzioneGiocatore.Passa)
    }

    @Test
    fun `profilo avventato dichiara sempre accoppiamento`() {
        val carta = CartaRazza("A", 2, 1, 1, 2, Taglia.MEDIA)
        val profilo = ProfiloAvventato()
        val giocatore = Giocatore(1, 0, 0, PlanciaGiocatore(listOf(mutableListOf(carta), mutableListOf(), mutableListOf())))
        val stato = StatoGiocatoreGiornata(giocatore, profilo)

        assertTrue(profilo.vuoleDichiarareAccoppiamento(stato, carta))
    }
}
