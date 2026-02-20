package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.*

fun applicaPopolamentoCarteNuove(
    giocatore: Giocatore) {
    giocatore.plancia.righe
        .flatten()
        .filter { carta -> carta.cani.isEmpty() && !carta.collassata}
        .forEach { carta ->
            carta.cani.add(Cane.crea(StatoCane.ADULTO))
            carta.cani.add(Cane.crea(StatoCane.ADULTO))
            carta.appenaGiocata = false 
        }
}
