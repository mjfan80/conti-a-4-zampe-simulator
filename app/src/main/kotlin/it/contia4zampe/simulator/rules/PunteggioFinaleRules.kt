package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.*

fun calcolaPuntiVittoriaBase(giocatore: Giocatore): Int {
    var puntiTotali = 0

    for (riga in giocatore.plancia.righe) {
        for (carta in riga) {
            // 1. Punti dalla Carta (Base o Upgrade)
            puntiTotali += if (carta.upgrade) carta.puntiUpgrade else puntiAtterraggio(carta)

            // 2. Punti standard dai Cani (Adulto +1, Addestrato +2)
            for (cane in carta.cani) {
                when (cane.stato) {
                    StatoCane.ADULTO, 
                    StatoCane.IN_ACCOPPIAMENTO, 
                    StatoCane.IN_ADDESTRAMENTO -> puntiTotali += 1
                    
                    StatoCane.ADULTO_ADDESTRATO -> puntiTotali += 2
                    
                    StatoCane.CUCCIOLO -> {} // I cuccioli non danno punti base
                }
            }

            // 3. Calcolo bonus speciali basati sui cani "fisicamente adulti"
            // Definiamo quali stati contano come "adulto" per i bonus speciali
            var conteggioAdultiFisici = 0
            for (cane in carta.cani) {
                if (cane.stato != StatoCane.CUCCIOLO) {
                    conteggioAdultiFisici++
                }
            }

            when (carta.effettoFine) {
                EffettoFinePartita.BONUS_1PV_ADULTO -> {
                    puntiTotali += conteggioAdultiFisici
                }
                EffettoFinePartita.BONUS_2PV_COPPIA_ADULTI -> {
                    val coppie = conteggioAdultiFisici / 2
                    puntiTotali += (coppie * 2)
                }
                EffettoFinePartita.NESSUNO -> {}
            }
        }
    }

    // 4. Penalità Debiti: -1 punto per ogni debito residuo
    puntiTotali -= giocatore.debiti

    return puntiTotali
}

// Helper per gestire il valore delle carte collassate (se previsto dal regolamento)
private fun puntiAtterraggio(carta: CartaRazza): Int {
    return if (carta.collassata) 1 else carta.puntiBase
}