package it.contia4zampe.simulator.engine

import it.contia4zampe.simulator.player.*
import it.contia4zampe.simulator.rules.*
import it.contia4zampe.simulator.model.CartaEvento
import it.contia4zampe.simulator.model.TipoEffettoEvento

fun eseguiAzione(
    azione: AzioneGiocatore,
    statoGiocatore: StatoGiocatoreGiornata,
    statoGenerale: StatoGiornata
) {
    val giocatore = statoGiocatore.giocatore
     val evento = statoGenerale.eventoAttivo // Recuperiamo l'evento attivo

    when (azione) {
        is AzioneGiocatore.GiocaCartaRazza -> {
            eseguiGiocaCarta(giocatore, azione.carta, azione.rigaDestinazione, azione.slotDestinazione, evento)
        }

        is AzioneGiocatore.VendiCani -> {
            val lista = azione.vendite.map { it.carta to it.cane }
            val haPescatoExtra = eseguiAzioneVendita(giocatore, lista, azione.pescaCartaInveceDi5Doin, evento)
            
            if (haPescatoExtra && statoGenerale.mazzoCarteRazza.isNotEmpty()) {
                val nuova = statoGenerale.mazzoCarteRazza.removeAt(0)
                giocatore.mano.add(nuova)
            }
        }

        is AzioneGiocatore.BloccoAzioniSecondarie -> {
            // Gestiamo il blocco delle (fino a) 2 azioni
            // Limiti di sicurezza: non più di 2
            val limite = if (azione.azioni.size > 2) 2 else azione.azioni.size
            
            for (i in 0 until limite) {
                eseguiSingolaAzioneSecondaria(azione.azioni[i], statoGiocatore, evento)
            }
        }

        is AzioneGiocatore.Passa -> {
            // Logica di chiusura gestita dall'Engine
        }
    }
}

private fun eseguiSingolaAzioneSecondaria(subAzione: AzioneSecondaria, sg: StatoGiocatoreGiornata, evento: CartaEvento?) {
    val giocatore = sg.giocatore

    when (subAzione) {
        is AzioneSecondaria.AcquistaMiniPlancia -> {
            // 1. Controllo Blocco Evento
            if (evento?.tipo == TipoEffettoEvento.BLOCCO_ACQUISTO_ADDESTRAMENTO) {
                println("LOG: Acquisto mini-plance bloccato dall'evento!")
                return
            }

            // 2. Calcolo costo con eventuale modifica evento
            var costoMiniPlancia = 5
            if (evento?.tipo == TipoEffettoEvento.MODIFICA_COSTO_PLANCIAADDESTRAMENTO) {
                costoMiniPlancia += evento.variazione
            }

            if (giocatore.doin >= costoMiniPlancia) {
                val ok = giocatore.plancia.acquistaMiniPlancia(subAzione.indiceRiga, subAzione.slotSinistro)
                if (ok) {
                    giocatore.doin -= costoMiniPlancia
                    println("LOG: G${giocatore.id} acquista mini-plancia per $costoMiniPlancia doin")
                }
            }
        }
        is AzioneSecondaria.AddestraCane -> {
            val ok = provaAvviareAddestramento(sg.giocatore, subAzione.carta, subAzione.cane)
            if (ok) {
                println("LOG: G${sg.giocatore.id} mette cane in addestramento")
            }
        }
        is AzioneSecondaria.PagaDebito -> {
            if (sg.giocatore.debiti > 0 && sg.giocatore.doin >= 2) {
                sg.giocatore.debiti--
                sg.giocatore.doin -= 2
                println("LOG: G${sg.giocatore.id} paga 1 debito")
            }
        }
        // Altre azioni secondarie...
        else -> println("LOG: Azione secondaria non ancora implementata")
    }
}
