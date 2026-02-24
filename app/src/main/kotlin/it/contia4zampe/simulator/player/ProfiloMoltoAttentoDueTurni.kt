package it.contia4zampe.simulator.player

import it.contia4zampe.simulator.engine.StatoGiornata
import it.contia4zampe.simulator.engine.StatoGiocatoreGiornata
import it.contia4zampe.simulator.model.Cane
import it.contia4zampe.simulator.model.CartaRazza
import it.contia4zampe.simulator.model.SceltaCucciolo
import it.contia4zampe.simulator.player.decision.ValutatoreAzioneEconomica
import it.contia4zampe.simulator.player.decision.SelettoreMiniPlancia
import it.contia4zampe.simulator.player.decision.ConfigSelettoreMiniPlancia
import it.contia4zampe.simulator.player.decision.PolicyAccoppiamento
import it.contia4zampe.simulator.player.decision.PolicyAccoppiamentoConfig
import it.contia4zampe.simulator.rules.calcolaUpkeep

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

        val sceltaPrincipale = ValutatoreAzioneEconomica.scegliMigliore(
            statoGiornata, statoGiocatore, azioniPossibili, 
            sogliaScore = 7.0, 
            sogliaSicurezza = 15,  // Vuole sempre 15 doin in tasca
            pesoRiserva = 4.0      // Se scende sotto i 15, ogni doin in meno vale -4 nello score
        )
        if (sceltaPrincipale !is AzioneGiocatore.Passa) {
            return sceltaPrincipale
        }

        val upkeep = calcolaUpkeep(giocatore, statoGiornata.eventoAttivo).costoTotale
        val acquistoMiniPlancia = SelettoreMiniPlancia.suggerisciAcquisto(
            stato = statoGiornata,
            sg = statoGiocatore,
            marginePostAcquisto = upkeep + 5,
            config = ConfigSelettoreMiniPlancia(carteMinimeCoperte = 2, adultiMinimiSullaCoppia = 4, scoreMinimoPosizione = 8)
        )

        return acquistoMiniPlancia?.let { AzioneGiocatore.BloccoAzioniSecondarie(listOf(it)) }
            ?: AzioneGiocatore.Passa
    }

    override fun scegliCartaDalMercato(
        giocatore: StatoGiocatoreGiornata,
        mercato: List<CartaRazza>
    ): CartaRazza {
        return mercato.minByOrNull { it.costo } ?: mercato.first()
    }

    override fun vuoleDichiarareAccoppiamento(
        sg: StatoGiocatoreGiornata,
        carta: CartaRazza
    ): Boolean {
        return PolicyAccoppiamento.dovrebbeDichiarare(
            statoGiocatore = sg,
            carta = carta,
            config = PolicyAccoppiamentoConfig(
                sogliaDebitiMassima = sogliaDebitiMassima,
                margineDoinMinimoPostUpkeep = 4,
                consentiPeggioramentoDebiti = false,
                tolleranzaRiduzioneDoin = 3
            )
        )
    }
}
