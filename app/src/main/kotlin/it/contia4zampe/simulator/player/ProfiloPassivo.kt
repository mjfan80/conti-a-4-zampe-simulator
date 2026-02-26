package it.contia4zampe.simulator.player

import it.contia4zampe.simulator.engine.*
import it.contia4zampe.simulator.model.*
import it.contia4zampe.simulator.player.decision.ValutatoreAzioneEconomica

class ProfiloPassivo : PlayerProfile {

    override fun decidiAzione(
        statoGiornata: StatoGiornata,
        statoGiocatore: StatoGiocatoreGiornata
    ): AzioneGiocatore {
        val g = statoGiocatore.giocatore

        // Il pigro valuta di giocare solo se ha più di 22 doin
        if (g.doin > 22) {
            val opzioni = generaGiocatePossibili(g)
            
            // Parametri "Inerzia":
            // sogliaScore = 15.0 (deve essere costretto dalla convenienza)
            // sogliaSicurezza = 20 (non vuole scendere sotto le 20 monete)
            // pesoRiserva = 10.0 (severissimo se scende sotto soglia)
            val scelta = ValutatoreAzioneEconomica.scegliMigliore(
                statoGiornata, statoGiocatore, opzioni, 
                sogliaScore = 15.0, sogliaSicurezza = 20, pesoRiserva = 10.0
            )
            
            if (scelta is AzioneGiocatore.GiocaCartaRazza) return scelta
        }

        // Unica azione secondaria permessa: pagare debiti se è pieno di soldi
        if (g.debiti > 0 && g.doin > 15) {
            return AzioneGiocatore.BloccoAzioniSecondarie(listOf(AzioneSecondaria.PagaDebito))
        }

        // Altrimenti passa. Non addestra, non compra plance extra.
        return AzioneGiocatore.Passa
    }

    override fun decidiGestioneCucciolo(sg: StatoGiocatoreGiornata, cucciolo: Cane): SceltaCucciolo {
        // Vende sempre per semplicità
        return SceltaCucciolo.VENDI
    }

    override fun scegliCartaDalMercato(giocatore: StatoGiocatoreGiornata, mercato: List<CartaRazza>): CartaRazza {
        return mercato.first()
    }

    override fun vuoleDichiarareAccoppiamento(sg: StatoGiocatoreGiornata, carta: CartaRazza): Boolean {
        // Non ha voglia di gestire le nascite
        return false
    }
}