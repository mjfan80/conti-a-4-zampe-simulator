package it.contia4zampe.simulator.player

import it.contia4zampe.simulator.engine.StatoGiornata
import it.contia4zampe.simulator.engine.StatoGiocatoreGiornata
import it.contia4zampe.simulator.model.*
import it.contia4zampe.simulator.rules.calcolaRenditaNetta
import it.contia4zampe.simulator.rules.calcolaUpkeep

interface PlayerProfile {

    fun decidiAzione(statoGiornata: StatoGiornata, statoGiocatore: StatoGiocatoreGiornata): AzioneGiocatore

    fun decidiGestioneCucciolo(sg: StatoGiocatoreGiornata, cucciolo: Cane): SceltaCucciolo {
        if (sg.giocatore.debiti > 0) return SceltaCucciolo.VENDI
        return SceltaCucciolo.TRASFORMA_IN_ADULTO
    }

    fun scegliCartaDalMercato(giocatore: StatoGiocatoreGiornata, mercato: List<CartaRazza>): CartaRazza {
        return mercato.first()
    }

    fun vuoleDichiarareAccoppiamento(sg: StatoGiocatoreGiornata, carta: CartaRazza): Boolean {
        return sg.giocatore.debiti < 3 // Regola base di buon senso
    }

    // --- HELPER DI SOPRAVVIVENZA (Usati dai profili) ---

    fun controllaUrgenzaVendita(sg: StatoGiocatoreGiornata, statoGiornata: StatoGiornata): AzioneGiocatore.VendiCani? {
        val g = sg.giocatore
        val rendita = calcolaRenditaNetta(g)
        val upkeep = calcolaUpkeep(g, statoGiornata.eventoAttivo).costoTotale

        // Se stasera non posso pagare l'upkeep nemmeno con la rendita, devo vendere un cane ORA
        if (g.doin + rendita < upkeep) {
            val cane = trovaCanePerEmergenza(g)
            if (cane != null) return AzioneGiocatore.VendiCani(listOf(cane))
        }
        return null
    }

    fun trovaCanePerEmergenza(g: Giocatore): DettaglioVendita? {
        for (riga in g.plancia.righe) {
            for (carta in riga) {
                val cane = carta.cani.firstOrNull { it.stato == StatoCane.ADULTO || it.stato == StatoCane.ADULTO_ADDESTRATO }
                if (cane != null) return DettaglioVendita(carta, cane)
            }
        }
        return null
    }

    fun cercaAzioneAddestramento(g: Giocatore): AzioneSecondaria.AddestraCane? {
        for (riga in g.plancia.righe) {
            for (carta in riga) {
                val adultiPresenti = carta.cani.count {
                    it.stato == StatoCane.ADULTO || it.stato == StatoCane.ADULTO_ADDESTRATO ||
                            it.stato == StatoCane.IN_ACCOPPIAMENTO || it.stato == StatoCane.IN_ADDESTRAMENTO
                }
                val haSlot = g.plancia.haSlotAddestramentoDisponibilePerCarta(carta)
                val caneLibero = carta.cani.firstOrNull { it.stato == StatoCane.ADULTO }
                if (adultiPresenti >= 3 && haSlot && caneLibero != null) {
                    return AzioneSecondaria.AddestraCane(carta, caneLibero)
                }
            }
        }
        return null
    }

    fun generaGiocatePossibili(g: Giocatore): List<AzioneGiocatore> {
        val lista = mutableListOf<AzioneGiocatore>(AzioneGiocatore.Passa)
        for (carta in g.mano) {
            for (r in 0 until g.plancia.righe.size) {
                if (g.plancia.puoOspitareTaglia(r, carta.taglia) && g.plancia.haSpazioInRiga(r)) {
                    lista.add(AzioneGiocatore.GiocaCartaRazza(carta, r, g.plancia.righe[r].size))
                }
            }
        }
        return lista
    }
}