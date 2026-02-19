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
fun eseguiGiocaCarta(giocatore: Giocatore, carta: CartaRazza, indiceRiga: Int, indiceSlot: Int, evento: CartaEvento? = null): Boolean {
    // 1. Calcolo costo dinamico basato sull'evento
    var costoEffettivo = carta.costo

    when (evento?.tipo) {
        TipoEffettoEvento.MODIFICA_COSTO_RAZZA_TUTTE -> {
            costoEffettivo += evento.variazione
        }
        TipoEffettoEvento.MODIFICA_COSTO_RAZZA_TAGLIA -> {
            if (carta.taglia == evento.tagliaTarget) {
                costoEffettivo += evento.variazione
            } else if (carta.taglia == evento.tagliaTargetSecondaria) {
                costoEffettivo += evento.variazioneSecondaria
            }
        }
        else -> {}
    }
    // Il costo non può mai scendere sotto zero (regola di sicurezza)
    if (costoEffettivo < 0) costoEffettivo = 0
    
    // 2. Controllo disponibilità economica
    if (giocatore.doin < costoEffettivo) {
        println("LOG: G${giocatore.id} non ha abbastanza doin ($costoEffettivo richiesti)")
        return false
    }

    // 3. Controllo vincoli riga/taglia
    if (!puòPiazzareInRiga(giocatore.plancia, carta, indiceRiga)) {
        println("LOG: Taglia ${carta.taglia} non ammessa nella riga $indiceRiga")
        return false
    }

    // 4. Controllo slot libero nella riga
    if (!giocatore.plancia.haSpazioInRiga(indiceRiga)) {
        println("LOG: Riga $indiceRiga piena!")
        return false
    }

    // 5. Esecuzione
    giocatore.doin -= costoEffettivo
    giocatore.mano.remove(carta)
    giocatore.plancia.righe[indiceRiga].add(carta)

    println("LOG: G${giocatore.id} ha giocato ${carta.nome} nella riga $indiceRiga (slot richiesto=$indiceSlot)")
    return true
}
