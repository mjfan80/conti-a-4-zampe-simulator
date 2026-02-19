package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.*

data class RisultatoUpkeep(
    val costoTotale: Int,
    val pagato: Int,
    val debitiAggiunti: Int
)

fun calcolaUpkeep(giocatore: Giocatore, evento: CartaEvento? = null): RisultatoUpkeep {
    // 1. Calcolo base: 1 doin per ogni cane presente (adulti, cuccioli, tutti)
    var costoTotale = giocatore.plancia.righe
        .flatten()
        .flatMap { it.cani }
        .size

    // 2. APPLICAZIONE EVENTO: Se l'evento modifica l'upkeep totale
    if (evento?.tipo == TipoEffettoEvento.MODIFICA_UPKEEP_TOTALE) {
        costoTotale += evento.variazione
        // Nota: variazione sarà +1 o -1 in base alla carta pescata
    }

    // 3. SICUREZZA: L'upkeep non può mai scendere sotto lo zero
    if (costoTotale < 0) {
        costoTotale = 0
    }

    // 4. Calcolo effettivo del pagamento e dei debiti
    val pagato = minOf(giocatore.doin, costoTotale)
    val debitiAggiunti = costoTotale - pagato

    return RisultatoUpkeep(
        costoTotale = costoTotale,
        pagato = pagato,
        debitiAggiunti = debitiAggiunti
    )
}

fun applicaUpkeep(giocatore: Giocatore, evento: CartaEvento? = null) {
    // Passiamo l'evento anche qui per cascata
    val risultato = calcolaUpkeep(giocatore, evento)

    giocatore.doin -= risultato.pagato
    giocatore.debiti += risultato.debitiAggiunti
}