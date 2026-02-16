package it.contia4zampe.simulator.engine

import it.contia4zampe.simulator.model.Giocatore
import it.contia4zampe.simulator.model.PlanciaGiocatore
import it.contia4zampe.simulator.player.PlayerProfile
import it.contia4zampe.simulator.player.ProfiloPassivo
import it.contia4zampe.simulator.rules.calcolaPuntiVittoriaBase
import it.contia4zampe.simulator.rules.deveTerminarePartita

class PartitaEngine(
    private val giornataEngine: GiornataEngine = GiornataEngine()
) {

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
                collector.onDayStarted(gameId, statoPartita.numero, snapshotGiocatori(statoPartita))

                giornataEngine.eseguiGiornata(statoPartita)

                collector.onDayEnded(gameId, statoPartita.numero, snapshotGiocatori(statoPartita))

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
        val profiliGiocatori = profiliPerGiocatori(config.numeroGiocatori, config.profili)

        val statoGiocatori = (1..config.numeroGiocatori).map { id ->
            val giocatore = Giocatore(
                id = id,
                doin = config.doinIniziali,
                debiti = config.debitiIniziali,
                plancia = PlanciaGiocatore(listOf(mutableListOf(), mutableListOf(), mutableListOf()))
            )
            StatoGiocatoreGiornata(giocatore, profiliGiocatori[id - 1])
        }

        return StatoGiornata(
            numero = 1,
            giocatori = statoGiocatori,
            sogliaPassaggi = config.sogliaPassaggi(),
            maxGiornateEvento = config.maxGiornateEvento
        )
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
}