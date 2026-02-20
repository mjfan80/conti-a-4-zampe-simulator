package it.contia4zampe.simulator.player

import it.contia4zampe.simulator.engine.*
import it.contia4zampe.simulator.model.*
import it.contia4zampe.simulator.player.decision.ValutatoreAzioneEconomica

class ProfiloCostruttore : PlayerProfile {

    override fun decidiAzione(stato: StatoGiornata, sg: StatoGiocatoreGiornata): AzioneGiocatore {
        val g = sg.giocatore
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

        val miglioreAzione = ValutatoreAzioneEconomica.scegliMigliore(stato, sg, azioniPossibili, sogliaScore = 2.0)
        if (miglioreAzione is AzioneGiocatore.GiocaCartaRazza) {
            return miglioreAzione
        }

        // 3. Se non posso comprare e non ho debiti, ma rischio di non pagare l'upkeep stasera: VENDO
        if (g.doin < nCaniAttuali && nCaniAttuali > 2) {
            val vendita = trovaCaneSacrificabile(g)
            if (vendita != null) return AzioneGiocatore.VendiCani(listOf(vendita))
        }

        return AzioneGiocatore.Passa // Chiude la giornata per questo giocatore
    }

    override fun vuoleDichiarareAccoppiamento(sg: StatoGiocatoreGiornata, carta: CartaRazza): Boolean {
        val g = sg.giocatore
        val nCani = g.plancia.righe.flatten().flatMap { it.cani }.size
        // Regola di buon senso: accoppia solo se hai almeno 5 doin extra oltre all'upkeep
        return g.doin > (nCani + 5) && g.debiti <2
    }

    // Faccio l'override perché io voglio tenerli i cuccioli!
    override fun decidiGestioneCucciolo(sg: StatoGiocatoreGiornata, cucciolo: Cane): SceltaCucciolo {
        val g = sg.giocatore
        // Se però sono disperato, vendo anche io
        if (g.debiti > 2) return SceltaCucciolo.VENDI
        
        return SceltaCucciolo.TRASFORMA_IN_ADULTO
    }
    override fun scegliCartaDalMercato(sg: StatoGiocatoreGiornata, m: List<CartaRazza>) = m.first()
}