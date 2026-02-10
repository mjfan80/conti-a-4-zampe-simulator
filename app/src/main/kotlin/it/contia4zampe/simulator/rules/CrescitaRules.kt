package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.Cane
import it.contia4zampe.simulator.model.Giocatore
import it.contia4zampe.simulator.model.StatoCane

const val GIORNI_PER_MATURAZIONE = 1 

fun trovaCuccioliMaturi(giocatore: Giocatore, giornataCorrente: Int): List<Cane> {
    return giocatore.plancia.righe
        .flatten()
        .flatMap { it.cani }
        .filter { cane ->
            cane.stato == StatoCane.CUCCIOLO &&
            cane.giornataNascita != null &&
            (giornataCorrente - cane.giornataNascita >= GIORNI_PER_MATURAZIONE)
        }
}