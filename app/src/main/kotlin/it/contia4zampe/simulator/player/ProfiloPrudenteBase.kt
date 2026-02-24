package it.contia4zampe.simulator.player

import it.contia4zampe.simulator.engine.*
import it.contia4zampe.simulator.model.*
import it.contia4zampe.simulator.player.decision.ValutatoreAzioneEconomica
import it.contia4zampe.simulator.player.decision.SelettoreMiniPlancia
import it.contia4zampe.simulator.player.decision.ConfigSelettoreMiniPlancia
import it.contia4zampe.simulator.player.decision.PolicyAccoppiamento
import it.contia4zampe.simulator.player.decision.PolicyAccoppiamentoConfig
import it.contia4zampe.simulator.rules.calcolaUpkeep

class ProfiloPrudenteBase : PlayerProfile {

    override fun decidiAzione(statoGiornata: StatoGiornata, statoGiocatore: StatoGiocatoreGiornata): AzioneGiocatore {
        val g = statoGiocatore.giocatore
        val upkeep = calcolaUpkeep(g, statoGiornata.eventoAttivo).costoTotale

        // 1. PRIORITÀ ASSOLUTA: Paga debiti subito
        if (g.debiti > 0 && g.doin >= 2) {
            return AzioneGiocatore.BloccoAzioniSecondarie(listOf(AzioneSecondaria.PagaDebito))
        }

        val azioniPossibili = mutableListOf<AzioneGiocatore>(AzioneGiocatore.Passa)
        for (carta in g.mano) {
            for (r in 0 until g.plancia.righe.size) {
                if (g.plancia.puoOspitareTaglia(r, carta.taglia) && g.plancia.haSpazioInRiga(r)) {
                    azioniPossibili.add(AzioneGiocatore.GiocaCartaRazza(carta, r, g.plancia.righe[r].size))
                }
            }
        }

        // Prudente: accetta solo giocate con score chiaramente positivo.
        val sceltaPrincipale = ValutatoreAzioneEconomica.scegliMigliore(
            statoGiornata, statoGiocatore, azioniPossibili, 
            sogliaScore = 5.0, 
            sogliaSicurezza = 15,  // Vuole sempre 15 doin in tasca
            pesoRiserva = 4.0      // Se scende sotto i 15, ogni doin in meno vale -4 nello score
        )
        if (sceltaPrincipale !is AzioneGiocatore.Passa) {
            return sceltaPrincipale
        }

        val acquistoMiniPlancia = SelettoreMiniPlancia.suggerisciAcquisto(
            stato = statoGiornata,
            sg = statoGiocatore,
            marginePostAcquisto = upkeep + 3,
            config = ConfigSelettoreMiniPlancia(carteMinimeCoperte = 2, adultiMinimiSullaCoppia = 4, scoreMinimoPosizione = 8)
        )

        return acquistoMiniPlancia?.let { AzioneGiocatore.BloccoAzioniSecondarie(listOf(it)) }
            ?: AzioneGiocatore.Passa
    }

    override fun vuoleDichiarareAccoppiamento(sg: StatoGiocatoreGiornata, carta: CartaRazza): Boolean {
        return PolicyAccoppiamento.dovrebbeDichiarare(
            statoGiocatore = sg,
            carta = carta,
            config = PolicyAccoppiamentoConfig(
                sogliaDebitiMassima = 0,
                margineDoinMinimoPostUpkeep = 5,
                consentiPeggioramentoDebiti = false,
                tolleranzaRiduzioneDoin = 2
            )
        )
    }
    

    override fun scegliCartaDalMercato(giocatore: StatoGiocatoreGiornata, mercato: List<CartaRazza>) = mercato.minByOrNull { it.costo } ?: mercato.first()
}
