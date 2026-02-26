package it.contia4zampe.simulator.engine

import it.contia4zampe.simulator.player.*
import it.contia4zampe.simulator.rules.*
import it.contia4zampe.simulator.model.CartaEvento
import it.contia4zampe.simulator.model.TipoEffettoEvento

fun eseguiAzione(
    azione: AzioneGiocatore,
    statoGiocatore: StatoGiocatoreGiornata,
    statoGenerale: StatoGiornata,
    collector: SimulationCollector = NoOpSimulationCollector, // Aggiungiamo il collector
    gameId: Int = 0
) {
    val giocatore = statoGiocatore.giocatore
    val evento = statoGenerale.eventoAttivo

    when (azione) {
        is AzioneGiocatore.GiocaCartaRazza -> {
            eseguiGiocaCarta(giocatore, azione.carta, azione.rigaDestinazione, azione.slotDestinazione, evento)
        }

        is AzioneGiocatore.VendiCani -> {
            val lista = azione.vendite.map { it.carta to it.cane }
            val haPescatoExtra = eseguiAzioneVendita(giocatore, lista, azione.pescaCartaInveceDi5Doin, evento)
            if (haPescatoExtra && statoGenerale.mazzoCarteRazza.isNotEmpty()) {
                giocatore.mano.add(statoGenerale.mazzoCarteRazza.removeAt(0))
            }
        }

        is AzioneGiocatore.BloccoAzioniSecondarie -> {
            for (subAzione in azione.azioni.take(2)) {
                // REGISTRAZIONE NEL REPORT
                collector.onDecisionTaken(
                    DecisionEvent(
                        gameId = gameId,
                        dayNumber = statoGenerale.numero,
                        playerId = giocatore.id,
                        profileName = statoGiocatore.profilo::class.simpleName ?: "Unknown",
                        actionType = "SECONDARIA",
                        actionName = subAzione::class.simpleName ?: "UnknownSubAction"
                    )
                )
                eseguiSingolaAzioneSecondaria(subAzione, statoGiocatore, evento)
            }
        }
        is AzioneGiocatore.Passa -> {}
    }
}

private fun eseguiSingolaAzioneSecondaria(subAzione: AzioneSecondaria, sg: StatoGiocatoreGiornata, evento: CartaEvento?) {
    val giocatore = sg.giocatore
    when (subAzione) {
        is AzioneSecondaria.AcquistaMiniPlancia -> {
            if (evento?.tipo == TipoEffettoEvento.BLOCCO_ACQUISTO_ADDESTRAMENTO) return
            var costo = 5
            if (evento?.tipo == TipoEffettoEvento.MODIFICA_COSTO_PLANCIAADDESTRAMENTO) costo += evento.variazione
            if (giocatore.doin >= costo) {
                if (giocatore.plancia.acquistaMiniPlancia(subAzione.indiceRiga, subAzione.slotSinistro)) {
                    giocatore.doin -= costo
                }
            }
        }
        is AzioneSecondaria.AddestraCane -> provaAvviareAddestramento(giocatore, subAzione.carta, subAzione.cane)
        is AzioneSecondaria.PagaDebito -> {
            if (giocatore.debiti > 0 && giocatore.doin >= 2) {
                giocatore.debiti--
                giocatore.doin -= 2
            }
        }
        else -> {}
    }
}