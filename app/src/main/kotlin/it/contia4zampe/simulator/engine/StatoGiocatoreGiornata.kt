package it.contia4zampe.simulator.engine

import it.contia4zampe.simulator.model.Giocatore
import it.contia4zampe.simulator.player.PlayerProfile

data class StatoGiocatoreGiornata(
    val giocatore: Giocatore,
    val profilo: PlayerProfile,
    var statoTurno: StatoTurno = StatoTurno.OPEN,
    var haFattoUltimoTurno: Boolean = false
) { // <--- Apriamo le graffe per aggiungere i metodi

    /**
     * Calcola l'upkeep basato sui cani FISICAMENTE presenti ora sulla plancia.
     */
    fun calcolaUpkeepAttuale(): Int {
        var totaleCani = 0
        // Ciclo for classico sulle righe
        for (riga in giocatore.plancia.righe) {
            // Ciclo for sulle carte di ogni riga
            for (carta in riga) {
                totaleCani += carta.cani.size
            }
        }
        return totaleCani
    }

    /**
     * Calcola l'upkeep "di domani mattina".
     * Somma i cani attuali + i 2 cani che nasceranno in ogni carta attiva ma vuota.
     */
    fun calcolaUpkeepFuturo(): Int {
        var stimaCani = calcolaUpkeepAttuale()
        
        for (riga in giocatore.plancia.righe) {
            for (carta in riga) {
                // Se la carta è attiva (non collassata) ed è vuota, 
                // sappiamo che domani riceverà 2 cani.
                if (!carta.collassata && carta.cani.isEmpty()) {
                    stimaCani += 2
                }
            }
        }
        return stimaCani
    }
}