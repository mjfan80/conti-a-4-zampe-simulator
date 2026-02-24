package it.contia4zampe.simulator.player

import it.contia4zampe.simulator.engine.*
import it.contia4zampe.simulator.model.*
import it.contia4zampe.simulator.player.decision.*
import it.contia4zampe.simulator.rules.calcolaUpkeep

class ProfiloPrudenteBase : PlayerProfile {

    override fun decidiAzione(statoGiornata: StatoGiornata, statoGiocatore: StatoGiocatoreGiornata): AzioneGiocatore {
        val g = statoGiocatore.giocatore

        // 1. PRIORITÀ: SE INDEBITATO, PAGA (se ha almeno 4 doin)
        if (g.debiti > 0 && g.doin >= 4) {
            return AzioneGiocatore.BloccoAzioniSecondarie(listOf(AzioneSecondaria.PagaDebito))
        }

        // 2. AZIONE PRINCIPALE: GIOCA CARTA
        val opzioni = generaGiocatePossibili(g)
        val sogliaS = if (g.doin > 25) -5.0 else 5.0
        val scelta = ValutatoreAzioneEconomica.scegliMigliore(
            statoGiornata, statoGiocatore, opzioni, 
            sogliaScore = sogliaS, sogliaSicurezza = 10, pesoRiserva = 3.0
        )
        if (scelta is AzioneGiocatore.GiocaCartaRazza) return scelta

        // 3. AZIONI SECONDARIE
        val blocco = mutableListOf<AzioneSecondaria>()
        
        // Addestramento (solo se ha un discreto margine)
        val addestramento = cercaAzioneAddestramento(g)
        if (addestramento != null && g.doin > 12) {
            blocco.add(addestramento)
        }

        // Mini-Plancia (Gate severo)
        val acquisto = SelettoreMiniPlancia.suggerisciAcquisto(
            statoGiornata, statoGiocatore, marginePostAcquisto = 5
        )
        if (acquisto != null && blocco.size < 2) {
            blocco.add(acquisto)
        }

        if (blocco.isNotEmpty()) return AzioneGiocatore.BloccoAzioniSecondarie(blocco)

        return AzioneGiocatore.Passa
    }

    override fun vuoleDichiarareAccoppiamento(sg: StatoGiocatoreGiornata, carta: CartaRazza): Boolean {
        // Prudente: non accoppia se ha debiti
        if (sg.giocatore.debiti > 0) return false
        return PolicyAccoppiamento.dovrebbeDichiarare(
            sg, carta,
            PolicyAccoppiamentoConfig(0, 5, false, 2)
        )
    }

    override fun scegliCartaDalMercato(giocatore: StatoGiocatoreGiornata, mercato: List<CartaRazza>) = mercato.minByOrNull { it.costo } ?: mercato.first()
}