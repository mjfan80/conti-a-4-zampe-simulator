package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.Cane
import it.contia4zampe.simulator.model.CartaRazza
import it.contia4zampe.simulator.model.Giocatore
import it.contia4zampe.simulator.model.StatoCane

private const val COSTO_ADDESTRAMENTO = 2

/**
 * Avvia l'addestramento di un cane adulto.
 * Regole minime:
 * - il cane deve essere ADULTO
 * - la carta deve avere almeno 3 cani adulti (ADULTO o ADULTO_ADDESTRATO)
 * - il giocatore deve pagare 2 doin
 */
fun provaAvviareAddestramento(giocatore: Giocatore, carta: CartaRazza, cane: Cane): Boolean {
    if (giocatore.doin < COSTO_ADDESTRAMENTO) return false
    if (!carta.cani.contains(cane)) return false
    if (cane.stato != StatoCane.ADULTO) return false
    if (!giocatore.plancia.haSlotAddestramentoDisponibilePerCarta(carta)) return false

    val adultiPresenti = carta.cani.count {
        it.stato == StatoCane.ADULTO || it.stato == StatoCane.ADULTO_ADDESTRATO
    }

    if (adultiPresenti < 3) return false

    if (!giocatore.plancia.occupaSlotAddestramentoPerCarta(carta)) return false

    giocatore.doin -= COSTO_ADDESTRAMENTO
    cane.statoPrecedente = StatoCane.ADULTO
    cane.stato = StatoCane.IN_ADDESTRAMENTO
    return true
}

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
                    giocatore.plancia.liberaUnoSlotAddestramentoPerCarta(carta)
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
