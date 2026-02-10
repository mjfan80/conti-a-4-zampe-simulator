package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.*

/**
 * Risolve gli effetti speciali delle carte attive sulla plancia.
 */
fun applicaEffettiInizioGiornata(giocatore: Giocatore) {
    // Scorriamo ogni riga della plancia
    for (i in 0 until giocatore.plancia.righe.size) {
        val riga = giocatore.plancia.righe[i]
        
        // Scorriamo ogni carta nella riga
        for (carta in riga) {
            
            // Eseguiamo l'effetto in base al tipo dichiarato dalla carta
            when (carta.effettoInizio) {
                
                EffettoInizioGiornata.DOIN1_RENDITA -> {
                    giocatore.doin += 1
                    println("LOG: + 1 doin nella rendita attivato per G${giocatore.id}: +1 doin")
                }
                
                EffettoInizioGiornata.DOIN1_RENDITA_UNICORIGA -> {
                    // Logica specifica: +1 doin se è l'unica carta attiva nella riga
                    if (riga.size == 1) {
                        giocatore.doin += 1
                        println("LOG: Effetto+1 doin se unico in riga attivato per G${giocatore.id}: +1 doin")
                    }
                }
                
                EffettoInizioGiornata.NESSUNO -> { /* Non fa nulla */ }
            }
        }
    }
}