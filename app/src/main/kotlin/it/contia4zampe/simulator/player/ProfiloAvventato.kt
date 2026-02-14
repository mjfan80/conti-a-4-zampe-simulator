package it.contia4zampe.simulator.player

import it.contia4zampe.simulator.engine.StatoGiornata
import it.contia4zampe.simulator.engine.StatoGiocatoreGiornata
import it.contia4zampe.simulator.model.Cane
import it.contia4zampe.simulator.model.CartaRazza
import it.contia4zampe.simulator.model.SceltaCucciolo
import it.contia4zampe.simulator.rules.puòPiazzareInRiga

class ProfiloAvventato : PlayerProfile {

    override fun decidiAzione(
        statoGiornata: StatoGiornata,
        statoGiocatore: StatoGiocatoreGiornata
    ): AzioneGiocatore {
        val giocatore = statoGiocatore.giocatore

        for (carta in giocatore.mano) {
            if (giocatore.doin >= carta.costo) {
                for (indiceRiga in giocatore.plancia.righe.indices) {
                    if (puòPiazzareInRiga(giocatore.plancia, carta, indiceRiga) && giocatore.plancia.haSpazioInRiga(indiceRiga)) {
                        return AzioneGiocatore.GiocaCartaRazza(carta, indiceRiga, giocatore.plancia.righe[indiceRiga].size)
                    }
                }
            }
        }

        return AzioneGiocatore.Passa
    }

    override fun decidiGestioneCucciolo(statoGiocatore: StatoGiocatoreGiornata, cucciolo: Cane): SceltaCucciolo {
        return SceltaCucciolo.TRASFORMA_IN_ADULTO
    }

    override fun scegliCartaDalMercato(giocatore: StatoGiocatoreGiornata, mercato: List<CartaRazza>): CartaRazza {
        return mercato.first()
    }

    override fun vuoleDichiarareAccoppiamento(statoGiocatore: StatoGiocatoreGiornata, carta: CartaRazza): Boolean {
        return true
    }
}
