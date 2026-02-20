package it.contia4zampe.simulator.player.decision

import it.contia4zampe.simulator.engine.StatoGiocatoreGiornata
import it.contia4zampe.simulator.engine.StatoGiornata
import it.contia4zampe.simulator.model.Cane
import it.contia4zampe.simulator.model.CartaRazza
import it.contia4zampe.simulator.model.Giocatore
import it.contia4zampe.simulator.model.PlanciaGiocatore
import it.contia4zampe.simulator.model.TipoEffettoEvento
import it.contia4zampe.simulator.player.AzioneGiocatore
import it.contia4zampe.simulator.rules.applicaPopolamentoCarteNuove
import it.contia4zampe.simulator.rules.applicaRenditaNetta
import it.contia4zampe.simulator.rules.calcolaUpkeep

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
        azione: AzioneGiocatore
    ): EsitoValutazioneEconomica {
        val giocatore = statoGiocatore.giocatore
        val evento = statoGiornata.eventoAttivo

        val costoAzione = when (azione) {
            is AzioneGiocatore.GiocaCartaRazza -> costoCartaConEvento(azione.carta, evento)
            AzioneGiocatore.Passa -> 0
            else -> Int.MAX_VALUE
        }

        if (costoAzione == Int.MAX_VALUE || costoAzione > giocatore.doin) {
            return EsitoValutazioneEconomica(azione, score = -1_000.0, debitiAttesi = Int.MAX_VALUE, doinResiduiPostCosto = -1)
        }

        val copia = clonaGiocatore(giocatore)
        when (azione) {
            is AzioneGiocatore.GiocaCartaRazza -> {
                copia.doin -= costoAzione
                copia.plancia.righe[azione.rigaDestinazione].add(azione.carta.copy(cani = azione.carta.cani.map { it.copy() }.toMutableList()))
            }
            AzioneGiocatore.Passa -> Unit
            else -> Unit
        }

        val doinResiduiPostCosto = copia.doin

        val upkeepCorrente = calcolaUpkeep(copia, evento).costoTotale
        val debitiCorrente = (upkeepCorrente - copia.doin).coerceAtLeast(0)
        val doinDopoUpkeepCorrente = (copia.doin - upkeepCorrente).coerceAtLeast(0)

        copia.doin = doinDopoUpkeepCorrente
        copia.debiti += debitiCorrente

        // Stima sostenibilità prossima giornata, riusando regole economiche esistenti.
        applicaRenditaNetta(copia)
        applicaPopolamentoCarteNuove(copia)

        val upkeepSuccessivo = calcolaUpkeep(copia).costoTotale
        val debitiSuccessivo = (upkeepSuccessivo - copia.doin).coerceAtLeast(0)
        val margineUpkeepCorrente = doinResiduiPostCosto - upkeepCorrente
        val margineUpkeepSuccessivo = copia.doin - upkeepSuccessivo

        val debitiAttesi = debitiCorrente + debitiSuccessivo

        val score =
            (doinResiduiPostCosto * 1.0) +
                (margineUpkeepCorrente * 2.5) +
                (margineUpkeepSuccessivo * 2.0) -
                (debitiAttesi * 12.0)

        return EsitoValutazioneEconomica(
            azione = azione,
            score = score,
            debitiAttesi = debitiAttesi,
            doinResiduiPostCosto = doinResiduiPostCosto
        )
    }

    fun scegliMigliore(
        statoGiornata: StatoGiornata,
        statoGiocatore: StatoGiocatoreGiornata,
        azioni: List<AzioneGiocatore>,
        sogliaScore: Double
    ): AzioneGiocatore {
        val migliore = azioni
            .map { valuta(statoGiornata, statoGiocatore, it) }
            .maxByOrNull { it.score }
            ?: return AzioneGiocatore.Passa

        return if (migliore.score >= sogliaScore) migliore.azione else AzioneGiocatore.Passa
    }

    private fun costoCartaConEvento(carta: CartaRazza, evento: it.contia4zampe.simulator.model.CartaEvento?): Int {
        var costo = carta.costo
        when (evento?.tipo) {
            TipoEffettoEvento.MODIFICA_COSTO_RAZZA_TUTTE -> costo += evento.variazione
            TipoEffettoEvento.MODIFICA_COSTO_RAZZA_TAGLIA -> {
                if (carta.taglia == evento.tagliaTarget) {
                    costo += evento.variazione
                } else if (carta.taglia == evento.tagliaTargetSecondaria) {
                    costo += evento.variazioneSecondaria
                }
            }
            else -> Unit
        }
        return costo.coerceAtLeast(0)
    }

    private fun clonaGiocatore(originale: Giocatore): Giocatore {
        val righeCopia = originale.plancia.righe.map { rigaOriginale ->
            rigaOriginale.map { carta ->
                carta.copy(cani = carta.cani.map { cane -> cane.copy() }.toMutableList())
            }.toMutableList()
        }

        return Giocatore(
            id = originale.id,
            doin = originale.doin,
            debiti = originale.debiti,
            plancia = PlanciaGiocatore(righeCopia),
            mano = originale.mano.map { carta -> carta.copy(cani = carta.cani.map(Cane::copy).toMutableList()) }.toMutableList()
        )
    }
}
