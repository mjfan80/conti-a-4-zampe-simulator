package it.contia4zampe.simulator.player

import it.contia4zampe.simulator.engine.StatoGiornata
import it.contia4zampe.simulator.engine.StatoGiocatoreGiornata
import it.contia4zampe.simulator.model.Cane
import it.contia4zampe.simulator.model.CartaRazza
import it.contia4zampe.simulator.model.SceltaCucciolo
import it.contia4zampe.simulator.rules.calcolaUpkeep
import it.contia4zampe.simulator.rules.puòPiazzareInRiga
import it.contia4zampe.simulator.rules.stimaEconomiaDueGiornateConAccoppiamento

class ProfiloMoltoAttentoDueTurni(
    private val sogliaDebitiMassima: Int = 1
) : PlayerProfile {

    override fun decidiAzione(
        statoGiornata: StatoGiornata,
        statoGiocatore: StatoGiocatoreGiornata
    ): AzioneGiocatore {
        val giocatore = statoGiocatore.giocatore
        val upkeepCorrente = calcolaUpkeep(giocatore).costoTotale

        var miglioreCarta: CartaRazza? = null
        var miglioreRiga = -1
        var migliorCosto = Int.MAX_VALUE

        for (carta in giocatore.mano) {
            if (giocatore.doin < carta.costo) continue

            var doinResidui = giocatore.doin - carta.costo 
            doinResidui -=10 // margine di 10 doin per sicurezza dopo l'acquisto
            if (doinResidui < upkeepCorrente) continue

            for (indiceRiga in giocatore.plancia.righe.indices) {
                if (puòPiazzareInRiga(giocatore.plancia, carta, indiceRiga) && giocatore.plancia.haSpazioInRiga(indiceRiga)) {
                    if (carta.costo < migliorCosto) {
                        miglioreCarta = carta
                        miglioreRiga = indiceRiga
                        migliorCosto = carta.costo
                    }
                }
            }
        }

        if (miglioreCarta != null) {
            return AzioneGiocatore.GiocaCartaRazza(
                carta = miglioreCarta,
                rigaDestinazione = miglioreRiga,
                slotDestinazione = giocatore.plancia.righe[miglioreRiga].size
            )
        }

        return AzioneGiocatore.Passa
    }

    override fun scegliCartaDalMercato(
        giocatore: StatoGiocatoreGiornata,
        mercato: List<CartaRazza>
    ): CartaRazza {
        return mercato.minByOrNull { it.costo } ?: mercato.first()
    }

    override fun vuoleDichiarareAccoppiamento(
        statoGiocatore: StatoGiocatoreGiornata,
        carta: CartaRazza
    ): Boolean {
        val esitoSenza = stimaEconomiaDueGiornateConAccoppiamento(
            statoGiocatore = statoGiocatore,
            cartaTarget = carta,
            dichiaraAccoppiamento = false
        )

        val esitoCon = stimaEconomiaDueGiornateConAccoppiamento(
            statoGiocatore = statoGiocatore,
            cartaTarget = carta,
            dichiaraAccoppiamento = true
        )

        if (esitoCon.debitiFinali > sogliaDebitiMassima) {
            return false
        }

        // Ulteriore vincolo prudente: l'accoppiamento non deve peggiorare la posizione debitoria
        if (esitoCon.debitiFinali > esitoSenza.debitiFinali) {
            return false
        }

        // Se rimane entro soglia debiti, accetta anche riduzioni moderate di liquidità
        return true
    }
}
