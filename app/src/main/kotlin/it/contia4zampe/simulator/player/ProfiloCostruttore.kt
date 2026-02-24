package it.contia4zampe.simulator.player

import it.contia4zampe.simulator.engine.*
import it.contia4zampe.simulator.model.*
import it.contia4zampe.simulator.player.decision.*
import it.contia4zampe.simulator.rules.calcolaUpkeep

class ProfiloCostruttore : PlayerProfile {

    override fun decidiAzione(statoGiornata: StatoGiornata, statoGiocatore: StatoGiocatoreGiornata): AzioneGiocatore {
        val g = statoGiocatore.giocatore
        
        // 1. EMERGENZA DEBITO CRITICO: Se ho > 5 debiti e sono a secco, vendi un cane
        if (g.debiti > 5 && g.doin < 2) {
            val vendita = trovaCanePerEmergenza(g)
            if (vendita != null) return AzioneGiocatore.VendiCani(listOf(vendita))
        }

        // 2. SE INDEBITATO: Paga prima di fare altro
        if (g.debiti > 0 && g.doin >= 2) {
            return AzioneGiocatore.BloccoAzioniSecondarie(listOf(AzioneSecondaria.PagaDebito))
        }

        // 3. AZIONE PRINCIPALE
        val opzioni = generaGiocatePossibili(g)
        val miglioreGiocata = ValutatoreAzioneEconomica.scegliMigliore(
            statoGiornata, statoGiocatore, opzioni, 
            sogliaScore = 0.5, sogliaSicurezza = 6, pesoRiserva = 2.0
        )
        if (miglioreGiocata is AzioneGiocatore.GiocaCartaRazza) return miglioreGiocata

        // 4. AZIONI SECONDARIE
        val blocco = mutableListOf<AzioneSecondaria>()
        
        val addestramento = cercaAzioneAddestramento(g)
        if (addestramento != null) blocco.add(addestramento)
        
        val acquisto = SelettoreMiniPlancia.suggerisciAcquisto(
            statoGiornata, statoGiocatore, marginePostAcquisto = 1
        )
        if (acquisto != null && blocco.size < 2) blocco.add(acquisto)

        if (blocco.isNotEmpty()) return AzioneGiocatore.BloccoAzioniSecondarie(blocco)

        return AzioneGiocatore.Passa
    }

    override fun vuoleDichiarareAccoppiamento(sg: StatoGiocatoreGiornata, carta: CartaRazza): Boolean {
        // Consente accoppiamento anche con debiti leggeri per fare cassa
        if (sg.giocatore.debiti > 6) return false
        
        return PolicyAccoppiamento.dovrebbeDichiarare(
            sg, carta,
            PolicyAccoppiamentoConfig(4, 2, true, 10)
        )
    }

    override fun decidiGestioneCucciolo(sg: StatoGiocatoreGiornata, cucciolo: Cane): SceltaCucciolo {
        if (sg.giocatore.debiti > 0) return SceltaCucciolo.VENDI
        return SceltaCucciolo.TRASFORMA_IN_ADULTO
    }

    override fun scegliCartaDalMercato(giocatore: StatoGiocatoreGiornata, mercato: List<CartaRazza>) = mercato.first()
}