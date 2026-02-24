package it.contia4zampe.simulator.player

import it.contia4zampe.simulator.engine.StatoGiornata
import it.contia4zampe.simulator.engine.StatoGiocatoreGiornata
import it.contia4zampe.simulator.model.*
import it.contia4zampe.simulator.player.AzioneGiocatore
import it.contia4zampe.simulator.model.CartaRazza

interface PlayerProfile {

    // Questo rimane astratto: ogni profilo DEVE decidere cosa fare nel turno
    fun decidiAzione(
        statoGiornata: StatoGiornata,
        statoGiocatore: StatoGiocatoreGiornata
    ): AzioneGiocatore

    // LOGICA DI DEFAULT: Valida per quasi tutti (Prudente, Avventato, ecc.)
    fun decidiGestioneCucciolo(sg: StatoGiocatoreGiornata, cucciolo: Cane): SceltaCucciolo {
        val g = sg.giocatore
        val upkeepAttuale = sg.calcolaUpkeepAttuale()
        val upkeepFuturo = sg.calcolaUpkeepFuturo()

        // Se sono sotto pressione economica, monetizzo subito il cucciolo.
        if (g.debiti > 0 || g.doin <= upkeepAttuale || g.doin <= upkeepFuturo) {
            return SceltaCucciolo.VENDI
        }

        // Se economicamente reggo, preferisco far crescere il cane.
        return SceltaCucciolo.TRASFORMA_IN_ADULTO
    }

    // LOGICA DI DEFAULT: Prendi la prima carta (o la più economica)
    fun scegliCartaDalMercato(giocatore: StatoGiocatoreGiornata, mercato: List<CartaRazza>): CartaRazza {
        return mercato.first()
    }

    // LOGICA DI DEFAULT: Accoppia solo se sei in salute economica
    fun vuoleDichiarareAccoppiamento(sg: StatoGiocatoreGiornata, carta: CartaRazza): Boolean {
        val g = sg.giocatore
        return g.debiti == 0 && g.doin >= 15
    }

    // FUNZIONE HELPER: Disponibile per tutti i profili senza doverla riscrivere
    fun trovaCaneSacrificabile(g: Giocatore): DettaglioVendita? {
        for (riga in g.plancia.righe) {
            for (carta in riga) {
                // Cerchiamo carte con almeno 3 cani per non farle collassare
                if (carta.cani.size >= 3) {
                    for (cane in carta.cani) {
                        // Preferiamo vendere adulti semplici
                        if (cane.stato == StatoCane.ADULTO) {
                            return DettaglioVendita(carta, cane)
                        }
                    }
                }
            }
        }
        return null
    }

    fun cercaAzioneAddestramento(g: Giocatore): AzioneSecondaria.AddestraCane? {
        for (riga in g.plancia.righe) {
            for (carta in riga) {
                // Contiamo tutti i tipi di adulti (regola dei 3)
                val adultiTotali = carta.cani.count { 
                    it.stato == StatoCane.ADULTO || 
                    it.stato == StatoCane.ADULTO_ADDESTRATO || 
                    it.stato == StatoCane.IN_ACCOPPIAMENTO || 
                    it.stato == StatoCane.IN_ADDESTRAMENTO 
                }
                
                val haSlotLibero = g.plancia.haSlotAddestramentoDisponibilePerCarta(carta)
                val caneFisicoDisponibile = carta.cani.firstOrNull { it.stato == StatoCane.ADULTO }

                if (adultiTotali >= 3 && haSlotLibero && caneFisicoDisponibile != null) {
                    return AzioneSecondaria.AddestraCane(carta, caneFisicoDisponibile)
                }
            }
        }
        return null
    }

    /**
     * Genera l'elenco di tutte le carte che il giocatore potrebbe legalmente 
     * piazzare sulla sua plancia in questo momento.
     */
    fun generaGiocatePossibili(g: Giocatore): List<AzioneGiocatore> {
        val lista = mutableListOf<AzioneGiocatore>()
        
        // Aggiungiamo sempre il "Passa" come opzione base
        lista.add(AzioneGiocatore.Passa)
        
        // Cicliamo su ogni carta che abbiamo in mano
        for (carta in g.mano) {
            // Proviamo a vedere se sta in una delle 3 righe
            for (indiceRiga in 0 until g.plancia.righe.size) {
                // Se la riga accetta la taglia e ha ancora spazio...
                if (g.plancia.puoOspitareTaglia(indiceRiga, carta.taglia) && g.plancia.haSpazioInRiga(indiceRiga)) {
                    // ...aggiungiamo la giocata alle opzioni
                    lista.add(
                        AzioneGiocatore.GiocaCartaRazza(
                            carta = carta,
                            rigaDestinazione = indiceRiga,
                            slotDestinazione = g.plancia.righe[indiceRiga].size
                        )
                    )
                }
            }
        }
        return lista
    }


    fun trovaCanePerEmergenza(g: Giocatore): DettaglioVendita? {
        // Cerchiamo il primo cane adulto (anche addestrato) per fare cassa subito
        for (riga in g.plancia.righe) {
            for (carta in riga) {
                val cane = carta.cani.firstOrNull { it.stato == StatoCane.ADULTO || it.stato == StatoCane.ADULTO_ADDESTRATO }
                if (cane != null) return DettaglioVendita(carta, cane)
            }
        }
        return null
    }

}
