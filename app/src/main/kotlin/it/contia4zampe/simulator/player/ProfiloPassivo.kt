package it.contia4zampe.simulator.player

import it.contia4zampe.simulator.engine.StatoGiornata
import it.contia4zampe.simulator.engine.StatoGiocatoreGiornata
import it.contia4zampe.simulator.model.Cane
import it.contia4zampe.simulator.model.SceltaCucciolo
import it.contia4zampe.simulator.model.CartaRazza

class ProfiloPassivo : PlayerProfile {

    override fun decidiAzione(
        statoGiornata: StatoGiornata,
        statoGiocatore: StatoGiocatoreGiornata
    ): AzioneGiocatore {
        return AzioneGiocatore.Passa
    }

    // IMPLEMENTAZIONE MANCANTE:
    override fun decidiGestioneCucciolo(
        statoGiocatore: StatoGiocatoreGiornata,
        cucciolo: Cane
    ): SceltaCucciolo {
        // Il profilo passivo è "pigro" o conservativo: vende per fare cassa subito
        return SceltaCucciolo.VENDI
    }

    override fun scegliCartaDalMercato(
        giocatore: StatoGiocatoreGiornata,
        mercato: List<CartaRazza>
    ): CartaRazza {
        // Strategia base: prendi la prima carta della fila
        return mercato.first()
    }

    override fun vuoleDichiarareAccoppiamento(
        statoGiocatore: StatoGiocatoreGiornata,
        carta: CartaRazza
    ): Boolean {
        // Profilo passivo: evita rischio/complessità e non dichiara accoppiamenti
        return false
    }
}
