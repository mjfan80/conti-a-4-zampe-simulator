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
        // Se ho debiti o sono quasi a secco, vendo sempre per sopravvivere
        if (g.debiti > 0 || g.doin < 5) {
            return SceltaCucciolo.VENDI
        }
        // Default prudente: vendi. Il Costruttore farà l'override di questo metodo.
        return SceltaCucciolo.VENDI
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
}