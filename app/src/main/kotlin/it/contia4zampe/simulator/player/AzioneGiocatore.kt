package it.contia4zampe.simulator.player

sealed class AzioneGiocatore {

    abstract val chiudeTurno: Boolean

    object Passa : AzioneGiocatore() {
        override val chiudeTurno = true
    }

    object AzioneFittizia : AzioneGiocatore() {
        override val chiudeTurno = false
    }
}
