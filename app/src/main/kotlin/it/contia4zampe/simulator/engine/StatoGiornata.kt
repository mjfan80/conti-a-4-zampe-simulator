package it.contia4zampe.simulator.engine

import it.contia4zampe.simulator.model.Giocatore

data class StatoGiornata(
    val numero: Int,
    val giocatori: List<Giocatore>,
    var fase: FaseGiornata = FaseGiornata.INIZIO,
    var passaggi: Int = 0,
    val sogliaPassaggi: Int
)
