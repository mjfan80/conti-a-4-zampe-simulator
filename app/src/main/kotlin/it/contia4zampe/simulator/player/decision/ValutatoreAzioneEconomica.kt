/* FILE COMPLETO: src/main/kotlin/it/contia4zampe/simulator/player/decision/ValutatoreAzioneEconomica.kt */

package it.contia4zampe.simulator.player.decision

import it.contia4zampe.simulator.engine.StatoGiocatoreGiornata
import it.contia4zampe.simulator.engine.StatoGiornata
import it.contia4zampe.simulator.model.*
import it.contia4zampe.simulator.player.AzioneGiocatore
import it.contia4zampe.simulator.rules.*

// Struttura per contenere i risultati della valutazione
data class EsitoValutazioneEconomica(
    val azione: AzioneGiocatore,
    val score: Double,
    val debitiAttesi: Int,
    val doinResiduiPostCosto: Int
)

object ValutatoreAzioneEconomica {

    /**
     * FUNZIONE DI VALUTAZIONE: Simula l'impatto di un'azione sulla plancia
     */
    fun valuta(
        statoGiornata: StatoGiornata,
        statoGiocatore: StatoGiocatoreGiornata,
        azione: AzioneGiocatore,
        sogliaSicurezza: Int,  // Quanti doin il profilo vuole che restino
        pesoRiserva: Double    // Quanto è grave scendere sotto la soglia (0 = fregatene, 5 = gravissimo)
    ): EsitoValutazioneEconomica {
        val giocatore = statoGiocatore.giocatore
        val evento = statoGiornata.eventoAttivo

        // 1. CALCOLO COSTO REALE (tenendo conto degli eventi)
        var costoAzione = 0
        if (azione is AzioneGiocatore.GiocaCartaRazza) {
            costoAzione = costoCartaConEvento(azione.carta, evento)
        } else {
            costoAzione = 0 
        }

        // Se l'azione costa più di quanto ho, restituisco uno score pessimo
        if (costoAzione > giocatore.doin) {
            return EsitoValutazioneEconomica(azione, score = -1000.0, debitiAttesi = 99, doinResiduiPostCosto = -1)
        }

        // 2. CLONAZIONE (Creo un mondo virtuale per fare i test)
        val copia = clonaGiocatore(giocatore)
        var puntiVittoriaCarta = 0.0
        var renditaCarta = 0.0

        if (azione is AzioneGiocatore.GiocaCartaRazza) {
            copia.doin -= costoAzione
            // Metto la carta sulla plancia virtuale
            val cartaCopia = azione.carta.copy(cani = azione.carta.cani.map { it.copy() }.toMutableList())
            copia.plancia.righe[azione.rigaDestinazione].add(cartaCopia)
            
            puntiVittoriaCarta = cartaCopia.puntiBase.toDouble()
            renditaCarta = cartaMessaDettaglio(cartaCopia).rendita.toDouble() // Usiamo la rendita reale della carta
        }

        // 3. LA TUA LOGICA: CUSCINETTO DI SICUREZZA
        val doinResidui = copia.doin
        var penalitaPoverta = 0.0
        if (doinResidui < sogliaSicurezza) {
            val mancanza = sogliaSicurezza - doinResidui
            penalitaPoverta = mancanza * pesoRiserva
        }

        // 4. SIMULAZIONE DEL FUTURO (OGGI E DOMANI)
        // Calcolo i debiti che farei STASERA
        val upkeepStasera = calcolaUpkeep(copia, evento).costoTotale
        val debitiOggi = (upkeepStasera - copia.doin).coerceAtLeast(0)
        
        // Simulo l'avanzamento alla giornata di domani
        copia.doin = (copia.doin - upkeepStasera).coerceAtLeast(0) // Paga upkeep stasera
        applicaRenditaNetta(copia)        // Incassa rendita domani mattina
        applicaPopolamentoCarteNuove(copia) // Nascono cani domani mattina
        
        // Calcolo i debiti che farei DOMANI SERA
        val upkeepDomani = calcolaUpkeep(copia).costoTotale
        val debitiDomani = (upkeepDomani - copia.doin).coerceAtLeast(0)
        
        val debitiTotaliAttesi = debitiOggi + debitiDomani

        // 5. CALCOLO SCORE FINALE
        val slotOccupati = giocatore.plancia.slotOccupatiTotali()
        val bonusEspansione = (16 - slotOccupati) * 2.0 // Più slot sono vuoti, più voglio giocare
        
        // Se ho molti soldi (> 20), ho meno paura del debito
        val fattorePauraDebito = if (giocatore.doin > 20) 4.0 else 12.0

        val score = (doinResidui * 0.5) + 
                    (puntiVittoriaCarta * 10.0) + // I PV sono molto pesanti!
                    (renditaCarta * 5.0) + 
                    bonusEspansione - 
                    (debitiTotaliAttesi * fattorePauraDebito) - 
                    penalitaPoverta

        return EsitoValutazioneEconomica(azione, score, debitiTotaliAttesi, doinResidui)
    }

    /**
     * SCEGLI MIGLIORE: Valuta tutte le opzioni e prende la migliore sopra la soglia
     */
    fun scegliMigliore(
        statoGiornata: StatoGiornata,
        statoGiocatore: StatoGiocatoreGiornata,
        azioni: List<AzioneGiocatore>,
        sogliaScore: Double,
        sogliaSicurezza: Int,
        pesoRiserva: Double
    ): AzioneGiocatore {
        
        var miglioreAzione: AzioneGiocatore = AzioneGiocatore.Passa
        var punteggioMigliore = -999.0

        for (azione in azioni) {
            val esito = valuta(statoGiornata, statoGiocatore, azione, sogliaSicurezza, pesoRiserva)
            if (esito.score > punteggioMigliore) {
                punteggioMigliore = esito.score
                miglioreAzione = azione
            }
        }

        // Se l'azione migliore non raggiunge la soglia del profilo, preferisco passare
        if (punteggioMigliore >= sogliaScore) {
            return miglioreAzione
        } else {
            return AzioneGiocatore.Passa
        }
    }

    // --- FUNZIONI HELPER (SUPPORTO) ---

    private fun cartaMessaDettaglio(c: CartaRazza) = c // Helper per leggere dati carta

    private fun costoCartaConEvento(carta: CartaRazza, evento: CartaEvento?): Int {
        var costo = carta.costo
        if (evento?.tipo == TipoEffettoEvento.MODIFICA_COSTO_RAZZA_TUTTE) {
            costo += evento.variazione
        } else if (evento?.tipo == TipoEffettoEvento.MODIFICA_COSTO_RAZZA_TAGLIA) {
            if (carta.taglia == evento.tagliaTarget) costo += evento.variazione
            else if (carta.taglia == evento.tagliaTargetSecondaria) costo += evento.variazioneSecondaria
        }
        return if (costo < 0) 0 else costo
    }

    private fun clonaGiocatore(originale: Giocatore): Giocatore {
        val righeCopia = originale.plancia.righe.map { riga ->
            riga.map { carta ->
                carta.copy(cani = carta.cani.map { it.copy() }.toMutableList())
            }.toMutableList()
        }
        return Giocatore(
            id = originale.id,
            doin = originale.doin,
            debiti = originale.debiti,
            plancia = PlanciaGiocatore(righeCopia),
            mano = originale.mano.toMutableList()
        )
    }
}