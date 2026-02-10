package it.contia4zampe.simulator.model

enum class StatoCane {
    ADULTO,
    ADULTO_ADDESTRATO,
    CUCCIOLO,
    IN_ADDESTRAMENTO,
    IN_ACCOPPIAMENTO
}

// NUOVO: Enum per la decisione del giocatore (spostato qui come richiesto)
enum class SceltaCucciolo {
    VENDI,
    TRASFORMA_IN_ADULTO
}

data class Cane(
    val id: Int,
    var stato: StatoCane,
    var statoPrecedente: StatoCane? = null,
    val giornataNascita: Int? = null // Null per i cani iniziali
) {
    companion object {
        private var nextId: Int = 1
        
        // Factory method per creare cani con ID automatico
        fun crea(stato: StatoCane, giornataCorrente: Int? = null): Cane {
            return Cane(
                id = nextId++,
                stato = stato,
                giornataNascita = giornataCorrente
            )
        }

        // Per resettare il contatore (utile nei test o tra partite)
        fun resetIdGenerator() {
            nextId = 1
        }
    }
}