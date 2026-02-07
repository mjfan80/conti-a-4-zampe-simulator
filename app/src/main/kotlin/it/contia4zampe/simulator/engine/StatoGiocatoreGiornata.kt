package it.contia4zampe.simulator.engine

import it.contia4zampe.simulator.model.Giocatore
import it.contia4zampe.simulator.player.PlayerProfile

data class StatoGiocatoreGiornata(
    val giocatore: Giocatore,
    val profilo: PlayerProfile,
    var statoTurno: StatoTurno = StatoTurno.OPEN,
    var haFattoUltimoTurno: Boolean = false
)
