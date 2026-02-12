package it.contia4zampe.simulator.player

import it.contia4zampe.simulator.model.Cane
import it.contia4zampe.simulator.model.CartaRazza

enum class TipoAzione {
    PRINCIPALE,
    SECONDARIA,
    PASSA
}

sealed class AzioneGiocatore {
    abstract val tipo: TipoAzione
    abstract val chiudeTurno: Boolean

    // --- AZIONI PRINCIPALI ---

    data class GiocaCartaRazza(
        val carta: CartaRazza,
        val rigaDestinazione: Int,
        val slotDestinazione: Int = 0
    ) : AzioneGiocatore() {
        override val tipo = TipoAzione.PRINCIPALE
        override val chiudeTurno = false // Di norma non chiude il giorno, solo il turno
    }

    data class VendiCani(
        val vendite: List<DettaglioVendita>,
        val pescaCartaInveceDi5Doin: Boolean = false
    ) : AzioneGiocatore() {
        override val tipo = TipoAzione.PRINCIPALE
        // Se pesca l'extra, il regolamento dice che diventa CLOSED (chiude il giorno per lui)
        override val chiudeTurno = pescaCartaInveceDi5Doin 
    }

    // --- AZIONI SECONDARIE (Wrapper) ---

    // Questa classe rappresenta la scelta del giocatore di fare il "blocco secondarie"
    data class BloccoAzioniSecondarie(
        val azioni: List<AzioneSecondaria> // Può contenerne 1 o 2
    ) : AzioneGiocatore() {
        override val tipo = TipoAzione.SECONDARIA
        override val chiudeTurno = false
    }

    // --- PASSA ---

    object Passa : AzioneGiocatore() {
        override val tipo = TipoAzione.PASSA
        override val chiudeTurno = true
    }
}

// Definiamo cosa può essere un'azione secondaria
sealed class AzioneSecondaria {
    object AcquistaMiniPlancia : AzioneSecondaria()
    data class AddestraCane(val carta: CartaRazza, val cane: Cane) : AzioneSecondaria()
    object SpostaMiniPlancia : AzioneSecondaria()
    object PagaDebito : AzioneSecondaria()
}

data class DettaglioVendita(val carta: CartaRazza, val cane: Cane)