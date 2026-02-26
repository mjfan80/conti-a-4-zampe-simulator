package it.contia4zampe.simulator.player

import it.contia4zampe.simulator.engine.*
import it.contia4zampe.simulator.model.*
import it.contia4zampe.simulator.player.decision.*

class ProfiloMoltoAttentoDueTurni(
    private val sogliaDebitiMassima: Int = 1
) : PlayerProfile {

    override fun decidiAzione(statoGiornata: StatoGiornata, statoGiocatore: StatoGiocatoreGiornata): AzioneGiocatore {
        val g = statoGiocatore.giocatore

        // 1. AZIONE PRINCIPALE
        val opzioni = generaGiocatePossibili(g)
        
        // Parametri ultra-conservativi:
        // sogliaScore = 3.0 (vuole una giocata eccellente)
        // sogliaSicurezza = 18 (vuole quasi 20 monete di scorta)
        // pesoRiserva = 8.0 (se scende sotto la soglia, lo score crolla)
        val miglioreGiocata = ValutatoreAzioneEconomica.scegliMigliore(
            statoGiornata, statoGiocatore, opzioni, 
            sogliaScore = 3.0, sogliaSicurezza = 18, pesoRiserva = 8.0
        )
        
        if (miglioreGiocata is AzioneGiocatore.GiocaCartaRazza) return miglioreGiocata

        // 2. AZIONI SECONDARIE
        val blocco = mutableListOf<AzioneSecondaria>()

        // Priorità assoluta: Debiti (non ne vuole nemmeno uno)
        if (g.debiti > 0 && g.doin >= 2) {
            blocco.add(AzioneSecondaria.PagaDebito)
        }
        
        // Addestramento: Solo se è ricchissimo (> 20 doin) e non ha debiti
        val addestramento = cercaAzioneAddestramento(g)
        if (addestramento != null && blocco.size < 2 && g.doin > 20 && g.debiti == 0) {
            blocco.add(addestramento)
        }

        if (blocco.isNotEmpty()) return AzioneGiocatore.BloccoAzioniSecondarie(blocco)

        return AzioneGiocatore.Passa
    }

    override fun vuoleDichiarareAccoppiamento(sg: StatoGiocatoreGiornata, carta: CartaRazza): Boolean {
        // Usa la simulazione a 2 turni per decidere (già implementata)
        return PolicyAccoppiamento.dovrebbeDichiarare(
            sg, carta,
            PolicyAccoppiamentoConfig(
                sogliaDebitiMassima = sogliaDebitiMassima, 
                margineDoinMinimoPostUpkeep = 6, // Molto alto
                consentiPeggioramentoDebiti = false, 
                tolleranzaRiduzioneDoin = 2
            )
        )
    }

    override fun scegliCartaDalMercato(giocatore: StatoGiocatoreGiornata, mercato: List<CartaRazza>) = mercato.minByOrNull { it.costo } ?: mercato.first()
    override fun decidiGestioneCucciolo(sg: StatoGiocatoreGiornata, cucciolo: Cane) = SceltaCucciolo.VENDI
}