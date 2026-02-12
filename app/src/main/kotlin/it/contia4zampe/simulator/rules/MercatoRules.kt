package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.engine.StatoGiornata

fun eseguiDraftMercato(stato: StatoGiornata) {
    val nGiocatori = stato.giocatori.size
    // Ciclo imperativo classico
    for (i in 0 until nGiocatori) {
        val currentIndex = (stato.indicePrimoGiocatore + i) % nGiocatori
        val sg = stato.giocatori[currentIndex]
        
        if (stato.mercatoComune.isNotEmpty()) {
            val scelta = sg.profilo.scegliCartaDalMercato(sg, stato.mercatoComune)
            
            stato.mercatoComune.remove(scelta)
            sg.giocatore.mano.add(scelta)
            
            if (stato.mazzoCarteRazza.isNotEmpty()) {
                val nuova = stato.mazzoCarteRazza.removeAt(0)
                stato.mercatoComune.add(nuova)
            }
        }
    }
}