package it.contia4zampe.simulator.model

data class PlanciaGiocatore(
    val righe: List<MutableList<CartaRazza>>
)

/**
 * Rappresenta la mini-plancia di addestramento con due slot distinti,
 * agganciati alle due carte adiacenti in una riga della plancia.
 */
data class MiniPlanciaAddestramento(
    val carte: List<CartaRazza>,
    val slotSinistro: Int,
    var slotSinistroOccupato: Boolean = false,
    var slotDestroOccupato: Boolean = false
) {

    fun haSlotAddestramentoDisponibilePerCarta(carta: CartaRazza): Boolean {
        return when (carte.indexOf(carta)) {
            slotSinistro -> !slotSinistroOccupato
            slotSinistro + 1 -> !slotDestroOccupato
            else -> false
        }
    }

    fun occupaSlotAddestramentoPerCarta(carta: CartaRazza): Boolean {
        if (!haSlotAddestramentoDisponibilePerCarta(carta)) {
            return false
        }

        return when (carte.indexOf(carta)) {
            slotSinistro -> {
                slotSinistroOccupato = true
                true
            }
            slotSinistro + 1 -> {
                slotDestroOccupato = true
                true
            }
            else -> false
        }
    }

    fun liberaUnoSlotAddestramentoPerCarta(carta: CartaRazza): Boolean {
        return when (carte.indexOf(carta)) {
            slotSinistro -> {
                if (!slotSinistroOccupato) return false
                slotSinistroOccupato = false
                true
            }
            slotSinistro + 1 -> {
                if (!slotDestroOccupato) return false
                slotDestroOccupato = false
                true
            }
            else -> false
        }
    }
}
