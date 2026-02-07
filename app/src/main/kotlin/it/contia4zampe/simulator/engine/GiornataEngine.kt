package it.contia4zampe.simulator.engine

import it.contia4zampe.simulator.rules.*
import it.contia4zampe.simulator.player.AzioneGiocatore


class GiornataEngine {

    fun eseguiGiornata(stato: StatoGiornata) {
        inizioGiornata(stato)
        turniGiocatori(stato)
        fineGiornata(stato)
    }

    private fun inizioGiornata(stato: StatoGiornata) {
        stato.fase = FaseGiornata.INIZIO

        stato.giocatori.forEach { statoGiocatore ->
            applicaRenditaNetta(statoGiocatore.giocatore)
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
                    is AzioneGiocatore.Passa -> {
                        passaGiocatore(stato, statoGiocatore)
                        if (stato.inChiusura) {
                            statoGiocatore.haFattoUltimoTurno = true
                        }
                    }

                    is AzioneGiocatore.AzioneFittizia -> {
                        // per ora non fa nulla
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

}
