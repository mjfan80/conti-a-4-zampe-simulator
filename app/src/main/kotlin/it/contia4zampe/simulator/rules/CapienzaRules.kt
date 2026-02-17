package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.*

/**
 * Restituisce il limite massimo di cani adulti per una carta in base alla taglia (Regolamento Cap. 3).
 */
fun getLimiteCapienza(taglia: Taglia): Int {
    return when (taglia) {
        Taglia.GRANDE -> 3
        Taglia.MEDIA -> 4
        Taglia.PICCOLA -> 5
    }
}

/**
 * Calcola quanti cani occupano effettivamente spazio.
 * ECCEZIONE: Il primo cane adulto addestrato non viene conteggiato (Regolamento Cap. 3).
 */
fun calcolaOccupazioneEffettiva(carta: CartaRazza): Int {
    var conteggio = 0
    var primoAddestratoTrovato = false

    for (cane in carta.cani) {
        // Solo i cani adulti (normali o addestrati) occupano slot. I cuccioli no.
        if (cane.stato == StatoCane.ADULTO || cane.stato == StatoCane.ADULTO_ADDESTRATO || cane.stato == StatoCane.IN_ACCOPPIAMENTO || cane.stato == StatoCane.IN_ADDESTRAMENTO     ) {
            if (cane.stato == StatoCane.ADULTO_ADDESTRATO && !primoAddestratoTrovato) {
                primoAddestratoTrovato = true // Il primo è gratis
            } else {
                conteggio++
            }
        }
    }
    return conteggio
}

/**
 * Verifica ogni carta del giocatore. Se supera la capienza, vende i cani in eccesso.
 * Stile imperativo con cicli for espliciti.
 */
fun applicaControlloCapienza(giocatore: Giocatore) {
    for (riga in giocatore.plancia.righe) {
        for (carta in riga) {
            if (carta.collassata) continue

            val limite = getLimiteCapienza(carta.taglia)
            
            // Finché la carta è sovraffollata, vendiamo un cane
            while (calcolaOccupazioneEffettiva(carta) > limite) {
                var caneDaVendere: Cane? = null
                
                // Strategia di vendita automatica: cerchiamo prima un adulto non addestrato (vale meno)
                for (cane in carta.cani) {
                    if (cane.stato == StatoCane.ADULTO) {
                        caneDaVendere = cane
                        break
                    }
                }
                
                // Se non ci sono adulti semplici, dobbiamo vendere un addestrato
                if (caneDaVendere == null) {
                    for (cane in carta.cani) {
                        if (cane.stato == StatoCane.ADULTO_ADDESTRATO) {
                            caneDaVendere = cane
                            break
                        }
                    }
                }

                if (caneDaVendere != null) {
                    val ricavo = calcolaValoreCane(carta, caneDaVendere)
                    giocatore.doin += ricavo
                    carta.cani.remove(caneDaVendere)
                    println("LOG: Capienza superata su ${carta.nome} (G${giocatore.id}). Venduto cane per $ricavo doin.")
                } else {
                    break 
                }
            }
        }
    }
}