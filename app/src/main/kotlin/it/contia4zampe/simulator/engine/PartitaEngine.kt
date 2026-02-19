package it.contia4zampe.simulator.engine

import it.contia4zampe.simulator.model.*
import it.contia4zampe.simulator.player.PlayerProfile
import it.contia4zampe.simulator.player.ProfiloPassivo
import it.contia4zampe.simulator.rules.*

class PartitaEngine(private val giornataEngine: GiornataEngine = GiornataEngine()) {

    /**
     * API legacy: esegue una singola partita demo senza restituire risultati strutturati.
     */
    fun avviaPartita() {
        val result = simula(
            SimulationConfig(numeroPartite = 1, numeroGiocatori = 1)
        )

        val partita = result.partite.first()
        println("=== PARTITA TERMINATA ===")
        println("Giornate giocate: ${partita.giornateGiocate}")
        println("Vincitore/i: ${partita.winnerIds}")
    }

    /**
     * Esegue un batch di partite e restituisce un risultato strutturato.
     */
    fun simula(
        config: SimulationConfig,
        collector: SimulationCollector = NoOpSimulationCollector
    ): SimulationResult {
        collector.onSimulationStarted(config)

        val risultati = mutableListOf<PartitaResult>()

        for (gameId in 1..config.numeroPartite) {
            val statoPartita = creaStatoIniziale(config)
            collector.onGameStarted(gameId, statoPartita)

            while (!statoPartita.partitaFinita) {

                // --- LOGICA EVENTI (Aggiungi questo blocco) ---
                // Se siamo oltre il primo giorno, peschiamo un evento
                if (statoPartita.numero > 1) {
                    if (statoPartita.mazzoEventi.isNotEmpty()) {
                        statoPartita.eventoAttivo = statoPartita.mazzoEventi.removeAt(0)
                        println("LOG: Giorno ${statoPartita.numero} - Evento Attivo: ${statoPartita.eventoAttivo?.nome}")
                    } else {
                        statoPartita.eventoAttivo = null // Mazzo finito
                    }
                } else {
                    statoPartita.eventoAttivo = null // Giorno 1: nessun evento
                }
                // ----------------------------------------------

                collector.onDayStarted(gameId, statoPartita.numero, snapshotGiocatori(statoPartita))

                giornataEngine.eseguiGiornata(statoPartita)

                collector.onDayEnded(gameId, statoPartita.numero, snapshotGiocatori(statoPartita))

                println("--- GIORNATA ${statoPartita.numero} ---")
                for (sg in statoPartita.giocatori) {
                    println("G${sg.giocatore.id}: ${sg.giocatore.doin} doin, ${sg.giocatore.debiti} debiti, ${sg.giocatore.plancia.slotOccupatiTotali()} carte")
                }

                println("--- FINE GIORNATA ${statoPartita.numero} ---")
                for (sg in statoPartita.giocatori) {
                    val puntiAttuali = calcolaPuntiVittoriaBase(sg.giocatore)
                    println("G${sg.giocatore.id}: ${sg.giocatore.doin} doin, ${sg.giocatore.debiti} deb, ${sg.giocatore.plancia.slotOccupatiTotali()} carte, PV: $puntiAttuali")
                }


                if (deveTerminarePartita(statoPartita)) {
                    statoPartita.partitaFinita = true
                } else {
                    avanzaGiornata(statoPartita)
                }
            }

            val partitaResult = costruisciRisultatoPartita(gameId, statoPartita)
            collector.onGameEnded(partitaResult)
            risultati.add(partitaResult)
        }

        return SimulationResult(config = config, partite = risultati)
            .also { collector.onSimulationEnded(it) }
    }

    private fun snapshotGiocatori(stato: StatoGiornata): List<PlayerSnapshot> {
        return stato.giocatori.map { sg ->
            PlayerSnapshot(
                playerId = sg.giocatore.id,
                doin = sg.giocatore.doin,
                debiti = sg.giocatore.debiti,
                carteInPlancia = sg.giocatore.plancia.righe.sumOf { it.size },
                carteInMano = sg.giocatore.mano.size
            )
        }
    }

    private fun costruisciRisultatoPartita(gameId: Int, stato: StatoGiornata): PartitaResult {
        val playerResults = stato.giocatori.map { sg ->
            val giocatore = sg.giocatore
            PlayerGameResult(
                playerId = giocatore.id,
                puntiFinali = calcolaPuntiVittoriaBase(giocatore),
                doinFinali = giocatore.doin,
                debitiFinali = giocatore.debiti,
                carteInPlanciaFinali = giocatore.plancia.righe.sumOf { it.size },
                carteInManoFinali = giocatore.mano.size,
                carteInPlanciaNomi = giocatore.plancia.righe.flatten().map { it.nome }
            )
        }

        val maxPunti = playerResults.maxOfOrNull { it.puntiFinali } ?: 0
        val winners = playerResults
            .filter { it.puntiFinali == maxPunti }
            .map { it.playerId }

        return PartitaResult(
            gameId = gameId,
            giornateGiocate = stato.numero,
            playerResults = playerResults,
            winnerIds = winners
        )
    }

