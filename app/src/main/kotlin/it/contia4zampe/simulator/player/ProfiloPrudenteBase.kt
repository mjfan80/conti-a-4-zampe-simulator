package it.contia4zampe.simulator.player

import it.contia4zampe.simulator.engine.*
import it.contia4zampe.simulator.model.*
import it.contia4zampe.simulator.player.decision.*

class ProfiloPrudenteBase : PlayerProfile {

    override fun decidiAzione(statoGiornata: StatoGiornata, statoGiocatore: StatoGiocatoreGiornata): AzioneGiocatore {
        val g = statoGiocatore.giocatore

        // 1. RIGORE FINANZIARIO: Paga debiti appena possibile
        if (g.debiti > 0 && g.doin >= 2) {
            return AzioneGiocatore.BloccoAzioniSecondarie(listOf(AzioneSecondaria.PagaDebito))
        }

        // 2. AZIONE PRINCIPALE: Gioca carte se hanno un senso (Score 0.0)
        val opzioni = generaGiocatePossibili(g)
        val scelta = ValutatoreAzioneEconomica.scegliMigliore(
            statoGiornata, statoGiocatore, opzioni,
            sogliaScore = 0.0,  // Accetta giocate "in pareggio", non aspetta l'occasione perfetta
            sogliaSicurezza = 8, // Abbassata da 15 a 8 doin
            pesoRiserva = 3.0
        )
        if (scelta is AzioneGiocatore.GiocaCartaRazza) return scelta

        // 3. AZIONI SECONDARIE
        val blocco = mutableListOf<AzioneSecondaria>()

        val addestramento = cercaAzioneAddestramento(g)
        if (addestramento != null && g.doin > 8) blocco.add(addestramento)

        val acquisto = SelettoreMiniPlancia.suggerisciAcquisto(statoGiornata, statoGiocatore, 4)
        if (acquisto != null && blocco.size < 2) blocco.add(acquisto)

        if (blocco.isNotEmpty()) return AzioneGiocatore.BloccoAzioniSecondarie(blocco)

        return AzioneGiocatore.Passa
    }

    override fun vuoleDichiarareAccoppiamento(sg: StatoGiocatoreGiornata, carta: CartaRazza): Boolean {
        // Il prudente accoppia se non ha troppi debiti (max 2)
        if (sg.giocatore.debiti > 2) return false
        return true
    }

    override fun decidiGestioneCucciolo(sg: StatoGiocatoreGiornata, cucciolo: Cane): SceltaCucciolo {
        // Vende per sicurezza se ha debiti, altrimenti fa crescere
        if (sg.giocatore.debiti > 0) return SceltaCucciolo.VENDI
        return SceltaCucciolo.TRASFORMA_IN_ADULTO
    }

    override fun scegliCartaDalMercato(giocatore: StatoGiocatoreGiornata, mercato: List<CartaRazza>) = mercato.minByOrNull { it.costo } ?: mercato.first()
}