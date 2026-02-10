package it.contia4zampe.simulator.engine

import it.contia4zampe.simulator.model.Giocatore
import it.contia4zampe.simulator.model.PlanciaGiocatore
import it.contia4zampe.simulator.player.ProfiloPassivo

class PartitaEngine {

    private val giornataEngine = GiornataEngine()

    fun avviaPartita() {
        // 1. Creo l'oggetto stato UNA VOLTA sola
        val statoPartita = creaStatoIniziale()

        // 2. Loop finché non finisce
        while (!statoPartita.partitaFinita) {
            println("=== INIZIO GIORNATA ${statoPartita.numero} ===")

            // Eseguo la giornata sullo stesso oggetto
            giornataEngine.eseguiGiornata(statoPartita)

            // Controllo fine partita
            if (statoPartita.numero >= 15) { // Esempio: fermiamoci al giorno 10
                statoPartita.partitaFinita = true
                println("=== PARTITA TERMINATA ===")
            } else {
                // PREPARO LA PROSSIMA GIORNATA
                // Modifico direttamente l'oggetto esistente (Stile Java)
                avanzaGiornata(statoPartita)
            }
        }
    }

    private fun avanzaGiornata(stato: StatoGiornata) {
        // Incremento il giorno
        stato.numero++ 
        
        // Resetto i contatori della giornata
        stato.passaggi = 0
        stato.inChiusura = false
        stato.fase = FaseGiornata.INIZIO

        // Resetto lo stato dei giocatori (tornano tutti OPEN)
        stato.giocatori.forEach { statoGiocatore ->
            statoGiocatore.statoTurno = StatoTurno.OPEN
            statoGiocatore.haFattoUltimoTurno = false
        }
    }

    private fun creaStatoIniziale(): StatoGiornata {
        // Setup finto per test
        val g1 = Giocatore(1, 25, 0, PlanciaGiocatore(listOf(mutableListOf(), mutableListOf(), mutableListOf())))
        val statoG1 = StatoGiocatoreGiornata(g1, ProfiloPassivo())

        return StatoGiornata(
            numero = 1,
            giocatori = listOf(statoG1),
            sogliaPassaggi = 1
        )
    }
}