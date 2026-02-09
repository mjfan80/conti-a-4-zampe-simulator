package it.contia4zampe.simulator.player

import it.contia4zampe.simulator.engine.StatoGiornata
import it.contia4zampe.simulator.engine.StatoGiocatoreGiornata

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
}
