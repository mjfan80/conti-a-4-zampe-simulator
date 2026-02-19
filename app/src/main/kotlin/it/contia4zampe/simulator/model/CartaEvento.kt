package it.contia4zampe.simulator.model

enum class TipoEffettoEvento {
    MODIFICA_VENDITA,       // Es: +1 doin per ogni cane venduto
    MODIFICA_UPKEEP,        // Es: +1 doin di costo totale
    MODIFICA_COSTO_ADDESTRA,// Es: Comprare mini-plancia costa -2
    BONUS_RENDITA_INIZIO,   // Es: Tutti guadagnano +2 doin subito
    RESTRITTIVO_ACCOPPIAMENTO, // Es: Non si può accoppiare oggi
    RESTRITTIVO_ADDESTRAMENTO  // Es: Non si può addestrare oggi
}

data class CartaEvento(
    val nome: String,
    val tipo: TipoEffettoEvento,
    val variazione: Int = 0,
    val scartaCartaDopoMercato: Boolean = false // Regola presente in molte carte evento
)