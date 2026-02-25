package it.contia4zampe.simulator.player.decision

import it.contia4zampe.simulator.engine.StatoGiocatoreGiornata
import it.contia4zampe.simulator.engine.StatoGiornata
import it.contia4zampe.simulator.model.*
import it.contia4zampe.simulator.player.AzioneGiocatore
import it.contia4zampe.simulator.rules.*

data class EsitoValutazioneEconomica(
    val azione: AzioneGiocatore,
    val score: Double,
    val debitiAttesi: Int,
    val doinResiduiPostCosto: Int
)

object ValutatoreAzioneEconomica {

    fun valuta(
        statoGiornata: StatoGiornata,
        statoGiocatore: StatoGiocatoreGiornata,
        azione: AzioneGiocatore,
        sogliaSicurezza: Int,
        pesoRiserva: Double
    ): EsitoValutazioneEconomica {
        val giocatore = statoGiocatore.giocatore
        val evento = statoGiornata.eventoAttivo

        // 1. COSTO AZIONE
        var costoAzione = 0
        if (azione is AzioneGiocatore.GiocaCartaRazza) {
            costoAzione = costoCartaConEvento(azione.carta, evento)
        }
        
        // Se non ho i soldi base, non posso farla
        if (costoAzione > giocatore.doin) return EsitoValutazioneEconomica(azione, -10000.0, 99, -1)

        // 2. MONDO VIRTUALE
        val copia = clonaGiocatore(giocatore)
        var pvCarta = 0.0
        var renditaPotenziale = 0.0

        if (azione is AzioneGiocatore.GiocaCartaRazza) {
            copia.doin -= costoAzione
            val cartaMessa = azione.carta.copy(cani = azione.carta.cani.map { it.copy() }.toMutableList())
            copia.plancia.righe[azione.rigaDestinazione].add(cartaMessa)
            pvCarta = cartaMessa.puntiBase.toDouble()
            renditaPotenziale = cartaMessa.rendita.toDouble()
        }

        // 3. SIMULAZIONE FUTURO
        val upkeepStasera = calcolaUpkeep(copia, evento).costoTotale
        val debitiOggi = (upkeepStasera - copia.doin).coerceAtLeast(0)
        
        copia.doin = (copia.doin - upkeepStasera).coerceAtLeast(0)
        applicaRenditaNetta(copia)
        applicaPopolamentoCarteNuove(copia)
        
        val upkeepDomani = calcolaUpkeep(copia).costoTotale
        val debitiDomani = (upkeepDomani - copia.doin).coerceAtLeast(0)
        val debitiTotali = debitiOggi + debitiDomani

        // 4. PESI RICALIBRATI (SVOLTA STRATEGICA)
        
        // La Rendita è la priorità assoluta (moltiplicatore alto: 15.0)
        val scoreRendita = renditaPotenziale * 15.0
        
        // I Punti Vittoria pesano in base a quanto siamo avanti nella partita
        val progresso = statoGiornata.numero.toDouble() / 15.0
        val scorePV = pvCarta * (5.0 + (progresso * 10.0)) 

        // Il Debito: ora è scalabile. 
        // Fare 1 o 2 debiti non è un dramma (-20 l'uno). Farne 5+ è un disastro (-100 l'uno).
        val malusDebito = if (debitiTotali <= 2) debitiTotali * 20.0 else debitiTotali * 60.0
        
        // Malus debito esistente (meno aggressivo se ho rendita)
        val malusDebitoPregresso = giocatore.debiti * 15.0

        // Bonus cani: ogni cane "fisico" sulla plancia virtuale dà un piccolo bonus (sono futuri cuccioli!)
        val numeroCaniTotali = copia.plancia.righe.flatten().sumOf { it.cani.size }
        val bonusPotenzialeCrescita = numeroCaniTotali * 3.0

        // 5. CALCOLO FINALE
        val score = (copia.doin * 0.8) + 
                    scoreRendita + 
                    scorePV + 
                    bonusPotenzialeCrescita - 
                    malusDebito - 
                    malusDebitoPregresso -
                    ((sogliaSicurezza - copia.doin).coerceAtLeast(0) * pesoRiserva)

        return EsitoValutazioneEconomica(azione, score, debitiTotali, copia.doin)
    }

    fun scegliMigliore(
        statoGiornata: StatoGiornata,
        statoGiocatore: StatoGiocatoreGiornata,
        azioni: List<AzioneGiocatore>,
        sogliaScore: Double,
        sogliaSicurezza: Int,
        pesoRiserva: Double
    ): AzioneGiocatore {
        var migliore: AzioneGiocatore = AzioneGiocatore.Passa
        var punteggioMax = -20000.0
        for (azione in azioni) {
            val esito = valuta(statoGiornata, statoGiocatore, azione, sogliaSicurezza, pesoRiserva)
            if (esito.score > punteggioMax) {
                punteggioMax = esito.score
                migliore = azione
            }
        }
        return if (punteggioMax >= sogliaScore) migliore else AzioneGiocatore.Passa
    }

    // Helper per clonaGiocatore e costoCarta (rimangono uguali a prima)
    private fun costoCartaConEvento(carta: CartaRazza, evento: CartaEvento?): Int {
        var costo = carta.costo
        if (evento?.tipo == TipoEffettoEvento.MODIFICA_COSTO_RAZZA_TUTTE) costo += evento.variazione
        else if (evento?.tipo == TipoEffettoEvento.MODIFICA_COSTO_RAZZA_TAGLIA) {
            if (carta.taglia == evento.tagliaTarget) costo += evento.variazione
            else if (carta.taglia == evento.tagliaTargetSecondaria) costo += evento.variazioneSecondaria
        }
        return if (costo < 0) 0 else costo
    }

    private fun clonaGiocatore(originale: Giocatore): Giocatore {
        val righeCopia = originale.plancia.righe.map { riga ->
            riga.map { it.copy(cani = it.cani.map { c -> c.copy() }.toMutableList()) }.toMutableList()
        }
        return Giocatore(originale.id, originale.doin, originale.debiti, PlanciaGiocatore(righeCopia), originale.mano.toMutableList())
    }
}