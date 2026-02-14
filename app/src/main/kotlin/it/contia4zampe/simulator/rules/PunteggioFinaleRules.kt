package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.Giocatore
import it.contia4zampe.simulator.model.StatoCane

fun calcolaPuntiVittoriaBase(giocatore: Giocatore): Int {
    var punti = 0

    for (carta in giocatore.plancia.righe.flatten()) {
        punti += if (carta.upgrade) carta.puntiUpgrade else carta.puntiBase

        for (cane in carta.cani) {
            punti += when (cane.stato) {
                StatoCane.ADULTO -> 1
                StatoCane.ADULTO_ADDESTRATO -> 2
                else -> 0
            }
        }
    }

    // Debiti a fine partita: -1 punto ciascuno
    punti -= giocatore.debiti

    return punti
}
