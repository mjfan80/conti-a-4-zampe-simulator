package it.contia4zampe.simulator.engine

import it.contia4zampe.simulator.rules.*
import it.contia4zampe.simulator.model.*

class GiornataEngine {

    private val dado: Dado = DadoStandard()

    fun eseguiGiornata(stato: StatoGiornata) {
        inizioGiornata(stato)
        faseTurni(stato)
        fineGiornata(stato)
    }

    private fun inizioGiornata(stato: StatoGiornata) {
        stato.fase = FaseGiornata.INIZIO

        for (sg in stato.giocatori) {
            val g = sg.giocatore
            applicaRenditaNetta(g)
            applicaPopolamentoCarteNuove(g)
            applicaMaturazioneCuccioli(sg, stato.numero)
            applicaRisoluzioneAccoppiamenti(g, stato.numero, dado)
            applicaRisoluzioneAddestramento(g)
            applicaEffettiInizioGiornata(g)
        }

        // Reset stati e Draft
        preparaGiocatoriPerTurni(stato)
        eseguiDraftMercato(stato)
    }

    private fun faseTurni(stato: StatoGiornata) {
        stato.fase = FaseGiornata.TURNI

        while (true) {
            val giocatoriOpen = stato.giocatori.filter { it.statoTurno == StatoTurno.OPEN }
            if (giocatoriOpen.isEmpty()) break

            for (sg in giocatoriOpen) {
                if (stato.inChiusura && sg.haFattoUltimoTurno) continue

                // 1. Il profilo decide (Principale, Blocco Secondarie o Passa)
                val scelta = sg.profilo.decidiAzione(stato, sg)

                // 2. Esecuzione
                eseguiAzione(scelta, sg, stato)

                // 3. Gestione fine turno
                // Il turno finisce SEMPRE dopo un'azione, a meno che non sia un'azione 
                // speciale che non consuma il turno (ma nel nostro gioco non esistono).
                
                // Se l'azione ha il flag chiudeTurno (Passa o Vendita con Pesca), 
                // il giocatore diventa CLOSED per il resto della giornata.
                if (scelta.chiudeTurno) {
                    passaGiocatore(stato, sg)
                }

                // Se siamo in chiusura e il giocatore ha appena agito, ha consumato il suo ultimo turno
                if (stato.inChiusura) {
                    sg.haFattoUltimoTurno = true
                }
            }

            if (stato.inChiusura && stato.giocatori.all { it.statoTurno == StatoTurno.CLOSED || it.haFattoUltimoTurno }) {
                break
            }
        }
    }

    private fun fineGiornata(stato: StatoGiornata) {
        stato.fase = FaseGiornata.FINE
        for (sg in stato.giocatori) {
            applicaUpkeep(sg.giocatore)
        }
    }

    private fun preparaGiocatoriPerTurni(stato: StatoGiornata) {
        for (sg in stato.giocatori) {
            sg.statoTurno = StatoTurno.OPEN
            sg.haFattoUltimoTurno = false
        }
    }

    /**
     * Gestisce il passaggio di un giocatore allo stato CLOSED.
     * Incrementa il contatore dei passaggi e verifica se attivare la fase di chiusura.
     */
    private fun passaGiocatore(stato: StatoGiornata, statoGiocatore: StatoGiocatoreGiornata) {
        // Se è già chiuso, non facciamo nulla (evitiamo doppi conteggi)
        if (statoGiocatore.statoTurno == StatoTurno.CLOSED) return

        // 1. Cambiamo lo stato del giocatore
        statoGiocatore.statoTurno = StatoTurno.CLOSED
        
        // 2. Incrementiamo i passaggi totali della giornata
        stato.passaggi++

        // 3. Verifichiamo se abbiamo raggiunto la soglia per la chiusura
        // (La soglia è definita nel regolamento: 2 giocatori = 1 pass, 3-4 = 2 pass, ecc.)
        if (stato.passaggi >= stato.sogliaPassaggi) {
            if (!stato.inChiusura) {
                stato.inChiusura = true
                println("LOG: Soglia passaggi raggiunta. La Giornata entra in CHIUSURA.")
            }
        }
    }
}