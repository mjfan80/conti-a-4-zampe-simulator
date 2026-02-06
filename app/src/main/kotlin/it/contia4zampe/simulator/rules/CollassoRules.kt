package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.*

fun deveCollassare(carta: CartaRazza): Boolean {
    val adulti = carta.cani.count {
        it.stato == StatoCane.ADULTO || it.stato == StatoCane.ADULTO_ADDESTRATO
    }

    return adulti < 2
}

fun applicaCollasso(carta: CartaRazza) {
    if (carta.collassata) return

    if (deveCollassare(carta)) {
        carta.collassata = true
    }
}

fun applicaCollassoPlancia(giocatore: Giocatore) {
    giocatore.plancia.righe
        .flatten()
        .forEach { carta ->
            applicaCollasso(carta)
        }
}
