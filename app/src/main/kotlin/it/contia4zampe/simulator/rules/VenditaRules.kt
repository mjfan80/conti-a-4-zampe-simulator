package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.*

fun calcolaValoreCane(carta: CartaRazza, cane: Cane): Int {
    val valoreBase = carta.costo
    return when (cane.stato) {
        StatoCane.ADULTO -> valoreBase
        StatoCane.ADULTO_ADDESTRATO -> valoreBase * 2
        StatoCane.CUCCIOLO -> valoreBase + 3
        else -> 0
    }
}

/**
 * Esegue la vendita dei cani scelti dal giocatore.
 * Restituisce TRUE se il giocatore ha scelto (e potuto) attivare la pesca carta extra.
 */
fun eseguiAzioneVendita(
    giocatore: Giocatore, 
    pacchettiVendita: List<Pair<CartaRazza, Cane>>, 
    vuolePescareCarta: Boolean,
    evento: CartaEvento? = null // Aggiungiamo l'evento come parametro

): Boolean {
    var totaleRicavato = 0
    val carteCoinvolte = mutableSetOf<CartaRazza>()

    // 1. Vendita dei cani selezionati
    for (coppia in pacchettiVendita) {
        val carta = coppia.first
        val cane = coppia.second

        var valoreCane = calcolaValoreCane(carta, cane)
        
        // APPLICAZIONE EVENTO: Se c'è un bonus vendite (es. +1 per capo)
        if (evento?.tipo == TipoEffettoEvento.MODIFICA_VENDITA) {
            valoreCane += evento.variazione
        }
        
        totaleRicavato += valoreCane
        carta.cani.remove(cane)
        carteCoinvolte.add(carta)
    }

    // 2. Controllo collasso per ogni carta da cui abbiamo tolto cani
    for (carta in carteCoinvolte) {
        // Se la carta collassa, i soldi vengono aggiunti direttamente dentro gestisciCollassoCarta
        gestisciCollassoCarta(giocatore, carta)
    }

    // 3. Gestione scelta facoltativa "5 doin per 1 carta"
    // Nota: totaleRicavato qui include solo i cani venduti VOLONTARIAMENTE.
    // Il regolamento suggerisce che la scelta si basa sul valore della vendita effettuata.
    if (totaleRicavato >= 5 && vuolePescareCarta) {
        giocatore.doin += (totaleRicavato - 5)
        return true // Segnaliamo all'Engine di dare la carta
    } else {
        giocatore.doin += totaleRicavato
        return false
    }
}