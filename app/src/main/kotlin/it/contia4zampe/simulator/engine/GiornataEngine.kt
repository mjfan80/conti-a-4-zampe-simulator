package it.contia4zampe.simulator.engine

import it.contia4zampe.simulator.rules.*
import it.contia4zampe.simulator.player.AzioneGiocatore
import it.contia4zampe.simulator.model.*

class GiornataEngine {

    private val dado: Dado = DadoStandard()

    fun eseguiGiornata(stato: StatoGiornata) {
        inizioGiornata(stato)
        turniGiocatori(stato)
        fineGiornata(stato)
    }

    private fun inizioGiornata(stato: StatoGiornata) {
        stato.fase = FaseGiornata.INIZIO

        // 1. Rendita & 2. Popolamento
        for (sg in stato.giocatori) {
            applicaRenditaNetta(sg.giocatore)
            applicaPopolamentoCarteNuove(sg.giocatore)
        }

        // 3. Gestione dei cuccioli maturi
        for (sg in stato.giocatori) {
            applicaMaturazioneCuccioli(sg, stato.numero)
        }

        // 4. Risoluzione degli accoppiamenti
        for (sg in stato.giocatori) {
            risolviAccoppiamenti(sg.giocatore, stato.numero)
        }

        // 5. Risoluzione addestramento
        for (sg in stato.giocatori) {
            applicaRisoluzioneAddestramento(sg.giocatore)
        }

        // 6. Effetti "a inizio Giornata" (Usa la Rule esterna)
        for (sg in stato.giocatori) {
            applicaEffettiInizioGiornata(sg.giocatore)
        }

        // 7. Preparazione dei giocatori (Reset OPEN)
        preparaGiocatori(stato)

        // 8. Pesca dal mercato comune
        eseguiDraftMercato(stato)
    }

    private fun turniGiocatori(stato: StatoGiornata) {
        stato.fase = FaseGiornata.TURNI

        while (true) {
            val giocatoriOpen = stato.giocatori.filter { it.statoTurno == StatoTurno.OPEN }
            if (giocatoriOpen.isEmpty()) break

            for (statoGiocatore in giocatoriOpen) {
                if (stato.inChiusura && statoGiocatore.haFattoUltimoTurno) {
                    continue
                }

                val azione = statoGiocatore.profilo.decidiAzione(stato, statoGiocatore)

                // LOGICA CORRETTA DEL WHEN
                when (azione) {
                    is AzioneGiocatore.GiocaCartaRazza -> {
                        giocaCartaRazza(statoGiocatore.giocatore, azione.carta)
                    }

                    is AzioneGiocatore.VendiCani -> {
                        // Ora che è isolato, Kotlin capisce che azione è VendiCani!
                        val lista = azione.vendite.map { it.carta to it.cane }
                        
                        val haPescatoExtra = eseguiAzioneVendita(
                            statoGiocatore.giocatore, 
                            lista, 
                            azione.pescaCartaInveceDi5Doin
                        )
                        
                        if (haPescatoExtra && stato.mazzoCarteRazza.isNotEmpty()) {
                            val nuova = stato.mazzoCarteRazza.removeAt(0)
                            statoGiocatore.giocatore.mano.add(nuova)
                            println("LOG: G${statoGiocatore.giocatore.id} vende e pesca extra.")
                        }
                    }

                    is AzioneGiocatore.Passa -> {
                        // Non fa nulla di specifico, chiudeTurno farà il resto
                    }
                }

                if (azione.chiudeTurno) {
                    passaGiocatore(stato, statoGiocatore)
                    if (stato.inChiusura) {
                        statoGiocatore.haFattoUltimoTurno = true
                    }
                }
            }

            // Verifica uscita dal loop turni
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

    private fun preparaGiocatori(stato: StatoGiornata) {
        for (sg in stato.giocatori) {
            sg.statoTurno = StatoTurno.OPEN
            sg.haFattoUltimoTurno = false
        }
        println("LOG: Turno del Primo Giocatore indice ${stato.indicePrimoGiocatore}")
    }

    private fun eseguiDraftMercato(stato: StatoGiornata) {
        val nGiocatori = stato.giocatori.size
        for (i in 0 until nGiocatori) {
            val currentIndex = (stato.indicePrimoGiocatore + i) % nGiocatori
            val sg = stato.giocatori[currentIndex]
            
            if (stato.mercatoComune.isNotEmpty()) {
                val scelta = sg.profilo.scegliCartaDalMercato(sg, stato.mercatoComune)
                stato.mercatoComune.remove(scelta)
                sg.giocatore.mano.add(scelta)
                
                if (stato.mazzoCarteRazza.isNotEmpty()) {
                    val nuova = stato.mazzoCarteRazza.removeAt(0)
                    stato.mercatoComune.add(nuova)
                }
            }
        }
    }

    private fun passaGiocatore(stato: StatoGiornata, giocatore: StatoGiocatoreGiornata) {
        if (giocatore.statoTurno == StatoTurno.CLOSED) return
        giocatore.statoTurno = StatoTurno.CLOSED
        stato.passaggi++
        if (stato.passaggi >= stato.sogliaPassaggi) {
            stato.inChiusura = true
        }
    }

    private fun risolviAccoppiamenti(giocatore: Giocatore, giornataCorrente: Int) {
        for (carta in giocatore.plancia.righe.flatten()) {
            val inAccoppiamento = carta.cani.filter { it.stato == StatoCane.IN_ACCOPPIAMENTO }
            if (inAccoppiamento.size == 2) {
                val lancio = dado.lancia()
                val nati = when (lancio) {
                    1 -> 0
                    in 2..5 -> 1
                    6 -> 2
                    else -> 0
                }
                repeat(nati) {
                    carta.cani.add(Cane.crea(StatoCane.CUCCIOLO, giornataCorrente))
                }
                for (cane in inAccoppiamento) {
                    cane.stato = cane.statoPrecedente ?: StatoCane.ADULTO
                    cane.statoPrecedente = null
                }
            }
        }
    }
}