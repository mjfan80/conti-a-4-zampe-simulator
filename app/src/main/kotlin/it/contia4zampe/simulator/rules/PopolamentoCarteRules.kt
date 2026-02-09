package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.*

fun applicaPopolamentoCarteNuove(giocatore: Giocatore) {
    giocatore.plancia.righe
        .flatten()
        .filter { carta -> carta.cani.isEmpty() }
        .forEach { carta ->
            carta.cani.add(Cane(StatoCane.ADULTO))
            carta.cani.add(Cane(StatoCane.ADULTO))
        }
}
