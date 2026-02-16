package it.contia4zampe.simulator.player

import it.contia4zampe.simulator.engine.StatoGiornata
import it.contia4zampe.simulator.engine.StatoGiocatoreGiornata
import it.contia4zampe.simulator.model.Cane
import it.contia4zampe.simulator.model.SceltaCucciolo
import it.contia4zampe.simulator.model.CartaRazza
import it.contia4zampe.simulator.rules.puòPiazzareInRiga // Importiamo la regola che abbiamo scritto

class ProfiloCostruttore : PlayerProfile {

    override fun decidiAzione(
        statoGiornata: StatoGiornata,
        statoGiocatore: StatoGiocatoreGiornata
    ): AzioneGiocatore {
        val giocatore = statoGiocatore.giocatore
        val mano = giocatore.mano

        // 1. Ciclo classico sulla mano (stile Java)
        for (i in 0 until mano.size) {
            val carta = mano[i]

            // 2. Verifichiamo se abbiamo i soldi per questa carta
            if (giocatore.doin >= carta.costo) {
                
                // 3. Cerchiamo una riga valida dove piazzarla
                for (indiceRiga in 0 until giocatore.plancia.righe.size) {
                    
                    // Usiamo la regola di posizionamento che abbiamo definito in GiocaCartaRules
                    if (puòPiazzareInRiga(giocatore.plancia, carta, indiceRiga)) {
                        val riga = giocatore.plancia.righe[indiceRiga]
                        
                        // 4. Verifichiamo se c'è spazio fisico (es. max 4 slot per riga)
                        if (riga.size < giocatore.plancia.capacitaRiga(indiceRiga)) {
                            // ABBIAMO TROVATO UNA COMBINAZIONE VALIDA!
                            // Restituiamo l'azione con tutti i parametri richiesti
                            return AzioneGiocatore.GiocaCartaRazza(
                                carta = carta,
                                rigaDestinazione = indiceRiga,
                                slotDestinazione = riga.size // Lo mettiamo nel primo slot libero
                            )
                        }
                    }
                }
            }
        }

        // 5. Se il ciclo finisce e non abbiamo trovato nulla di giocabile (o niente soldi o niente spazio)
        // il giocatore decide di passare.
        return AzioneGiocatore.Passa
    }

    override fun decidiGestioneCucciolo(
        statoGiocatore: StatoGiocatoreGiornata,
        cucciolo: Cane
    ): SceltaCucciolo {
        return SceltaCucciolo.TRASFORMA_IN_ADULTO
    }

    override fun scegliCartaDalMercato(
        giocatore: StatoGiocatoreGiornata,
        mercato: List<CartaRazza>
    ): CartaRazza {
        return mercato.first()
    }

    override fun vuoleDichiarareAccoppiamento(
        statoGiocatore: StatoGiocatoreGiornata,
        carta: CartaRazza
    ): Boolean {
        // Profilo costruttore: cerca crescita e quindi prova sempre ad accoppiare
        return true
    }
}
