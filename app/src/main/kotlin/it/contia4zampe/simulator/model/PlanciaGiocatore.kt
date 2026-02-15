package it.contia4zampe.simulator.model

data class MiniPlanciaAddestramento(
    val indiceRiga: Int,
    val slotSinistro: Int,
    var slotSinistroOccupato: Boolean = false,
    var slotDestroOccupato: Boolean = false
) {
    fun copreSlot(indiceRigaCarta: Int, indiceSlotCarta: Int): Boolean {
        if (indiceRiga != indiceRigaCarta) return false
        return indiceSlotCarta == slotSinistro || indiceSlotCarta == (slotSinistro + 1)
    }

    fun haSpazioLiberoPerSlot(indiceSlotCarta: Int): Boolean {
        return when (indiceSlotCarta) {
            slotSinistro -> !slotSinistroOccupato
            slotSinistro + 1 -> !slotDestroOccupato
            else -> false
        }
    }

    fun occupaSlot(indiceSlotCarta: Int): Boolean {
        if (!haSpazioLiberoPerSlot(indiceSlotCarta)) return false

        return when (indiceSlotCarta) {
            slotSinistro -> {
                slotSinistroOccupato = true
                true
            }

            slotSinistro + 1 -> {
                slotDestroOccupato = true
                true
            }

            else -> false
        }
    }

    fun liberaSlot(indiceSlotCarta: Int): Boolean {
        return when (indiceSlotCarta) {
            slotSinistro -> {
                if (!slotSinistroOccupato) return false
                slotSinistroOccupato = false
                true
            }

            slotSinistro + 1 -> {
                if (!slotDestroOccupato) return false
                slotDestroOccupato = false
                true
            }

            else -> false
        }
    }
}

data class PlanciaGiocatore(
    val righe: List<MutableList<CartaRazza>>,
    val miniPlanceAddestramento: MutableList<MiniPlanciaAddestramento> = mutableListOf()
) {
    companion object {
        // Convenzione indici righe: 0 = basso, 1 = medio, 2 = alto
        const val RIGA_BASSA: Int = 0
        const val RIGA_MEDIA: Int = 1
        const val RIGA_ALTA: Int = 2
    }

    fun capacitaRiga(indiceRiga: Int): Int {
        return when (indiceRiga) {
            RIGA_BASSA, RIGA_MEDIA -> 6
            RIGA_ALTA -> 4
            else -> 0
        }
    }

    fun puoOspitareTaglia(indiceRiga: Int, taglia: Taglia): Boolean {
        return when (indiceRiga) {
            RIGA_BASSA -> taglia == Taglia.MEDIA || taglia == Taglia.GRANDE
            RIGA_MEDIA -> taglia == Taglia.PICCOLA || taglia == Taglia.MEDIA
            RIGA_ALTA -> true
            else -> false
        }
    }

    fun haSpazioInRiga(indiceRiga: Int): Boolean {
        if (indiceRiga !in righe.indices) return false
        return righe[indiceRiga].size < capacitaRiga(indiceRiga)
    }

    fun slotOccupatiTotali(): Int {
        return righe.sumOf { it.size }
    }

    fun capacitaTotale(): Int {
        var totale = 0
        for (indice in righe.indices) {
            totale += capacitaRiga(indice)
        }
        return totale
    }

    fun isPiena(): Boolean {
        if (righe.isEmpty()) return false
        return slotOccupatiTotali() >= capacitaTotale()
    }

    /**
     * Valida se una mini-plancia addestramento può essere messa su una coppia di slot adiacenti.
     *
     * Regole:
     * - Riga bassa e media: coppie strutturali (0-1), (2-3), (4-5)
     * - Riga alta: solo coppia centrale (1-2)
     */
    fun coppiaAddestramentoValida(indiceRiga: Int, slotSinistro: Int): Boolean {
        return when (indiceRiga) {
            RIGA_BASSA, RIGA_MEDIA -> slotSinistro == 0 || slotSinistro == 2 || slotSinistro == 4
            RIGA_ALTA -> slotSinistro == 1
            else -> false
        }
    }

    fun slotAddestrabile(indiceRiga: Int, indiceSlot: Int): Boolean {
        return indiceRiga == RIGA_ALTA && (indiceSlot == 1 || indiceSlot == 2)
    }

    fun puòAcquistareMiniPlancia(indiceRiga: Int, slotSinistro: Int): Boolean {
        if (indiceRiga !in righe.indices) return false
        if (!coppiaAddestramentoValida(indiceRiga, slotSinistro)) return false
        if (righe[indiceRiga].size <= slotSinistro + 1) return false // servono 2 carte adiacenti

        return miniPlanceAddestramento.none {
            it.indiceRiga == indiceRiga && it.slotSinistro == slotSinistro
        }
    }

    fun acquistaMiniPlancia(indiceRiga: Int, slotSinistro: Int): Boolean {
        if (!puòAcquistareMiniPlancia(indiceRiga, slotSinistro)) return false
        miniPlanceAddestramento.add(MiniPlanciaAddestramento(indiceRiga, slotSinistro))
        return true
    }

    fun haSlotAddestramentoDisponibilePerCarta(carta: CartaRazza): Boolean {
        val posizione = trovaPosizioneCarta(carta) ?: return false
        return miniPlanceAddestramento.any { mini ->
            mini.copreSlot(posizione.first, posizione.second) && mini.haSpazioLiberoPerSlot(posizione.second)
        }
    }

    fun occupaSlotAddestramentoPerCarta(carta: CartaRazza): Boolean {
        val posizione = trovaPosizioneCarta(carta) ?: return false

        val mini = miniPlanceAddestramento.firstOrNull {
            it.copreSlot(posizione.first, posizione.second) && it.haSpazioLiberoPerSlot(posizione.second)
        } ?: return false

        return mini.occupaSlot(posizione.second)
    }

    fun liberaUnoSlotAddestramentoPerCarta(carta: CartaRazza): Boolean {
        val posizione = trovaPosizioneCarta(carta) ?: return false

        val mini = miniPlanceAddestramento.firstOrNull {
            it.copreSlot(posizione.first, posizione.second)
        } ?: return false

        return mini.liberaSlot(posizione.second)
    }

    private fun trovaPosizioneCarta(carta: CartaRazza): Pair<Int, Int>? {
        for (indiceRiga in righe.indices) {
            val indiceSlot = righe[indiceRiga].indexOf(carta)
            if (indiceSlot >= 0) {
                return indiceRiga to indiceSlot
            }
        }
        return null
    }
}
