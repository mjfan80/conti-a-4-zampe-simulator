package it.contia4zampe.simulator.model

enum class StatoCane {
    ADULTO,
    ADULTO_ADDESTRATO,
    CUCCIOLO,
    IN_ADDESTRAMENTO,
    IN_ACCOPPIAMENTO
}

data class Cane(
    val id: Int,
    var stato: StatoCane,
    var statoPrecedente: StatoCane? = null
) {
    companion object {
        private var nextId: Int = 1
        
        // Factory method per creare cani con ID automatico
        fun crea(stato: StatoCane): Cane {
            return Cane(
                id = nextId++,
                stato = stato
            )
        }

        // Per resettare il contatore (utile nei test o tra partite)
        fun resetIdGenerator() {
            nextId = 1
        }
    }
}