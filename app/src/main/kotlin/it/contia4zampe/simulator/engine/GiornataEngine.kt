package it.contia4zampe.simulator.engine

import it.contia4zampe.simulator.rules.*
import it.contia4zampe.simulator.player.AzioneGiocatore
import it.contia4zampe.simulator.model.*


class GiornataEngine {

    private var nextCaneId: Int = 1

    private fun generaIdCane(): Int = nextCaneId++

    private val dado: Dado = DadoStandard()


    fun eseguiGiornata(stato: StatoGiornata) {
        inizioGiornata(stato)
        turniGiocatori(stato)
        fineGiornata(stato)
    }

    private fun inizioGiornata(stato: StatoGiornata) {
        stato.fase = FaseGiornata.INIZIO

        stato.giocatori.forEach { statoGiocatore ->
            val giocatore = statoGiocatore.giocatore
            applicaRenditaNetta(giocatore)
            applicaPopolamentoCarteNuove(giocatore)
            risolviAccoppiamenti(giocatore)
        }
    }


    private fun turniGiocatori(stato: StatoGiornata) {
        stato.fase = FaseGiornata.TURNI

        while (true) {
            val giocatoriOpen = stato.giocatori.filter {
                it.statoTurno == StatoTurno.OPEN
            }

            if (giocatoriOpen.isEmpty()) break

            giocatoriOpen.forEach { statoGiocatore ->

                if (stato.inChiusura && statoGiocatore.haFattoUltimoTurno) {
                    return@forEach
                }

                val azione = statoGiocatore.profilo.decidiAzione(stato, statoGiocatore)

                when (azione) {

                    is AzioneGiocatore.GiocaCartaRazza -> {
                        giocaCartaRazza(
                            statoGiocatore.giocatore,
                            azione.carta
                        )
                    }

                    is AzioneGiocatore.Passa,
                    is AzioneGiocatore.AzioneFittizia -> {
                        //al momento nessun altro effetto                    
                    }

                }
                if (azione.chiudeTurno) {
                    passaGiocatore(stato, statoGiocatore)

                    if (stato.inChiusura) {
                        statoGiocatore.haFattoUltimoTurno = true
                    }
                }
            }

            if (stato.inChiusura &&
                stato.giocatori.all {
                    it.statoTurno == StatoTurno.CLOSED || it.haFattoUltimoTurno
                }
            ) {
                break
            }
        }
    }


    private fun fineGiornata(stato: StatoGiornata) {
        stato.fase = FaseGiornata.FINE

        stato.giocatori.forEach { statoGiocatore ->
            applicaUpkeep(statoGiocatore.giocatore)
            applicaCollassoPlancia(statoGiocatore.giocatore)
        }
    }

    private fun passaGiocatore(
    stato: StatoGiornata,
    giocatore: StatoGiocatoreGiornata
    ) {
        if (giocatore.statoTurno == StatoTurno.CLOSED) return

        giocatore.statoTurno = StatoTurno.CLOSED
        stato.passaggi++

        if (stato.passaggi >= stato.sogliaPassaggi) {
            stato.inChiusura = true
        }
    }

    private fun risolviAccoppiamenti(giocatore: Giocatore) {

        giocatore.carteRazza.forEach { carta ->

            val caniInAccoppiamento = carta.cani.filter { it.stato == StatoCane.IN_ACCOPPIAMENTO }

            if (caniInAccoppiamento.size == 2) {

                val lancio = dado.lancia()

                val cuccioliDaCreare = when (lancio) {
                    1 -> 0
                    in 2..5 -> 1
                    6 -> 2
                    else -> 0
                }

                repeat(cuccioliDaCreare) {
                    carta.cani.add(
                        Cane(
                            id = generaIdCane(),
                            stato = StatoCane.CUCCIOLO
                        )
                    )
                }

                caniInAccoppiamento.forEach { cane ->
                    cane.stato = cane.statoPrecedente
                        ?: error("Cane ${cane.id} senza statoPrecedente")
                    cane.statoPrecedente = null
                }
            }
        }
    }

    private fun dichiaraAccoppiamento(
        carta: CartaRazza,
        cane1: Cane,
        cane2: Cane
    ) {
        require(cane1 != cane2)
        require(cane1.stato in listOf(StatoCane.ADULTO, StatoCane.ADULTO_ADDESTRATO))
        require(cane2.stato in listOf(StatoCane.ADULTO, StatoCane.ADULTO_ADDESTRATO))

        require(
            carta.cani.none { it.stato == StatoCane.IN_ACCOPPIAMENTO }
        )

        cane1.statoPrecedente = cane1.stato
        cane2.statoPrecedente = cane2.stato

        cane1.stato = StatoCane.IN_ACCOPPIAMENTO
        cane2.stato = StatoCane.IN_ACCOPPIAMENTO
    }




}


