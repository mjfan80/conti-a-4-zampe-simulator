package it.contia4zampe.simulator.engine

import it.contia4zampe.simulator.model.Giocatore

data class StatoGiocatoreGiornata(
    val giocatore: Giocatore,
    var statoTurno: StatoTurno = StatoTurno.OPEN,
    var haFattoUltimoTurno: Boolean = false
)
