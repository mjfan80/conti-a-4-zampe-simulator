package it.contia4zampe.simulator.player

import it.contia4zampe.simulator.engine.*
import it.contia4zampe.simulator.model.*
import it.contia4zampe.simulator.rules.puòPiazzareInRiga

class ProfiloAvventato : PlayerProfile {

    override fun decidiAzione(stato: StatoGiornata, sg: StatoGiocatoreGiornata): AzioneGiocatore {
        val g = sg.giocatore
        val nCaniAttuali = g.plancia.righe.flatten().flatMap { it.cani }.size

        // Compra se gli restano i soldi giusti per l'upkeep dei cani che avrà stasera
        for (carta in g.mano) {
            if (g.doin >= (carta.costo + nCaniAttuali + 2)) {
                for (r in 0 until g.plancia.righe.size) {
                    if (g.plancia.puoOspitareTaglia(r, carta.taglia) && g.plancia.haSpazioInRiga(r)) {
                        return AzioneGiocatore.GiocaCartaRazza(carta, r, g.plancia.righe[r].size)
                    }
                }
            }
        }

        // Vende solo se ha già dei debiti pesanti (3+)
        if (g.debiti >= 3) {
            val vendita = trovaCaneSacrificabile(g)
            if (vendita != null) return AzioneGiocatore.VendiCani(listOf(vendita))
        }

        return AzioneGiocatore.Passa
    }

    override fun vuoleDichiarareAccoppiamento(sg: StatoGiocatoreGiornata, carta: CartaRazza): Boolean {
        val g = sg.giocatore
        val nCani = g.plancia.righe.flatten().flatMap { it.cani }.size
        // Regola di buon senso: accoppia solo se hai almeno 2 doin extra oltre all'upkeep
        return g.doin > (nCani + 2) && g.debiti <3
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

    override fun decidiGestioneCucciolo(sg: StatoGiocatoreGiornata, c: Cane) = SceltaCucciolo.TRASFORMA_IN_ADULTO
    override fun scegliCartaDalMercato(sg: StatoGiocatoreGiornata, m: List<CartaRazza>) = m.maxByOrNull { it.rendita } ?: m.first()
}