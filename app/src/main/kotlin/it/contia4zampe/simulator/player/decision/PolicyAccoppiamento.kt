package it.contia4zampe.simulator.player.decision

import it.contia4zampe.simulator.engine.StatoGiocatoreGiornata
import it.contia4zampe.simulator.model.CartaRazza
import it.contia4zampe.simulator.rules.stimaEconomiaDueGiornateConAccoppiamento

data class PolicyAccoppiamentoConfig(
    val sogliaDebitiMassima: Int,
    val margineDoinMinimoPostUpkeep: Int,
    val consentiPeggioramentoDebiti: Boolean,
    val tolleranzaRiduzioneDoin: Int
)

object PolicyAccoppiamento {
    fun dovrebbeDichiarare(
        statoGiocatore: StatoGiocatoreGiornata,
        carta: CartaRazza,
        config: PolicyAccoppiamentoConfig
    ): Boolean {
        val esitoSenza = stimaEconomiaDueGiornateConAccoppiamento(statoGiocatore, carta, dichiaraAccoppiamento = false)
        val esitoCon = stimaEconomiaDueGiornateConAccoppiamento(statoGiocatore, carta, dichiaraAccoppiamento = true)

        if (esitoCon.debitiFinali > config.sogliaDebitiMassima) return false
        if (!config.consentiPeggioramentoDebiti && esitoCon.debitiFinali > esitoSenza.debitiFinali) return false
        if (esitoCon.doinFinali < config.margineDoinMinimoPostUpkeep) return false

        val perditaLiquidita = esitoSenza.doinFinali - esitoCon.doinFinali
        if (perditaLiquidita > config.tolleranzaRiduzioneDoin) return false

        return true
    }
}

