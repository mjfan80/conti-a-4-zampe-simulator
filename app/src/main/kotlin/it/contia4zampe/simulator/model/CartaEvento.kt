package it.contia4zampe.simulator.model

enum class TipoEffettoEvento {
    MODIFICA_VENDITA,               // +/- doin per capo
    MODIFICA_COSTO_PLANCIAADDESTRAMENTO,   // +/- doin per mini-plancia
    BLOCCO_ACCOPPIAMENTO,           // Non si accoppia
    BLOCCO_ACQUISTO_ADDESTRAMENTO,  // Non si comprano mini-plance
    RENDITA_CUCCIOLI,               // Cuccioli producono +1
    MODIFICA_UPKEEP_TOTALE,         // +/- doin al totale upkeep
    MODIFICA_COSTO_RAZZA_TAGLIA,    // +/- doin per taglia specifica
    MODIFICA_COSTO_RAZZA_TUTTE,     // -1 doin per tutte le razze
    RITMO_GIORNATA,                 // Serve un pass in più
    BONUS_DOIN_INIZIO               // +2 doin a inizio giornata
}

data class CartaEvento(
    val nome: String,
    val tipo: TipoEffettoEvento,
    val variazione: Int = 0,
    val tagliaTarget: Taglia? = null,        // Per eventi legati a taglie specifiche
    val tagliaTargetSecondaria: Taglia? = null, // Per eventi con due taglie (es: Grandi +1, Piccole -1)
    val variazioneSecondaria: Int = 0,
    val penalitàScartoMercato: Boolean = false // La regola "Scegli dal mercato e scarta dalla mano"
)