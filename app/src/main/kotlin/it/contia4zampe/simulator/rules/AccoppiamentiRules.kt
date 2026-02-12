package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.*
import it.contia4zampe.simulator.engine.Dado

fun applicaRisoluzioneAccoppiamenti(giocatore: Giocatore, giornataCorrente: Int, dado: Dado) {
    for (carta in giocatore.plancia.righe.flatten()) {
        val inAccoppiamento = mutableListOf<Cane>()
        for (cane in carta.cani) {
            if (cane.stato == StatoCane.IN_ACCOPPIAMENTO) {
                inAccoppiamento.add(cane)
            }
        }

        if (inAccoppiamento.size == 2) {
            val lancio = dado.lancia()
            val nati = when (lancio) {
                1 -> 0
                in 2..5 -> 1
                6 -> 2
                else -> 0
            }
            
            for (i in 1..nati) {
                carta.cani.add(Cane.crea(StatoCane.CUCCIOLO, giornataCorrente))
            }

            for (cane in inAccoppiamento) {
                cane.stato = cane.statoPrecedente ?: StatoCane.ADULTO
                cane.statoPrecedente = null
            }
        }
    }
}