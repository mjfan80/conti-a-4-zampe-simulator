package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.Giocatore
import kotlin.math.max

fun applicaRiduzioneDaDebito(
    renditaBase: Int,
    giocatore: Giocatore
): Int {
    val riduzione = giocatore.debiti / 3
    return max(0, renditaBase - riduzione)
}
