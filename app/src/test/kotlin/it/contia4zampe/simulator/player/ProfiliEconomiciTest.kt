package it.contia4zampe.simulator.player

import it.contia4zampe.simulator.engine.StatoGiocatoreGiornata
import it.contia4zampe.simulator.engine.StatoGiornata
import it.contia4zampe.simulator.model.*
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertEquals
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
    fun `profilo avventato dichiara accoppiamento in scenario sostenibile`() {
        val carta = CartaRazza("A", 2, 1, 1, 2, Taglia.MEDIA)
        carta.cani.add(Cane.crea(StatoCane.ADULTO))
        carta.cani.add(Cane.crea(StatoCane.ADULTO))
        val profilo = ProfiloAvventato()
        val giocatore = Giocatore(1, 5, 0, PlanciaGiocatore(listOf(mutableListOf(carta), mutableListOf(), mutableListOf())))
        val stato = StatoGiocatoreGiornata(giocatore, profilo)

        assertTrue(profilo.vuoleDichiarareAccoppiamento(stato, carta))
    }

    @Test
    fun `profilo avventato evita accoppiamento se produce troppi debiti`() {
        val carta = CartaRazza("A", 2, 1, 1, 2, Taglia.MEDIA)
        carta.cani.add(Cane.crea(StatoCane.ADULTO))
        carta.cani.add(Cane.crea(StatoCane.ADULTO))
        repeat(10) { carta.cani.add(Cane.crea(StatoCane.CUCCIOLO, 1)) }

        val profilo = ProfiloAvventato()
        val giocatore = Giocatore(1, 0, 0, PlanciaGiocatore(listOf(mutableListOf(carta), mutableListOf(), mutableListOf())))
        val stato = StatoGiocatoreGiornata(giocatore, profilo)

        assertFalse(profilo.vuoleDichiarareAccoppiamento(stato, carta))
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
        val cartaA = CartaRazza("A", 3, 1, 1, 2, Taglia.MEDIA)
        val cartaB = CartaRazza("B", 3, 1, 1, 2, Taglia.MEDIA)
        repeat(2) {
            cartaA.cani.add(Cane.crea(StatoCane.ADULTO))
            cartaB.cani.add(Cane.crea(StatoCane.ADULTO))
        }

        val giocatore = Giocatore(
            id = 1,
            doin = 18,
            debiti = 0,
            plancia = PlanciaGiocatore(
                listOf(
                    mutableListOf(cartaA, cartaB),
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

    @Test
    fun `profili non comprano mini plancia con una sola carta coperta anche con molti doin`() {
        val profili = listOf(
            ProfiloPrudenteBase(),
            ProfiloMoltoAttentoDueTurni(),
            ProfiloCostruttore(),
            ProfiloAvventato()
        )

        profili.forEach { profilo ->
            val giocatore = Giocatore(
                id = 1,
                doin = 20,
                debiti = 0,
                plancia = PlanciaGiocatore(
                    listOf(
                        mutableListOf(CartaRazza("A", 3, 1, 1, 2, Taglia.MEDIA, cani = mutableListOf(Cane.crea(StatoCane.ADULTO), Cane.crea(StatoCane.ADULTO)))),
                        mutableListOf(),
                        mutableListOf()
                    )
                ),
                mano = mutableListOf()
            )

            val statoGiocatore = StatoGiocatoreGiornata(giocatore, profilo)
            val stato = StatoGiornata(numero = 1, giocatori = listOf(statoGiocatore), sogliaPassaggi = 1)
            val azione = profilo.decidiAzione(stato, statoGiocatore)

            assertFalse(
                azione is AzioneGiocatore.BloccoAzioniSecondarie &&
                    (azione.azioni.firstOrNull() is AzioneSecondaria.AcquistaMiniPlancia),
                "${profilo::class.simpleName} non deve comprare mini-plancia con una sola carta coperta"
            )
        }
    }

    @Test
    fun `profili giocano una carta in early game quando la giocata e sostenibile`() {
        val profili = listOf(
            ProfiloPrudenteBase(),
            ProfiloMoltoAttentoDueTurni(),
            ProfiloCostruttore(),
            ProfiloAvventato()
        )

        profili.forEach { profilo ->
            val cartaGiocabile = CartaRazza("Economica", 3, 2, 2, 3, Taglia.MEDIA)
            val giocatore = Giocatore(
                id = 1,
                doin = 25,
                debiti = 0,
                plancia = PlanciaGiocatore(listOf(mutableListOf(), mutableListOf(), mutableListOf())),
                mano = mutableListOf(cartaGiocabile)
            )

            val statoGiocatore = StatoGiocatoreGiornata(giocatore, profilo)
            val stato = StatoGiornata(numero = 1, giocatori = listOf(statoGiocatore), sogliaPassaggi = 1)
            val azione = profilo.decidiAzione(stato, statoGiocatore)

            assertTrue(azione is AzioneGiocatore.GiocaCartaRazza, "${profilo::class.simpleName} dovrebbe giocare carta in early game sostenibile")
        }
    }

    @Test
    fun `costruttore vende cucciolo se la liquidita non copre upkeep attuale`() {
        val carta = CartaRazza("Papillon", 3, 1, 1, 2, Taglia.PICCOLA)
        repeat(4) { carta.cani.add(Cane.crea(StatoCane.CUCCIOLO, 1)) }

        val giocatore = Giocatore(
            id = 1,
            doin = 2,
            debiti = 0,
            plancia = PlanciaGiocatore(listOf(mutableListOf(), mutableListOf(carta), mutableListOf()))
        )

        val profilo = ProfiloCostruttore()
        val statoGiocatore = StatoGiocatoreGiornata(giocatore, profilo)

        val scelta = profilo.decidiGestioneCucciolo(statoGiocatore, carta.cani.first())

        assertEquals(SceltaCucciolo.VENDI, scelta)
    }

    @Test
    fun `avventato vende cucciolo quando e in debito`() {
        val carta = CartaRazza("Drever", 4, 1, 2, 3, Taglia.MEDIA)
        carta.cani.add(Cane.crea(StatoCane.CUCCIOLO, 1))

        val giocatore = Giocatore(
            id = 2,
            doin = 10,
            debiti = 1,
            plancia = PlanciaGiocatore(listOf(mutableListOf(carta), mutableListOf(), mutableListOf()))
        )

        val profilo = ProfiloAvventato()
        val statoGiocatore = StatoGiocatoreGiornata(giocatore, profilo)

        val scelta = profilo.decidiGestioneCucciolo(statoGiocatore, carta.cani.first())

        assertEquals(SceltaCucciolo.VENDI, scelta)
    }

}
