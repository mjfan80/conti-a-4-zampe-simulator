package it.contia4zampe.simulator.player

import it.contia4zampe.simulator.engine.StatoGiornata
import it.contia4zampe.simulator.engine.StatoGiocatoreGiornata
import it.contia4zampe.simulator.model.Cane
import it.contia4zampe.simulator.model.CartaRazza
import it.contia4zampe.simulator.model.SceltaCucciolo
import it.contia4zampe.simulator.rules.calcolaUpkeep
import it.contia4zampe.simulator.rules.puòPiazzareInRiga

class ProfiloPrudenteBase : PlayerProfile {

    override fun decidiAzione(
        statoGiornata: StatoGiornata,
        statoGiocatore: StatoGiocatoreGiornata
    ): AzioneGiocatore {
        val giocatore = statoGiocatore.giocatore
        val upkeepCorrente = calcolaUpkeep(giocatore).costoTotale

        var cartaScelta: CartaRazza? = null
        var rigaScelta = -1

        for (carta in giocatore.mano) {
            if (giocatore.doin >= carta.costo) {
                val doinResidui = giocatore.doin - carta.costo
                if (doinResidui < upkeepCorrente) {
                    continue
                }

                for (indiceRiga in giocatore.plancia.righe.indices) {
                    if (puòPiazzareInRiga(giocatore.plancia, carta, indiceRiga) && giocatore.plancia.haSpazioInRiga(indiceRiga)) {
                        cartaScelta = carta
                        rigaScelta = indiceRiga
                        break
                    }
                }
            }

            if (cartaScelta != null) break
        }

        if (cartaScelta != null) {
            return AzioneGiocatore.GiocaCartaRazza(cartaScelta, rigaScelta, giocatore.plancia.righe[rigaScelta].size)
        }

        return AzioneGiocatore.Passa
    }

    override fun decidiGestioneCucciolo(statoGiocatore: StatoGiocatoreGiornata, cucciolo: Cane): SceltaCucciolo {
        return SceltaCucciolo.VENDI
    }

    override fun scegliCartaDalMercato(giocatore: StatoGiocatoreGiornata, mercato: List<CartaRazza>): CartaRazza {
        return mercato.minByOrNull { it.costo } ?: mercato.first()
    }

    override fun vuoleDichiarareAccoppiamento(statoGiocatore: StatoGiocatoreGiornata, carta: CartaRazza): Boolean {
        // Prudente semplice: accoppia solo se ha un piccolo margine di cassa.
        val upkeepCorrente = calcolaUpkeep(statoGiocatore.giocatore).costoTotale
        return statoGiocatore.giocatore.doin - upkeepCorrente >= 2
    }
}
