package it.contia4zampe.simulator.engine

import it.contia4zampe.simulator.model.*
import it.contia4zampe.simulator.player.ProfiloCostruttore
import it.contia4zampe.simulator.player.ProfiloPassivo
import it.contia4zampe.simulator.player.PlayerProfile
import it.contia4zampe.simulator.player.AzioneGiocatore


import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test


/**
 * Profilo creato appositamente per il test: 
 * vogliamo che dica SEMPRE SI alla dichiarazione di accoppiamento.
 */
class ProfiloSempreAccoppia : PlayerProfile {
    override fun decidiAzione(s: StatoGiornata, sg: StatoGiocatoreGiornata) = AzioneGiocatore.Passa
    override fun decidiGestioneCucciolo(sg: StatoGiocatoreGiornata, c: Cane) = SceltaCucciolo.VENDI
    override fun scegliCartaDalMercato(sg: StatoGiocatoreGiornata, m: List<CartaRazza>) = m.first()
    
    // Forza il SI per il test
    override fun vuoleDichiarareAccoppiamento(sg: StatoGiocatoreGiornata, carta: CartaRazza) = true
}


class GiornataEngineTest {

    @Test
    fun `giornata eseguita passa per la fase finale`() {
        val statoGiocatore = StatoGiocatoreGiornata(
            giocatore = Giocatore(
                id = 1,
                doin = 0,
                debiti = 0,
                plancia = PlanciaGiocatore(emptyList())
            ),
            profilo = ProfiloPassivo()
        )

    val stato = StatoGiornata(
        numero = 1,
        giocatori = listOf(statoGiocatore),
        sogliaPassaggi = 1
    )

        val engine = GiornataEngine()
        engine.eseguiGiornata(stato)

        assertEquals(FaseGiornata.FINE, stato.fase)
    }

    @Test
    fun `inizio giornata ruota il primo giocatore ad ogni esecuzione`() {
        val giocatori = listOf(
            StatoGiocatoreGiornata(
                giocatore = Giocatore(1, 0, 0, PlanciaGiocatore(emptyList())),
                profilo = ProfiloPassivo()
            ),
            StatoGiocatoreGiornata(
                giocatore = Giocatore(2, 0, 0, PlanciaGiocatore(emptyList())),
                profilo = ProfiloPassivo()
            ),
            StatoGiocatoreGiornata(
                giocatore = Giocatore(3, 0, 0, PlanciaGiocatore(emptyList())),
                profilo = ProfiloPassivo()
            )
        )

        val stato = StatoGiornata(
            numero = 1,
            giocatori = giocatori,
            sogliaPassaggi = 2,
            indicePrimoGiocatore = 0
        )

        val engine = GiornataEngine()

        engine.eseguiGiornata(stato)
        assertEquals(0, stato.indicePrimoGiocatore)

        stato.numero = 2
        engine.eseguiGiornata(stato)
        assertEquals(1, stato.indicePrimoGiocatore)

        stato.numero = 3
        engine.eseguiGiornata(stato)
        assertEquals(2, stato.indicePrimoGiocatore)
    }

    @Test
    fun `fine giornata deve invocare la dichiarazione accoppiamenti`() {
        println("TEST: fine giornata deve invocare la dichiarazione accoppiamenti")
        // 1. SETUP
        val carta = CartaRazza("Test", 5, 1, 1, 2, Taglia.MEDIA)
        carta.cani.add(Cane.crea(StatoCane.ADULTO))
        carta.cani.add(Cane.crea(StatoCane.ADULTO))

        val giocatore = Giocatore(1, 10, 0, PlanciaGiocatore(listOf(mutableListOf(carta), mutableListOf(), mutableListOf())))
        
        // Usiamo il nostro profilo "pilota" invece di quello reale
        val statoGiocatore = StatoGiocatoreGiornata(giocatore, ProfiloSempreAccoppia())

        val stato = StatoGiornata(
            numero = 1,
            giocatori = listOf(statoGiocatore),
            sogliaPassaggi = 1
        )

        val engine = GiornataEngine()

        // 2. ESECUZIONE
        engine.eseguiGiornata(stato)

        // 3. VERIFICA
        // Verifichiamo che l'Engine abbia effettivamente chiamato la logica di fine giornata
        val inAccoppiamento = carta.cani.count { it.stato == StatoCane.IN_ACCOPPIAMENTO }
        assertEquals(2, inAccoppiamento, "L'Engine avrebbe dovuto mettere i 2 cani in accoppiamento")
    }
}
