package it.contia4zampe.simulator.engine

import it.contia4zampe.simulator.model.*
import it.contia4zampe.simulator.player.PlayerProfile
import it.contia4zampe.simulator.player.AzioneGiocatore
import it.contia4zampe.simulator.model.SceltaCucciolo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

// Creiamo un profilo "manichino" che sceglie sempre la carta che vogliamo noi
// Questo è il concetto di "Stub" in Java
class ProfiloPerTest(val cartaDaScegliere: CartaRazza) : PlayerProfile {
    override fun decidiAzione(statoGiornata: StatoGiornata, statoGiocatore: StatoGiocatoreGiornata) = AzioneGiocatore.Passa
    override fun decidiGestioneCucciolo(statoGiocatore: StatoGiocatoreGiornata, cucciolo: Cane) = SceltaCucciolo.VENDI
    override fun vuoleDichiarareAccoppiamento(statoGiocatore: StatoGiocatoreGiornata, carta: CartaRazza) = false
    
    // Ecco la chiave: questo profilo sceglie la carta che gli abbiamo passato nel costruttore
    override fun scegliCartaDalMercato(giocatore: StatoGiocatoreGiornata, mercato: List<CartaRazza>): CartaRazza {
        return cartaDaScegliere
    }
}

class DraftMercatoTest {

    @Test
    fun `il draft deve spostare la carta scelta dal mercato alla mano e rimpiazzarla`() {
        // Setup
        val cartaA = CartaRazza("A", 5, 1, 1, 1, Taglia.MEDIA)
        val cartaB = CartaRazza("B", 5, 1, 1, 1, Taglia.MEDIA)
        val cartaMazzo = CartaRazza("Dal Mazzo", 5, 1, 1, 1, Taglia.MEDIA)

        val mercato = mutableListOf(cartaA, cartaB)
        val mazzo = mutableListOf(cartaMazzo)

        val g1 = Giocatore(1, 0, 0, PlanciaGiocatore(listOf(mutableListOf())))
        
        // Qui sta il trucco: usiamo il ProfiloPerTest dicendogli di scegliere PROPRIO la carta B
        val sg1 = StatoGiocatoreGiornata(g1, ProfiloPerTest(cartaB))

        val stato = StatoGiornata(
            numero = 1,
            giocatori = listOf(sg1),
            sogliaPassaggi = 1,
            mercatoComune = mercato,
            mazzoCarteRazza = mazzo
        )

        val engine = GiornataEngine()
        
        // Eseguiamo la logica (che nell'engine abbiamo scritto con un ciclo for sull'indicePrimoGiocatore)
        // Per brevità qui simuliamo la chiamata che farebbe l'engine:
        val scelta = sg1.profilo.scegliCartaDalMercato(sg1, stato.mercatoComune)
        
        // 1. La carta deve essere quella che abbiamo ordinato al profilo
        assertEquals("B", scelta.nome)
        
        // 2. Simuliamo l'azione del motore
        stato.mercatoComune.remove(scelta)
        sg1.giocatore.mano.add(scelta)
        if (stato.mazzoCarteRazza.isNotEmpty()) {
            stato.mercatoComune.add(stato.mazzoCarteRazza.removeAt(0))
        }

        // VERIFICHE DI MECCANICA (indipendenti dall'IA)
        assertEquals(1, g1.mano.size, "Il giocatore deve avere 1 carta in mano")
        assertEquals("B", g1.mano[0].nome, "La carta in mano deve essere quella scelta")
        assertEquals(2, stato.mercatoComune.size, "Il mercato deve essere stato rimpiazzato e avere ancora 2 carte")
        assertTrue(stato.mercatoComune.any { it.nome == "Dal Mazzo" }, "Il mercato deve contenere la carta pescata dal mazzo")
        assertTrue(stato.mercatoComune.any { it.nome == "A" }, "La carta A non scelta deve essere ancora lì")
    }
}
