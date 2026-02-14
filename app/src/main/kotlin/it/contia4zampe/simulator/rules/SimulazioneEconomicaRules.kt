package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.engine.Dado
import it.contia4zampe.simulator.engine.StatoGiocatoreGiornata
import it.contia4zampe.simulator.model.*

private class DadoValoreAtteso : Dado {
    // 2..5 => 1 cucciolo, usiamo sempre 4 per rappresentare il caso atteso.
    override fun lancia(): Int = 4
}

data class EsitoSimulazioneEconomica(
    val doinFinali: Int,
    val debitiFinali: Int
)

fun stimaEconomiaDueGiornateConAccoppiamento(
    statoGiocatore: StatoGiocatoreGiornata,
    cartaTarget: CartaRazza,
    dichiaraAccoppiamento: Boolean
): EsitoSimulazioneEconomica {
    val copiaGiocatore = clonaGiocatore(statoGiocatore.giocatore)
    val tutteCarteOriginali = statoGiocatore.giocatore.plancia.righe.flatten()
    val indiceCarta = tutteCarteOriginali.indexOf(cartaTarget)

    if (dichiaraAccoppiamento && indiceCarta >= 0) {
        val cartaCopia = copiaGiocatore.plancia.righe.flatten()[indiceCarta]
        provaDichiarareAccoppiamento(cartaCopia)
    }

    // Fine giornata corrente: upkeep
    applicaUpkeep(copiaGiocatore)

    // Simulazione di 2 giornate future senza ulteriori azioni volontarie
    val dadoAtteso = DadoValoreAtteso()
    var giornataVirtuale = 1
    repeat(2) {
        applicaRenditaNetta(copiaGiocatore)
        applicaPopolamentoCarteNuove(copiaGiocatore)
        applicaMaturazioneCuccioliConservativa(copiaGiocatore, giornataVirtuale)
        applicaRisoluzioneAccoppiamenti(copiaGiocatore, giornataVirtuale, dadoAtteso)
        applicaRisoluzioneAddestramento(copiaGiocatore)
        applicaEffettiInizioGiornata(copiaGiocatore)
        applicaUpkeep(copiaGiocatore)
        giornataVirtuale++
    }

    return EsitoSimulazioneEconomica(
        doinFinali = copiaGiocatore.doin,
        debitiFinali = copiaGiocatore.debiti
    )
}

private fun applicaMaturazioneCuccioliConservativa(giocatore: Giocatore, giornataCorrente: Int) {
    for (carta in giocatore.plancia.righe.flatten()) {
        val maturi = carta.cani.filter { cane ->
            cane.stato == StatoCane.CUCCIOLO &&
                cane.giornataNascita != null &&
                (giornataCorrente - cane.giornataNascita >= 1)
        }

        for (cucciolo in maturi) {
            // Strategia conservativa nella simulazione: vendita immediata del cucciolo maturo
            giocatore.doin += (carta.costo + 3)
            carta.cani.remove(cucciolo)
        }
    }
}

private fun clonaGiocatore(originale: Giocatore): Giocatore {
    val righeCopia = originale.plancia.righe.map { rigaOriginale ->
        val rigaNuova = mutableListOf<CartaRazza>()
        for (carta in rigaOriginale) {
            val caniCopia = carta.cani.map { cane ->
                Cane(
                    id = cane.id,
                    stato = cane.stato,
                    statoPrecedente = cane.statoPrecedente,
                    giornataNascita = cane.giornataNascita
                )
            }.toMutableList()

            rigaNuova.add(
                CartaRazza(
                    nome = carta.nome,
                    costo = carta.costo,
                    rendita = carta.rendita,
                    puntiBase = carta.puntiBase,
                    puntiUpgrade = carta.puntiUpgrade,
                    taglia = carta.taglia,
                    cani = caniCopia,
                    upgrade = carta.upgrade,
                    collassata = carta.collassata,
                    effettoInizio = carta.effettoInizio
                )
            )
        }
        rigaNuova
    }

    return Giocatore(
        id = originale.id,
        doin = originale.doin,
        debiti = originale.debiti,
        plancia = PlanciaGiocatore(righeCopia),
        mano = originale.mano.toMutableList()
    )
}
