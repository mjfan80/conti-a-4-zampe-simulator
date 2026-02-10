package it.contia4zampe.simulator.player

import it.contia4zampe.simulator.engine.StatoGiornata
import it.contia4zampe.simulator.engine.StatoGiocatoreGiornata
import it.contia4zampe.simulator.model.Cane
import it.contia4zampe.simulator.model.SceltaCucciolo // Importiamo l'enum dal model
import it.contia4zampe.simulator.player.AzioneGiocatore
import it.contia4zampe.simulator.model.CartaRazza


interface PlayerProfile {

    fun decidiAzione(
        statoGiornata: StatoGiornata,
        statoGiocatore: StatoGiocatoreGiornata
    ): AzioneGiocatore

    // Metodo per la decisione sui cuccioli maturi
    fun decidiGestioneCucciolo(
        statoGiocatore: StatoGiocatoreGiornata,
        cucciolo: Cane
    ): SceltaCucciolo

     // NUOVO: Sceglie una carta tra quelle scoperte sul tavolo
    fun scegliCartaDalMercato(
        giocatore: StatoGiocatoreGiornata,
        mercato: List<CartaRazza>
    ): CartaRazza
}