    private fun avanzaGiornata(stato: StatoGiornata) {
        stato.numero++
        stato.passaggi = 0
        stato.inChiusura = false
        stato.fase = FaseGiornata.INIZIO

        stato.giocatori.forEach { statoGiocatore ->
            statoGiocatore.statoTurno = StatoTurno.OPEN
            statoGiocatore.haFattoUltimoTurno = false
        }
    }

    private fun creaStatoIniziale(config: SimulationConfig): StatoGiornata {
        val mazzoRazze = creaMazzoRazzeBase()
        val profiliGiocatori = profiliPerGiocatori(config.numeroGiocatori, config.profili)

        // 1. Creazione dei giocatori e assegnazione mano iniziale (3 carte)
        val statiGiocatori = mutableListOf<StatoGiocatoreGiornata>()
        for (id in 1..config.numeroGiocatori) {
            val giocatore = Giocatore(
                id = id,
                doin = config.doinIniziali,
                debiti = config.debitiIniziali,
                plancia = PlanciaGiocatore(listOf(mutableListOf(), mutableListOf(), mutableListOf()))
            )
            
            // Mano iniziale di 3 carte
            for (i in 0 until 3) {
                if (mazzoRazze.isNotEmpty()) {
                    giocatore.mano.add(mazzoRazze.removeAt(0))
                }
            }
            
            statiGiocatori.add(StatoGiocatoreGiornata(giocatore, profiliGiocatori[id - 1]))
        }

        // 2. Popolamento iniziale del mercato comune (5 carte)
        val mercato = mutableListOf<CartaRazza>()
        for (i in 0 until 5) {
            if (mazzoRazze.isNotEmpty()) {
                mercato.add(mazzoRazze.removeAt(0))
            }
        }

        // 3. Creazione dello stato temporaneo per gestire l'accelerazione
        val stato = StatoGiornata(
            numero = 1,
            giocatori = statiGiocatori,
            sogliaPassaggi = config.sogliaPassaggi(),
            maxGiornateEvento = config.maxGiornateEvento,
            mercatoComune = mercato,
            mazzoCarteRazza = mazzoRazze,
            mazzoEventi = creaMazzoEventiBaseCompleto()
        )

        // 4. Applicazione Accelerazione Iniziale (dal Mercato)
        for (sg in stato.giocatori) {
            applicaAccelerazioneIniziale(sg, stato)
        }

        return stato
    }

    private fun profiliPerGiocatori(numeroGiocatori: Int, profili: List<PlayerProfile>): List<PlayerProfile> {
        if (profili.isEmpty()) {
            return List(numeroGiocatori) { ProfiloPassivo() }
        }

        val risultato = mutableListOf<PlayerProfile>()
        for (i in 0 until numeroGiocatori) {
            risultato.add(profili[i % profili.size])
        }
        return risultato
    }

    private fun preparaMazzoEventi(dimensione: Int): MutableList<CartaEvento> {
        val mazzo = mutableListOf<CartaEvento>()
        
        // Esempio: riempiamo il mazzo con alcuni eventi tipo
        // In futuro qui leggeremo un file o un database
        for (i in 0 until dimensione) {
            val casuale = (1..3).random()
            when (casuale) {
                1 -> mazzo.add(CartaEvento("Mercato Favorevole", TipoEffettoEvento.MODIFICA_VENDITA, variazione = 1))
                2 -> mazzo.add(CartaEvento("Upkeep Pesante", TipoEffettoEvento.MODIFICA_UPKEEP_TOTALE, variazione = 1))
                3 -> mazzo.add(CartaEvento("Sussidi", TipoEffettoEvento.BONUS_DOIN_INIZIO, variazione = 2))
            }
        }
        
        mazzo.shuffle() // Mischiamo il mazzo
        return mazzo
    }

    private fun applicaAccelerazioneIniziale(sg: StatoGiocatoreGiornata, stato: StatoGiornata) {
        val g = sg.giocatore
        var cartaScelta: CartaRazza? = null

        // 1. Cerchiamo nel mercato una carta che costa <= 5
        for (i in 0 until stato.mercatoComune.size) {
            val carta = stato.mercatoComune[i]
            if (carta.costo <= 5) {
                cartaScelta = carta
                break
            }
        }

        if (cartaScelta != null) {
            // 2. Cerchiamo una riga che possa ospitare questa taglia
            var rigaDestinazione = -1
            for (r in 0 until g.plancia.righe.size) {
                if (g.plancia.puoOspitareTaglia(r, cartaScelta.taglia) && g.plancia.haSpazioInRiga(r)) {
                    rigaDestinazione = r
                    break
                }
            }

            // 3. Se troviamo posto, la giochiamo (senza cani, Phase 2 li aggiungerà)
            if (rigaDestinazione != -1) {
                stato.mercatoComune.remove(cartaScelta)
                g.plancia.righe[rigaDestinazione].add(cartaScelta)
                
                // Ricarica mercato
                if (stato.mazzoCarteRazza.isNotEmpty()) {
                    stato.mercatoComune.add(stato.mazzoCarteRazza.removeAt(0))
                }
                println("SETUP: G${g.id} accelera con ${cartaScelta.nome} in riga $rigaDestinazione")
            } else {
                // Caso limite: carta trovata ma nessun posto in plancia (difficile al setup)
                g.doin += 5
            }
        } else {
            // Nessuna carta economica nel mercato
            g.doin += 5
            println("SETUP: G${g.id} riceve 5 doin extra")
        }
    }
}