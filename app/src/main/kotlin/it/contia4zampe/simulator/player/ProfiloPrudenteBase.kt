package it.contia4zampe.simulator.player

import it.contia4zampe.simulator.engine.*
import it.contia4zampe.simulator.model.*
import it.contia4zampe.simulator.rules.calcolaUpkeep
import it.contia4zampe.simulator.rules.puòPiazzareInRiga

class ProfiloPrudenteBase : PlayerProfile {

    override fun decidiAzione(stato: StatoGiornata, sg: StatoGiocatoreGiornata): AzioneGiocatore {
        val g = sg.giocatore
        val upkeep = calcolaUpkeep(g, stato.eventoAttivo).costoTotale

        // 1. PRIORITÀ ASSOLUTA: Paga debiti subito
        if (g.debiti > 0 && g.doin >= 2) {
            return AzioneGiocatore.BloccoAzioniSecondarie(listOf(AzioneSecondaria.PagaDebito))
        }

        // 2. ACQUISTO SUPER PROTETTO: Deve restargli il DOPPIO dell'upkeep in cassa
        for (carta in g.mano) {
            if (g.doin >= (carta.costo + (upkeep * 2) + 5)) {
                for (r in 0 until g.plancia.righe.size) {
                    if (g.plancia.puoOspitareTaglia(r, carta.taglia) && g.plancia.haSpazioInRiga(r)) {
                        return AzioneGiocatore.GiocaCartaRazza(carta, r, g.plancia.righe[r].size)
                    }
                }
            }
        }

        return AzioneGiocatore.Passa
    }

    override fun vuoleDichiarareAccoppiamento(sg: StatoGiocatoreGiornata, carta: CartaRazza): Boolean {
        val g = sg.giocatore
        val nCani = g.plancia.righe.flatten().flatMap { it.cani }.size
        // Regola di buon senso: accoppia solo se hai almeno 5 doin extra oltre all'upkeep
        return g.doin > (nCani + 5) && g.debiti == 0
    }
    

    override fun scegliCartaDalMercato(sg: StatoGiocatoreGiornata, m: List<CartaRazza>) = m.minByOrNull { it.costo } ?: m.first()
}