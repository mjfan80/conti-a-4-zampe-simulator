package it.contia4zampe.simulator.player

import it.contia4zampe.simulator.engine.*
import it.contia4zampe.simulator.model.*
import it.contia4zampe.simulator.player.decision.ValutatoreAzioneEconomica
import it.contia4zampe.simulator.player.decision.SelettoreMiniPlancia
import it.contia4zampe.simulator.rules.calcolaUpkeep

class ProfiloPrudenteBase : PlayerProfile {

    override fun decidiAzione(stato: StatoGiornata, sg: StatoGiocatoreGiornata): AzioneGiocatore {
        val g = sg.giocatore
        val upkeep = calcolaUpkeep(g, stato.eventoAttivo).costoTotale

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
        val sceltaPrincipale = ValutatoreAzioneEconomica.scegliMigliore(stato, sg, azioniPossibili, sogliaScore = (upkeep * 1.5) + 5)
        if (sceltaPrincipale !is AzioneGiocatore.Passa) {
            return sceltaPrincipale
        }

        val acquistoMiniPlancia = SelettoreMiniPlancia.suggerisciAcquisto(
            stato = stato,
            sg = sg,
            marginePostAcquisto = upkeep + 3
        )

        return acquistoMiniPlancia?.let { AzioneGiocatore.BloccoAzioniSecondarie(listOf(it)) }
            ?: AzioneGiocatore.Passa
    }

    override fun vuoleDichiarareAccoppiamento(sg: StatoGiocatoreGiornata, carta: CartaRazza): Boolean {
        val g = sg.giocatore
        val nCani = g.plancia.righe.flatten().flatMap { it.cani }.size
        // Regola di buon senso: accoppia solo se hai almeno 5 doin extra oltre all'upkeep
        return g.doin > (nCani + 5) && g.debiti == 0
    }
    

    override fun scegliCartaDalMercato(sg: StatoGiocatoreGiornata, m: List<CartaRazza>) = m.minByOrNull { it.costo } ?: m.first()
}
