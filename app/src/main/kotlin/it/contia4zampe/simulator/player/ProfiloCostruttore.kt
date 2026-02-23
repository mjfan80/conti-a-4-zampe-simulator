package it.contia4zampe.simulator.player

import it.contia4zampe.simulator.engine.*
import it.contia4zampe.simulator.model.*
import it.contia4zampe.simulator.player.decision.ValutatoreAzioneEconomica
import it.contia4zampe.simulator.player.decision.SelettoreMiniPlancia
import it.contia4zampe.simulator.player.decision.ConfigSelettoreMiniPlancia
import it.contia4zampe.simulator.player.decision.PolicyAccoppiamento
import it.contia4zampe.simulator.player.decision.PolicyAccoppiamentoConfig
import it.contia4zampe.simulator.rules.calcolaUpkeep

class ProfiloCostruttore : PlayerProfile {

    override fun decidiAzione(statoGiornata: StatoGiornata, statoGiocatore: StatoGiocatoreGiornata): AzioneGiocatore {
        val g = statoGiocatore.giocatore
        val nCaniAttuali = g.plancia.righe.flatten().flatMap { it.cani }.size
        
        // 1. Se ho debiti e ho soldi, la priorità è pagare il debito (Azione Secondaria)
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

        val miglioreAzione = ValutatoreAzioneEconomica.scegliMigliore(statoGiornata, statoGiocatore, azioniPossibili, sogliaScore = 0.5)
        if (miglioreAzione is AzioneGiocatore.GiocaCartaRazza) {
            return miglioreAzione
        }

        // 3. Se non posso comprare e non ho debiti, ma rischio di non pagare l'upkeep stasera: VENDO
        if (g.doin < nCaniAttuali && nCaniAttuali > 2) {
            val vendita = trovaCaneSacrificabile(g)
            if (vendita != null) return AzioneGiocatore.VendiCani(listOf(vendita))
        }

        val upkeep = calcolaUpkeep(g, statoGiornata.eventoAttivo).costoTotale
        val acquistoMiniPlancia = SelettoreMiniPlancia.suggerisciAcquisto(
            stato = statoGiornata,
            sg = statoGiocatore,
            marginePostAcquisto = upkeep + 1,
            config = ConfigSelettoreMiniPlancia(carteMinimeCoperte = 2, adultiMinimiSullaCoppia = 3, scoreMinimoPosizione = 7)
        )
        if (acquistoMiniPlancia != null) {
            return AzioneGiocatore.BloccoAzioniSecondarie(listOf(acquistoMiniPlancia))
        }

        return AzioneGiocatore.Passa // Chiude la giornata per questo giocatore
    }

    override fun vuoleDichiarareAccoppiamento(sg: StatoGiocatoreGiornata, carta: CartaRazza): Boolean {
        return PolicyAccoppiamento.dovrebbeDichiarare(
            statoGiocatore = sg,
            carta = carta,
            config = PolicyAccoppiamentoConfig(
                sogliaDebitiMassima = 2,
                margineDoinMinimoPostUpkeep = 2,
                consentiPeggioramentoDebiti = true,
                tolleranzaRiduzioneDoin = 8
            )
        )
    }

    // Faccio l'override perché io voglio tenerli i cuccioli!
    override fun decidiGestioneCucciolo(sg: StatoGiocatoreGiornata, cucciolo: Cane): SceltaCucciolo {
        val g = sg.giocatore
        val upkeepAttuale = sg.calcolaUpkeepAttuale()
        val upkeepFuturo = sg.calcolaUpkeepFuturo()

        // Se sono in stress economico vendo il cucciolo subito per fare cassa.
        if (g.debiti > 1 || g.doin <= upkeepAttuale || g.doin <= upkeepFuturo) {
            return SceltaCucciolo.VENDI
        }

        return SceltaCucciolo.TRASFORMA_IN_ADULTO
    }
    override fun scegliCartaDalMercato(giocatore: StatoGiocatoreGiornata, mercato: List<CartaRazza>) = mercato.first()
}
