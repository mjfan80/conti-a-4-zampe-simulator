package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.engine.StatoGiocatoreGiornata
import it.contia4zampe.simulator.model.SceltaCucciolo
import it.contia4zampe.simulator.model.StatoCane

/**
 * Questa funzione gestisce l'intero processo di maturazione per un giocatore.
 * Isola la logica complessa dall'Engine principale.
 */
fun applicaMaturazioneCuccioli(statoGiocatore: StatoGiocatoreGiornata, giornataCorrente: Int) {
    val giocatore = statoGiocatore.giocatore
    val profilo = statoGiocatore.profilo

    // Cicliamo su ogni carta nella plancia
    giocatore.plancia.righe.flatten().forEach { carta ->
        
        // 1. Identifichiamo i cuccioli maturi (Giorno Corrente - Giorno Nascita >= 1)
        val maturi = carta.cani.filter { cane ->
            cane.stato == StatoCane.CUCCIOLO &&
            cane.giornataNascita != null &&
            (giornataCorrente - cane.giornataNascita >= 1) 
        }

        // 2. Per ogni cucciolo maturo, chiediamo al giocatore cosa fare
        maturi.forEach { cucciolo ->
            val scelta = profilo.decidiGestioneCucciolo(statoGiocatore, cucciolo)

            when (scelta) {
                SceltaCucciolo.TRASFORMA_IN_ADULTO -> {
                    cucciolo.stato = StatoCane.ADULTO
                    println("LOG: G${giocatore.id} - Cucciolo di ${carta.nome} cresciuto ad Adulto.")
                }
                SceltaCucciolo.VENDI -> {
                    // Calcolo vendita: Costo carta + 3
                    val ricavo = carta.costo + 3
                    giocatore.doin += ricavo
                    carta.cani.remove(cucciolo) // Lo togliamo fisicamente dalla carta
                    println("LOG: G${giocatore.id} - Cucciolo di ${carta.nome} venduto per $ricavo doin.")
                }
            }
        }
    }
}