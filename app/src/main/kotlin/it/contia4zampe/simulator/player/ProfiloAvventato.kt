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
        
        // 1. AZIONE PRINCIPALE: Gioca quasi tutto
        val opzioni = generaGiocatePossibili(g)
        val migliore = ValutatoreAzioneEconomica.scegliMigliore(
            statoGiornata, statoGiocatore, opzioni, 
            sogliaScore = -10.0, sogliaSicurezza = 5, pesoRiserva = 0.0
        )
        if (migliore is AzioneGiocatore.GiocaCartaRazza) return migliore

        // 2. AZIONI SECONDARIE
        val blocco = mutableListOf<AzioneSecondaria>()
        
        // L'avventato non paga debiti a meno di non essere ricchissimo (già gestito in precedenza)
        
        val addestramento = cercaAzioneAddestramento(g)
        if (addestramento != null) blocco.add(addestramento)
        
        val acquisto = SelettoreMiniPlancia.suggerisciAcquisto(
            statoGiornata, statoGiocatore, marginePostAcquisto = 0
        )
        if (acquisto != null && blocco.size < 2) blocco.add(acquisto)

        if (blocco.isNotEmpty()) return AzioneGiocatore.BloccoAzioniSecondarie(blocco)
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

    override fun decidiGestioneCucciolo(sg: StatoGiocatoreGiornata, cucciolo: Cane): SceltaCucciolo {
        val g = sg.giocatore
        val upkeepAttuale = sg.calcolaUpkeepAttuale()

        // Anche l'avventato, se corto di liquidità o in debito, monetizza subito il cucciolo.
        if (g.debiti > 0 || g.doin <= upkeepAttuale) {
            return SceltaCucciolo.VENDI
        }

        return SceltaCucciolo.TRASFORMA_IN_ADULTO
    }
    override fun scegliCartaDalMercato(giocatore: StatoGiocatoreGiornata, mercato: List<CartaRazza>) = mercato.maxByOrNull { it.rendita } ?: mercato.first()
}
