package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.*

/**
 * Controlla se una carta deve collassare (meno di 2 adulti).
 * Se collassa, vende automaticamente i cani rimanenti e disattiva la carta.
 */
fun gestisciCollassoCarta(giocatore: Giocatore, carta: CartaRazza): Int {
    if (carta.collassata) return 0 // Già collassata, non fare nulla
    
    // Contiamo gli adulti (normali o addestrati)
    var adulti = 0
    for (cane in carta.cani) {
        if (cane.stato == StatoCane.ADULTO || cane.stato == StatoCane.ADULTO_ADDESTRATO) {
            adulti++
        }
    }

    // Se ci sono meno di 2 adulti, la carta collassa
    if (adulti < 2) {
        var ricavoExtra = 0
        println("ATTENZIONE: La carta ${carta.nome} sta collassando!")

        // Vendiamo tutti i cani rimasti sulla carta (anche cuccioli o addestrati)
        // Usiamo un iteratore o una copia della lista per evitare errori durante la rimozione
        val caniRimasti = carta.cani.toList() 
        for (cane in caniRimasti) {
            ricavoExtra += calcolaValoreCane(carta, cane) // Usiamo la funzione di VenditaRules
            carta.cani.remove(cane)
        }

        // Segniamo la carta come collassata
        carta.collassata = true
        giocatore.doin += ricavoExtra
        return ricavoExtra
    }

    return 0
}