package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.*

fun giocaCartaRazza(
    giocatore: Giocatore,
    carta: CartaRazza
) {
    if (giocatore.doin < carta.costo) return

    giocatore.doin -= carta.costo
    giocatore.mano.remove(carta)

    carta.cani.clear()

    giocatore.plancia.righe.first().add(carta)
}



