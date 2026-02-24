/* FILE: src/main/kotlin/it/contia4zampe/simulator/player/ProfiloPassivo.kt */
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

        // 1. Paga i debiti solo se proprio "gli avanzano" i soldi
        if (g.debiti > 0 && g.doin >= 15) {
            return AzioneGiocatore.BloccoAzioniSecondarie(listOf(AzioneSecondaria.PagaDebito))
        }

        // 2. Gioca una carta solo se ha un cuscinetto di soldi enorme (> 20 doin)
        // e se la carta è un "affare" ovvio (soglia score alta)
        if (g.doin > 20) {
            val azioniPossibili = mutableListOf<AzioneGiocatore>(AzioneGiocatore.Passa)
            for (carta in g.mano) {
                for (r in 0 until g.plancia.righe.size) {
                    if (g.plancia.puoOspitareTaglia(r, carta.taglia) && g.plancia.haSpazioInRiga(r)) {
                        azioniPossibili.add(AzioneGiocatore.GiocaCartaRazza(carta, r, g.plancia.righe[r].size))
                    }
                }
            }

            // Parametri da "Pigro":
            // sogliaScore = 20.0 (deve essere una carta bellissima per convincerlo)
            // sogliaSicurezza = 18 (vuole restare con quasi tutti i soldi in tasca)
            // pesoRiserva = 10.0 (se scende sotto i 18 doin, l'azione gli fa schifo subito)
            val scelta = ValutatoreAzioneEconomica.scegliMigliore(
                statoGiornata, 
                statoGiocatore, 
                azioniPossibili, 
                sogliaScore = 20.0, 
                sogliaSicurezza = 18, 
                pesoRiserva = 10.0
            )
            
            if (scelta is AzioneGiocatore.GiocaCartaRazza) return scelta
        }

        // 3. Se non è ricchissimo o non ha carte stupende, passa senza pensarci
        return AzioneGiocatore.Passa
    }

    override fun decidiGestioneCucciolo(sg: StatoGiocatoreGiornata, cucciolo: Cane): SceltaCucciolo {
        // Troppa fatica farli crescere: vende tutto subito per fare cassa
        return SceltaCucciolo.VENDI
    }

    override fun scegliCartaDalMercato(giocatore: StatoGiocatoreGiornata, mercato: List<CartaRazza>): CartaRazza {
        // Prende la prima che vede, non guarda nemmeno le statistiche
        return mercato.first()
    }

    override fun vuoleDichiarareAccoppiamento(sg: StatoGiocatoreGiornata, carta: CartaRazza): Boolean {
        // Troppo rischio di upkeep: non accoppia mai
        return false
    }
}