package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.*

fun calcolaRendita(giocatore: Giocatore): Int {
    return giocatore.plancia.righe
        .flatten()
        .flatMap { it.cani }
        .sumOf { cane ->
            when (cane.stato) {
                StatoCane.ADULTO -> 1
                StatoCane.ADULTO_ADDESTRATO -> 2
                else -> 0
            }
        }
}

fun applicaRendita(giocatore: Giocatore) {
    val rendita = calcolaRendita(giocatore)
    giocatore.doin += rendita
}
