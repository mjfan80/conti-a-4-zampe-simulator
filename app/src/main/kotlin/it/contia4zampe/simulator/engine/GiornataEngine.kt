package it.contia4zampe.simulator.engine

import it.contia4zampe.simulator.rules.*

class GiornataEngine {

    fun eseguiGiornata(stato: StatoGiornata) {
        inizioGiornata(stato)
        turniGiocatori(stato)
        fineGiornata(stato)
    }

    private fun inizioGiornata(stato: StatoGiornata) {
        stato.fase = FaseGiornata.INIZIO

        stato.giocatori.forEach { giocatore ->
            applicaRenditaNetta(giocatore)
            // TODO:
            // - popolamento carte vuote
            // - risoluzione cuccioli
            // - risoluzione accoppiamenti
            // - risoluzione addestramento
            // - effetti "inizio giornata"
        }
    }

    private fun turniGiocatori(stato: StatoGiornata) {
        stato.fase = FaseGiornata.TURNI

        // TODO:
        // - gestione OPEN / CLOSED
        // - profili giocatore
        // - passaggi
        // - soglia di chiusura

        // Per ora: simulazione vuota
    }

    private fun fineGiornata(stato: StatoGiornata) {
        stato.fase = FaseGiornata.FINE

        stato.giocatori.forEach { giocatore ->
            applicaUpkeep(giocatore)
            applicaCollassoPlancia(giocatore)
        }
    }
}
