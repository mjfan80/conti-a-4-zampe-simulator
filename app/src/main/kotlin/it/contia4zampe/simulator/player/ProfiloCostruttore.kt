package it.contia4zampe.simulator.player

import it.contia4zampe.simulator.engine.StatoGiornata
import it.contia4zampe.simulator.engine.StatoGiocatoreGiornata
import it.contia4zampe.simulator.model.Cane
import it.contia4zampe.simulator.model.SceltaCucciolo

class ProfiloCostruttore : PlayerProfile {

    override fun decidiAzione(
        statoGiornata: StatoGiornata,
        statoGiocatore: StatoGiocatoreGiornata
    ): AzioneGiocatore {
        val mano = statoGiocatore.giocatore.mano
        return if (mano.isNotEmpty()) {
            AzioneGiocatore.GiocaCartaRazza(mano.first())
        } else {
            AzioneGiocatore.Passa
        }
    }

    // IMPLEMENTAZIONE MANCANTE:
    override fun decidiGestioneCucciolo(
        statoGiocatore: StatoGiocatoreGiornata,
        cucciolo: Cane
    ): SceltaCucciolo {
        // Il profilo costruttore vuole ingrandire il canile: trasforma in adulto
        return SceltaCucciolo.TRASFORMA_IN_ADULTO
    }
}