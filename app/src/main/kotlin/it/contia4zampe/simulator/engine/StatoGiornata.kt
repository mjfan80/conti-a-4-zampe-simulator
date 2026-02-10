package it.contia4zampe.simulator.engine

data class StatoGiornata(
    var numero: Int,               // Era val, ora è VAR: possiamo fare numero++
    val giocatori: List<StatoGiocatoreGiornata>,
    var fase: FaseGiornata = FaseGiornata.INIZIO,
    var passaggi: Int = 0,
    val sogliaPassaggi: Int,
    var inChiusura: Boolean = false,
    var partitaFinita: Boolean = false
)