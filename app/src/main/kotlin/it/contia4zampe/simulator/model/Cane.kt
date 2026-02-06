package it.contia4zampe.simulator.model

enum class StatoCane {
    ADULTO,
    ADULTO_ADDESTRATO,
    CUCCIOLO,
    IN_ADDESTRAMENTO,
    IN_ACCOPPIAMENTO
}

data class Cane(
    val stato: StatoCane
)
