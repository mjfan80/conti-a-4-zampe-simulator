package it.contia4zampe.simulator.player

import it.contia4zampe.simulator.engine.*
import it.contia4zampe.simulator.model.*
import it.contia4zampe.simulator.player.decision.ValutatoreAzioneEconomica
import it.contia4zampe.simulator.player.decision.SelettoreMiniPlancia
import it.contia4zampe.simulator.player.decision.ConfigSelettoreMiniPlancia
import it.contia4zampe.simulator.player.decision.PolicyAccoppiamento
import it.contia4zampe.simulator.player.decision.PolicyAccoppiamentoConfig
import it.contia4zampe.simulator.rules.calcolaUpkeep

class ProfiloAvventato : PlayerProfile {

    override fun decidiAzione(statoGiornata: StatoGiornata, statoGiocatore: StatoGiocatoreGiornata): AzioneGiocatore {
        val g = statoGiocatore.giocatore
        val azioniPossibili = mutableListOf<AzioneGiocatore>(AzioneGiocatore.Passa)
        for (carta in g.mano) {
            for (r in 0 until g.plancia.righe.size) {
                if (g.plancia.puoOspitareTaglia(r, carta.taglia) && g.plancia.haSpazioInRiga(r)) {
                    azioniPossibili.add(AzioneGiocatore.GiocaCartaRazza(carta, r, g.plancia.righe[r].size))
                }
            }
        }

        val miglioreAzione = ValutatoreAzioneEconomica.scegliMigliore(statoGiornata, statoGiocatore, azioniPossibili, sogliaScore = -4.0)
        if (miglioreAzione is AzioneGiocatore.GiocaCartaRazza) {
            return miglioreAzione
        }

        // Vende solo se ha già dei debiti pesanti (3+)
        if (g.debiti >= 3) {
            val vendita = trovaCaneSacrificabile(g)
            if (vendita != null) return AzioneGiocatore.VendiCani(listOf(vendita))
        }

        val upkeep = calcolaUpkeep(g, statoGiornata.eventoAttivo).costoTotale
        val acquistoMiniPlancia = SelettoreMiniPlancia.suggerisciAcquisto(
            stato = statoGiornata,
            sg = statoGiocatore,
            marginePostAcquisto = (upkeep - 1).coerceAtLeast(0),
            config = ConfigSelettoreMiniPlancia(carteMinimeCoperte = 2, adultiMinimiSullaCoppia = 3, scoreMinimoPosizione = 7)
        )
        if (acquistoMiniPlancia != null) {
            return AzioneGiocatore.BloccoAzioniSecondarie(listOf(acquistoMiniPlancia))
        }

        return AzioneGiocatore.Passa
    }

    override fun vuoleDichiarareAccoppiamento(sg: StatoGiocatoreGiornata, carta: CartaRazza): Boolean {
        return PolicyAccoppiamento.dovrebbeDichiarare(
            statoGiocatore = sg,
            carta = carta,
            config = PolicyAccoppiamentoConfig(
                sogliaDebitiMassima = 3,
                margineDoinMinimoPostUpkeep = 0,
                consentiPeggioramentoDebiti = true,
                tolleranzaRiduzioneDoin = 12
            )
        )
    }
    

    private fun trovaCaneQualsiasi(g: Giocatore): DettaglioVendita? {
        for (riga in g.plancia.righe) {
            for (carta in riga) {
                if (carta.cani.isNotEmpty()) {
                    return DettaglioVendita(carta, carta.cani.first())
                }
            }
        }
        return null
    }

    override fun decidiGestioneCucciolo(sg: StatoGiocatoreGiornata, cucciolo: Cane) = SceltaCucciolo.TRASFORMA_IN_ADULTO
    override fun scegliCartaDalMercato(giocatore: StatoGiocatoreGiornata, mercato: List<CartaRazza>) = mercato.maxByOrNull { it.rendita } ?: mercato.first()
}
