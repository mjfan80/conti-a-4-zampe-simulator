package it.contia4zampe.simulator.player
import it.contia4zampe.simulator.model.CartaRazza

sealed class AzioneGiocatore {

    abstract val chiudeTurno: Boolean

    object Passa : AzioneGiocatore() {
        override val chiudeTurno = true
    }

    object AzioneFittizia : AzioneGiocatore() {
        override val chiudeTurno = false
    }

    data class GiocaCartaRazza(
            val carta: CartaRazza
        ) : AzioneGiocatore() {
            override val chiudeTurno = false
        }

}
