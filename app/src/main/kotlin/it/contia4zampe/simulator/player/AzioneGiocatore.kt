package it.contia4zampe.simulator.player

import it.contia4zampe.simulator.model.Cane
import it.contia4zampe.simulator.model.CartaRazza

sealed class AzioneGiocatore {
    abstract val chiudeTurno: Boolean

    object Passa : AzioneGiocatore() {
        override val chiudeTurno = true
    }

    data class GiocaCartaRazza(val carta: CartaRazza) : AzioneGiocatore() {
        override val chiudeTurno = false
    }

    // NUOVA AZIONE: Vendita
    // Contiene una lista di "pacchetti" (quale cane da quale carta)
    data class VendiCani(
        val vendite: List<DettaglioVendita>,
        val pescaCartaInveceDi5Doin: Boolean = false // Regola speciale vendita >= 5
    ) : AzioneGiocatore() {
        // Se scegli di pescare la carta invece dei 5 doin, diventi CLOSED (chiude il turno)
        override val chiudeTurno = pescaCartaInveceDi5Doin
    }
}

// Classe di supporto per mappare cosa vendiamo
data class DettaglioVendita(
    val carta: CartaRazza,
    val cane: Cane
)