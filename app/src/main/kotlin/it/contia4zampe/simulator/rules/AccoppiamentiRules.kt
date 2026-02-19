package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.*
import it.contia4zampe.simulator.engine.Dado
import it.contia4zampe.simulator.engine.StatoGiocatoreGiornata

fun applicaDichiarazioneAccoppiamenti(statoGiocatore: StatoGiocatoreGiornata, evento: CartaEvento? = null) {
    // CONTROLLO EVENTO: Se l'evento blocca gli accoppiamenti, usciamo subito
    if (evento?.tipo == TipoEffettoEvento.BLOCCO_ACCOPPIAMENTO) {
        println("LOG: Accoppiamenti bloccati dall'evento ${evento.nome}")
        return
    }

    val giocatore = statoGiocatore.giocatore
    val profilo = statoGiocatore.profilo

    for (carta in giocatore.plancia.righe.flatten()) {
        if (profilo.vuoleDichiarareAccoppiamento(statoGiocatore, carta)) {
            provaDichiarareAccoppiamento(carta)
        }
    }
}

fun provaDichiarareAccoppiamento(carta: CartaRazza): Boolean {
    // Una carta può avere un solo accoppiamento alla volta
    val giaInAccoppiamento = carta.cani.count { it.stato == StatoCane.IN_ACCOPPIAMENTO }
    if (giaInAccoppiamento > 0) {
        return false
    }

    val candidati = mutableListOf<Cane>()
    for (cane in carta.cani) {
        if (cane.stato == StatoCane.ADULTO || cane.stato == StatoCane.ADULTO_ADDESTRATO) {
            candidati.add(cane)
        }

        if (candidati.size == 2) {
            break
        }
    }

    if (candidati.size < 2) {
        return false
    }

    for (cane in candidati) {
        cane.statoPrecedente = cane.stato
        cane.stato = StatoCane.IN_ACCOPPIAMENTO
    }

    return true
}

fun applicaRisoluzioneAccoppiamenti(giocatore: Giocatore, giornataCorrente: Int, dado: Dado) {
    for (carta in giocatore.plancia.righe.flatten()) {
        val inAccoppiamento = mutableListOf<Cane>()
        for (cane in carta.cani) {
            if (cane.stato == StatoCane.IN_ACCOPPIAMENTO) {
                inAccoppiamento.add(cane)
            }
        }

        if (inAccoppiamento.size == 2) {
            val lancio = dado.lancia()
            val nati = when (lancio) {
                1 -> 0
                in 2..5 -> 1
                6 -> 2
                else -> 0
            }
            
            for (i in 1..nati) {
                carta.cani.add(Cane.crea(StatoCane.CUCCIOLO, giornataCorrente))
            }

            for (cane in inAccoppiamento) {
                cane.stato = cane.statoPrecedente ?: StatoCane.ADULTO
                cane.statoPrecedente = null
            }
        }
    }
}
