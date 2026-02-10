package it.contia4zampe.simulator.model

enum class Taglia {
    PICCOLA,
    MEDIA,
    GRANDE
}

package it.contia4zampe.simulator.model

// Definiamo gli effetti che possono attivarsi a inizio giornata
enum class EffettoInizioGiornata {
    NESSUNO,
    DOIN1_RENDITA,       // +1 doin
    DOIN1_RENDITA_UNICORIGA // +1 doin se unica nella riga
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
    var collassata: Boolean = false,
    
    // NUOVO: La carta dichiara il suo effetto
    val effettoInizio: EffettoInizioGiornata = EffettoInizioGiornata.NESSUNO
)
