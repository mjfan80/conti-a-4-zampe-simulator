package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.Giocatore
import it.contia4zampe.simulator.model.StatoCane

/**
 * Gestisce la trasformazione dei cani che hanno finito l'addestramento.
 * Stile imperativo: cicli espliciti per ogni riga e carta.
 */
fun applicaRisoluzioneAddestramento(giocatore: Giocatore) {
    // Navighiamo la plancia riga per riga
    for (riga in giocatore.plancia.righe) {
        // Per ogni riga, guardiamo ogni carta
        for (carta in riga) {
            
            var almenoUnoAddestrato = false
            
            // Per ogni cane sulla carta
            for (cane in carta.cani) {
                // Se il cane era in addestramento, ora ha finito!
                if (cane.stato == StatoCane.IN_ADDESTRAMENTO) {
                    cane.stato = StatoCane.ADULTO_ADDESTRATO
                    println("LOG: Cane ${cane.id} su ${carta.nome} ora è ADDESTRATO.")
                }
                
                // Controlliamo se la carta deve andare in upgrade
                if (cane.stato == StatoCane.ADULTO_ADDESTRATO) {
                    almenoUnoAddestrato = true
                }
            }
            
            // Se troviamo anche solo un cane addestrato, la carta è in upgrade
            if (almenoUnoAddestrato) {
                carta.upgrade = true
            }
        }
    }
}