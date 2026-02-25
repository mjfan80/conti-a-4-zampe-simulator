package it.contia4zampe.simulator.player

import it.contia4zampe.simulator.engine.*
import it.contia4zampe.simulator.model.*
import it.contia4zampe.simulator.player.decision.*

class ProfiloCostruttore : PlayerProfile {

    override fun decidiAzione(statoGiornata: StatoGiornata, statoGiocatore: StatoGiocatoreGiornata): AzioneGiocatore {
        val g = statoGiocatore.giocatore
        
        // 1. CRISI VERA: solo se il debito è paralizzante
        if (g.debiti > 5 && g.doin < 2) {
            val vendita = trovaCanePerEmergenza(g)
            if (vendita != null) return AzioneGiocatore.VendiCani(listOf(vendita))
        }

        // 2. AZIONE PRINCIPALE: Gioca carte (Soglia molto bassa per favorire espansione)
        val opzioni = generaGiocatePossibili(g)
        val miglioreGiocata = ValutatoreAzioneEconomica.scegliMigliore(
            statoGiornata, statoGiocatore, opzioni, 
            sogliaScore = -20.0, // Accetta anche score negativi pur di espandersi
            sogliaSicurezza = 4,   // Gli bastano 4 doin per sentirsi pronto
            pesoRiserva = 1.0
        )
        if (miglioreGiocata is AzioneGiocatore.GiocaCartaRazza) return miglioreGiocata

        // 3. AZIONI SECONDARIE
        val blocco = mutableListOf<AzioneSecondaria>()
        if (g.debiti > 0 && g.doin >= 2) blocco.add(AzioneSecondaria.PagaDebito)
        
        val addestramento = cercaAzioneAddestramento(g)
        if (addestramento != null && g.doin >= 2) blocco.add(addestramento)
        
        val acquisto = SelettoreMiniPlancia.suggerisciAcquisto(statoGiornata, statoGiocatore, 1)
        if (acquisto != null && blocco.size < 2) blocco.add(acquisto)

        if (blocco.isNotEmpty()) return AzioneGiocatore.BloccoAzioniSecondarie(blocco)

        return AzioneGiocatore.Passa
    }

    override fun vuoleDichiarareAccoppiamento(sg: StatoGiocatoreGiornata, carta: CartaRazza): Boolean {
        // Accoppia quasi sempre, i cuccioli sono la vita del canile
        if (sg.giocatore.debiti > 8) return false
        return true 
    }

    override fun decidiGestioneCucciolo(sg: StatoGiocatoreGiornata, cucciolo: Cane): SceltaCucciolo {
        if (sg.giocatore.debiti > 3) return SceltaCucciolo.VENDI
        return SceltaCucciolo.TRASFORMA_IN_ADULTO
    }

    override fun scegliCartaDalMercato(giocatore: StatoGiocatoreGiornata, mercato: List<CartaRazza>) = mercato.first()
}