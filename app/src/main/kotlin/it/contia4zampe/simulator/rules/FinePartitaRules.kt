package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.engine.StatoGiornata

fun condizioneMazzoEventiEsaurito(stato: StatoGiornata): Boolean {
    return stato.numero >= stato.maxGiornateEvento
}

fun condizionePlanciaPiena(stato: StatoGiornata): Boolean {
    return stato.giocatori.any { it.giocatore.plancia.isPiena() }
}

fun deveTerminarePartita(stato: StatoGiornata): Boolean {
    return condizioneMazzoEventiEsaurito(stato) || condizionePlanciaPiena(stato)
}
