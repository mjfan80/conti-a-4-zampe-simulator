package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.*

/**
 * Verifica se una carta può essere piazzata in una determinata riga.
 */
fun puòPiazzareInRiga(carta: CartaRazza, indiceRiga: Int): Boolean {
    // Vincoli di taglia (Esempio basato su logica eurogame standard)
    return when (indiceRiga) {
        0 -> carta.taglia == Taglia.PICCOLA || carta.taglia == Taglia.MEDIA
        1 -> true // La riga centrale di solito accetta tutto
        2 -> carta.taglia == Taglia.MEDIA || carta.taglia == Taglia.GRANDE
        else -> false
    }
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

    // 2. Controllo vincoli riga
    if (!puòPiazzareInRiga(carta, indiceRiga)) {
        println("LOG: Taglia ${carta.taglia} non ammessa nella riga $indiceRiga")
        return false
    }

    // 3. Controllo slot libero
    val riga = giocatore.plancia.righe[indiceRiga]
    // Se lo slot è già occupato (non nullo o lista non vuota in base a come gestiamo la lista)
    // Assumiamo che la riga sia una MutableList di carte. 
    // Se la riga ha una dimensione massima (es. 4 slot), dobbiamo controllare.
    if (riga.size >= 4) { 
        println("LOG: Riga $indiceRiga piena!")
        return false
    }

    // 4. Esecuzione
    giocatore.doin -= carta.costo
    giocatore.mano.remove(carta)
    riga.add(carta)
    
    println("LOG: G${giocatore.id} ha giocato ${carta.nome} nella riga $indiceRiga")
    return true
}