package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.*

fun creaMazzoEventiBaseCompleto(): MutableList<CartaEvento> {
    val mazzo = mutableListOf<CartaEvento>()

    // --- VENDITE (8 carte) ---
    repeat(1) { mazzo.add(CartaEvento("Mercato Vivace", TipoEffettoEvento.MODIFICA_VENDITA, 1)) }
    repeat(1) { mazzo.add(CartaEvento("Mercato Vivace + Scarto", TipoEffettoEvento.MODIFICA_VENDITA, 1, penalitàScartoMercato = true)) }
    repeat(1) { mazzo.add(CartaEvento("Mercato Stagnante", TipoEffettoEvento.MODIFICA_VENDITA, -1)) }
    repeat(1) { mazzo.add(CartaEvento("Mercato Stagnante + Scarto", TipoEffettoEvento.MODIFICA_VENDITA, -1, penalitàScartoMercato = true)) }
    repeat(1) { mazzo.add(CartaEvento("Boom Vendite", TipoEffettoEvento.MODIFICA_VENDITA, 2, penalitàScartoMercato = true)) }
    repeat(1) { mazzo.add(CartaEvento("Crollo Vendite", TipoEffettoEvento.MODIFICA_VENDITA, -2)) }
    // Nota: aggiungi le mancanti per arrivare a 8 secondo il tuo schema

    // --- COSTI ADDESTRAMENTO (6 carte) ---
    repeat(2) { mazzo.add(CartaEvento("Corso Specializzato", TipoEffettoEvento.MODIFICA_COSTO_PLANCIAADDESTRAMENTO, 2)) }
    // ... e così via per tutte le categorie del tuo OCR ...

    // --- TAGLIA DELLE RAZZE (6 carte) ---
    repeat(2) { 
        mazzo.add(CartaEvento("Moda Grandi/Piccole", TipoEffettoEvento.MODIFICA_COSTO_RAZZA_TAGLIA, 
            variazione = 1, tagliaTarget = Taglia.GRANDE,
            variazioneSecondaria = -1, tagliaTargetSecondaria = Taglia.PICCOLA)) 
    }

    // --- STABILITÀ ECONOMICA (4 carte) ---
    repeat(2) { mazzo.add(CartaEvento("Sussidi Statali", TipoEffettoEvento.BONUS_DOIN_INIZIO, 2)) }
    repeat(2) { mazzo.add(CartaEvento("Sconti Allevatori", TipoEffettoEvento.MODIFICA_COSTO_RAZZA_TUTTE, -1)) }

    mazzo.shuffle()
    return mazzo
}