package it.contia4zampe.simulator.engine

import it.contia4zampe.simulator.model.CartaRazza

data class StatoGiornata(
    var numero: Int,
    val giocatori: List<StatoGiocatoreGiornata>,
    var fase: FaseGiornata = FaseGiornata.INIZIO,
    var passaggi: Int = 0,
    val sogliaPassaggi: Int,
    var inChiusura: Boolean = false,
    var partitaFinita: Boolean = false,
    
    // NUOVI CAMPI:
    val mercatoComune: MutableList<CartaRazza> = mutableListOf(),
    var indicePrimoGiocatore: Int = 0, // Chi inizia questo giorno
    val mazzoCarteRazza: MutableList<CartaRazza> = mutableListOf(), // Da dove peschiamo
    val maxGiornateEvento: Int = 15
)