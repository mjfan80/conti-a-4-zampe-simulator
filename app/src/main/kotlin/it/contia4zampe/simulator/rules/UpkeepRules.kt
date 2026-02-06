package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.Giocatore

data class RisultatoUpkeep(
    val costoTotale: Int,
    val pagato: Int,
    val debitiAggiunti: Int
)

fun calcolaUpkeep(giocatore: Giocatore): RisultatoUpkeep {
    val costoTotale = giocatore.plancia.righe
        .flatten()
        .flatMap { it.cani }
        .size

    val pagato = minOf(giocatore.doin, costoTotale)
    val debitiAggiunti = costoTotale - pagato

    return RisultatoUpkeep(
        costoTotale = costoTotale,
        pagato = pagato,
        debitiAggiunti = debitiAggiunti
    )
}

fun applicaUpkeep(giocatore: Giocatore) {
    val risultato = calcolaUpkeep(giocatore)

    giocatore.doin -= risultato.pagato
    giocatore.debiti += risultato.debitiAggiunti
}
