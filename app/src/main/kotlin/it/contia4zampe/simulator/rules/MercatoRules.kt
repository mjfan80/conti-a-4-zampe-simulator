package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.engine.StatoGiornata

fun eseguiDraftMercato(stato: StatoGiornata) {
    val evento = stato.eventoAttivo
    val nGiocatori = stato.giocatori.size

    for (i in 0 until nGiocatori) {
        val currentIndex = (stato.indicePrimoGiocatore + i) % nGiocatori
        val sg = stato.giocatori[currentIndex]
        
        if (stato.mercatoComune.isNotEmpty()) {
            val scelta = sg.profilo.scegliCartaDalMercato(sg, stato.mercatoComune)
            
            stato.mercatoComune.remove(scelta)
            sg.giocatore.mano.add(scelta)
            
            // --- LOGICA PENALITÀ EVENTO ---
            if (evento?.penalitàScartoMercato == true && sg.giocatore.mano.size>1) { // Evitiamo di scartare ha solo la carta pescata in mano    
                // Il giocatore deve scartare una carta (per ora la prima disponibile, o chiediamo al profilo)
                val cartaDaScartare = sg.giocatore.mano.first() 
                sg.giocatore.mano.remove(cartaDaScartare)
                println("LOG: G${sg.giocatore.id} scarta ${cartaDaScartare.nome} per effetto evento.")
            }
            // ------------------------------

            if (stato.mazzoCarteRazza.isNotEmpty()) {
                stato.mercatoComune.add(stato.mazzoCarteRazza.removeAt(0))
            }
        }
    }
}