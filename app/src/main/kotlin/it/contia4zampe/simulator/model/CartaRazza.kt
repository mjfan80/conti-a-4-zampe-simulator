package it.contia4zampe.simulator.model

enum class Taglia {
    PICCOLA,
    MEDIA,
    GRANDE
}

data class CartaRazza(
    val nome: String,
    val costo: Int,
    val rendita: Int,
    val puntiBase: Int,
    val puntiUpgrade: Int,
    val taglia: Taglia,
    val cani: MutableList<Cane> = mutableListOf(),
    var upgrade: Boolean = false,
    var collassata: Boolean = false
)
