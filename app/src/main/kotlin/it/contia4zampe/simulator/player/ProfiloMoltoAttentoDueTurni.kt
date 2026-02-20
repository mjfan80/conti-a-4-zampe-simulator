package it.contia4zampe.simulator.player

import it.contia4zampe.simulator.engine.StatoGiornata
import it.contia4zampe.simulator.engine.StatoGiocatoreGiornata
import it.contia4zampe.simulator.model.Cane
import it.contia4zampe.simulator.model.CartaRazza
import it.contia4zampe.simulator.model.SceltaCucciolo
import it.contia4zampe.simulator.player.decision.ValutatoreAzioneEconomica
import it.contia4zampe.simulator.rules.stimaEconomiaDueGiornateConAccoppiamento

class ProfiloMoltoAttentoDueTurni(
    private val sogliaDebitiMassima: Int = 1
) : PlayerProfile {

    override fun decidiAzione(
        statoGiornata: StatoGiornata,
        statoGiocatore: StatoGiocatoreGiornata
    ): AzioneGiocatore {
        val giocatore = statoGiocatore.giocatore
        val azioniPossibili = mutableListOf<AzioneGiocatore>(AzioneGiocatore.Passa)
        for (carta in giocatore.mano) {
            for (indiceRiga in giocatore.plancia.righe.indices) {
                if (giocatore.plancia.puoOspitareTaglia(indiceRiga, carta.taglia) && giocatore.plancia.haSpazioInRiga(indiceRiga)) {
                    azioniPossibili.add(
                        AzioneGiocatore.GiocaCartaRazza(
                            carta = carta,
                            rigaDestinazione = indiceRiga,
                            slotDestinazione = giocatore.plancia.righe[indiceRiga].size
                        )
                    )
                }
            }
        }

        return ValutatoreAzioneEconomica.scegliMigliore(statoGiornata, statoGiocatore, azioniPossibili, sogliaScore = 12.0)
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
