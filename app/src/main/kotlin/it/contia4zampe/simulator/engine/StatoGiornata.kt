package it.contia4zampe.simulator.engine

data class StatoGiornata(
    val numero: Int,
    val giocatori: List<StatoGiocatoreGiornata>,
    var fase: FaseGiornata = FaseGiornata.INIZIO,
    var passaggi: Int = 0,
    val sogliaPassaggi: Int,
    var inChiusura: Boolean = false
)
