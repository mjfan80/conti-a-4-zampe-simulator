package it.contia4zampe.simulator.model

data class Giocatore(
    val id: Int,
    var doin: Int,
    var debiti: Int,
    val plancia: PlanciaGiocatore
)
