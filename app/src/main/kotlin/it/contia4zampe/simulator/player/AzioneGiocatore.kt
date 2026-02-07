package it.contia4zampe.simulator.player

sealed class AzioneGiocatore {

    object Passa : AzioneGiocatore()

    // placeholder per il futuro
    object AzioneFittizia : AzioneGiocatore()
}
