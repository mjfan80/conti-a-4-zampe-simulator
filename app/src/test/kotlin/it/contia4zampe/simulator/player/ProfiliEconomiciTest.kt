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

        val azione = profilo.decidiAzione(stato, statoGiocatore)

        assertTrue(azione is AzioneGiocatore.Passa)
    }

    @Test
    fun `profilo avventato dichiara sempre accoppiamento`() {
        val carta = CartaRazza("A", 2, 1, 1, 2, Taglia.MEDIA)
        val profilo = ProfiloAvventato()
        val giocatore = Giocatore(1, 5, 0, PlanciaGiocatore(listOf(mutableListOf(carta), mutableListOf(), mutableListOf())))
        val stato = StatoGiocatoreGiornata(giocatore, profilo)

        assertTrue(profilo.vuoleDichiarareAccoppiamento(stato, carta))
    }

    @Test
    fun `i profili economici passano quando la carta genera rischio upkeep e debito`() {
        val cartaRischiosa = CartaRazza("Carta Rischiosa", 4, 1, 2, 3, Taglia.MEDIA)
        val profili = listOf(
            ProfiloPrudenteBase(),
            ProfiloMoltoAttentoDueTurni(),
            ProfiloCostruttore(),
            ProfiloAvventato()
        )

        profili.forEach { profilo ->
            val giocatore = Giocatore(
                id = 1,
                doin = 6,
                debiti = 0,
                plancia = PlanciaGiocatore(listOf(mutableListOf(), mutableListOf(), mutableListOf())),
                mano = mutableListOf(cartaRischiosa.copy())
            )

            repeat(5) {
                giocatore.plancia.righe[0].add(
                    CartaRazza(
                        nome = "Base$it",
                        costo = 1,
                        rendita = 1,
                        puntiBase = 1,
                        puntiUpgrade = 1,
                        taglia = Taglia.MEDIA,
                        cani = mutableListOf(Cane.crea(StatoCane.ADULTO))
                    )
                )
            }

            val statoGiocatore = StatoGiocatoreGiornata(giocatore, profilo)
            val stato = StatoGiornata(
                numero = 1,
                giocatori = listOf(statoGiocatore),
                sogliaPassaggi = 1
            )

            val azione = profilo.decidiAzione(stato, statoGiocatore)
            assertTrue(azione is AzioneGiocatore.Passa, "${profilo::class.simpleName} doveva passare in scenario rischioso")
        }
    }

    @Test
    fun `profilo costruttore compra mini plancia su coppia legittima quando passa`() {
        val giocatore = Giocatore(
            id = 1,
            doin = 18,
            debiti = 0,
            plancia = PlanciaGiocatore(
                listOf(
                    mutableListOf(
                        CartaRazza("A", 3, 1, 1, 2, Taglia.MEDIA),
                        CartaRazza("B", 3, 1, 1, 2, Taglia.MEDIA)
                    ),
                    mutableListOf(),
                    mutableListOf()
                )
            ),
            mano = mutableListOf()
        )

        val profilo = ProfiloCostruttore()
        val statoGiocatore = StatoGiocatoreGiornata(giocatore, profilo)
        val stato = StatoGiornata(numero = 1, giocatori = listOf(statoGiocatore), sogliaPassaggi = 1)

        val azione = profilo.decidiAzione(stato, statoGiocatore)

        assertTrue(azione is AzioneGiocatore.BloccoAzioniSecondarie)
        val subAzione = (azione as AzioneGiocatore.BloccoAzioniSecondarie).azioni.first()
        assertTrue(subAzione is AzioneSecondaria.AcquistaMiniPlancia)
        val acquisto = subAzione as AzioneSecondaria.AcquistaMiniPlancia
        assertTrue(giocatore.plancia.coppiaAddestramentoValida(acquisto.indiceRiga, acquisto.slotSinistro))
    }

    @Test
    fun `profilo prudente non compra mini plancia senza margine economico`() {
        val giocatore = Giocatore(
            id = 1,
            doin = 6,
            debiti = 0,
            plancia = PlanciaGiocatore(
                listOf(
                    mutableListOf(CartaRazza("A", 3, 1, 1, 2, Taglia.MEDIA, cani = mutableListOf(Cane.crea(StatoCane.ADULTO)))),
                    mutableListOf(),
                    mutableListOf()
                )
            ),
            mano = mutableListOf()
        )

        val profilo = ProfiloPrudenteBase()
        val statoGiocatore = StatoGiocatoreGiornata(giocatore, profilo)
        val stato = StatoGiornata(numero = 1, giocatori = listOf(statoGiocatore), sogliaPassaggi = 1)

        val azione = profilo.decidiAzione(stato, statoGiocatore)

        assertTrue(azione is AzioneGiocatore.Passa)
    }

}
