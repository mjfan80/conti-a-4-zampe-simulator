package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.*

/**
 * Verifica se una carta può essere piazzata in una determinata riga.
 */
fun puòPiazzareInRiga(plancia: PlanciaGiocatore, carta: CartaRazza, indiceRiga: Int): Boolean {
    return plancia.puoOspitareTaglia(indiceRiga, carta.taglia)
}

/**
 * Logica per giocare la carta.
 * Restituisce true se l'operazione è riuscita.
 */
fun eseguiGiocaCarta(giocatore: Giocatore, carta: CartaRazza, indiceRiga: Int, indiceSlot: Int): Boolean {
    // 1. Controllo disponibilità economica
    if (giocatore.doin < carta.costo) {
        println("LOG: G${giocatore.id} non ha abbastanza doin per ${carta.nome}")
        return false
    }

    // 2. Controllo vincoli riga/taglia
    if (!puòPiazzareInRiga(giocatore.plancia, carta, indiceRiga)) {
        println("LOG: Taglia ${carta.taglia} non ammessa nella riga $indiceRiga")
        return false
    }

    // 3. Controllo slot libero nella riga
    if (!giocatore.plancia.haSpazioInRiga(indiceRiga)) {
        println("LOG: Riga $indiceRiga piena!")
        return false
    }

    // 4. Esecuzione
    giocatore.doin -= carta.costo
    giocatore.mano.remove(carta)
    giocatore.plancia.righe[indiceRiga].add(carta)

    println("LOG: G${giocatore.id} ha giocato ${carta.nome} nella riga $indiceRiga (slot richiesto=$indiceSlot)")
    return true
}
