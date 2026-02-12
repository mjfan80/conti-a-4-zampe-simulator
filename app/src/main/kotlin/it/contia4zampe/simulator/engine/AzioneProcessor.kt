package it.contia4zampe.simulator.engine

import it.contia4zampe.simulator.player.*
import it.contia4zampe.simulator.rules.*

fun eseguiAzione(
    azione: AzioneGiocatore,
    statoGiocatore: StatoGiocatoreGiornata,
    statoGenerale: StatoGiornata
) {
    val giocatore = statoGiocatore.giocatore

    when (azione) {
        is AzioneGiocatore.GiocaCartaRazza -> {
            eseguiGiocaCarta(giocatore, azione.carta, azione.rigaDestinazione, azione.slotDestinazione)
        }

        is AzioneGiocatore.VendiCani -> {
            val lista = azione.vendite.map { it.carta to it.cane }
            val haPescatoExtra = eseguiAzioneVendita(giocatore, lista, azione.pescaCartaInveceDi5Doin)
            
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
                val subAzione = azione.azioni[i]
                eseguiSingolaAzioneSecondaria(subAzione, statoGiocatore)
            }
        }

        is AzioneGiocatore.Passa -> {
            // Logica di chiusura gestita dall'Engine
        }
    }
}

private fun eseguiSingolaAzioneSecondaria(subAzione: AzioneSecondaria, sg: StatoGiocatoreGiornata) {
    when (subAzione) {
        is AzioneSecondaria.AddestraCane -> {
            // Qui chiameremo la regola specifica (da implementare)
            println("LOG: G${sg.giocatore.id} mette cane in addestramento")
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