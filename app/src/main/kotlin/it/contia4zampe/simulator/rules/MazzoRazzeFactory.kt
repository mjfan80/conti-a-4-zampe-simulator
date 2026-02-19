package it.contia4zampe.simulator.rules

import it.contia4zampe.simulator.model.*

/**
 * Factory per la creazione del mazzo completo delle Carte Razza.
 * I dati sono tratti dal database ufficiale del gioco.
 */
fun creaMazzoRazzeBase(): MutableList<CartaRazza> {
    val mazzo = mutableListOf<CartaRazza>()

    // TAGLIA PICCOLA
    val piccole = listOf(
        CartaRazza("Chihuahua", 3, 1, 3, 4, Taglia.PICCOLA),
        CartaRazza("Bulldog Francese", 5, 3, 3, 5, Taglia.PICCOLA),
        CartaRazza("Jack Russell Terrier", 4, 2, 4, 6, Taglia.PICCOLA),
        CartaRazza("Yorkshire Terrier", 4, 1, 5, 7, Taglia.PICCOLA, effettoFine = EffettoFinePartita.BONUS_1PV_ADULTO),
        CartaRazza("Pinscher", 4, 2, 3, 5, Taglia.PICCOLA),
        CartaRazza("Bassotto Tedesco", 4, 1, 5, 7, Taglia.PICCOLA),
        CartaRazza("Bolognese", 3, 1, 4, 5, Taglia.PICCOLA),
        CartaRazza("Carlino", 4, 1, 6, 8, Taglia.PICCOLA),
        CartaRazza("Volpino Italiano", 4, 1, 4, 6, Taglia.PICCOLA),
        CartaRazza("Skye Terrier", 4, 1, 4, 6, Taglia.PICCOLA),
        CartaRazza("Scottish Terrier", 4, 1, 5, 7, Taglia.PICCOLA, effettoFine = EffettoFinePartita.BONUS_2PV_COPPIA_ADULTI),
        CartaRazza("Spitz Tedesco", 4, 2, 3, 5, Taglia.PICCOLA),
        CartaRazza("Barboncino", 4, 1, 5, 9, Taglia.PICCOLA),
        CartaRazza("Cavalier King Charles", 4, 1, 5, 7, Taglia.PICCOLA),
        CartaRazza("Papillon", 4, 1, 5, 8, Taglia.PICCOLA)
    )

    // TAGLIA MEDIA
    val medie = listOf(
        CartaRazza("Labrador", 6, 2, 5, 7, Taglia.MEDIA, effettoInizio = EffettoInizioGiornata.DOIN1_RENDITA),
        CartaRazza("Golden Retriever", 6, 2, 6, 8, Taglia.MEDIA),
        CartaRazza("Border Collie", 6, 2, 5, 9, Taglia.MEDIA),
        CartaRazza("Beagle", 5, 1, 4, 6, Taglia.MEDIA),
        CartaRazza("Bulldog Inglese", 6, 3, 5, 7, Taglia.MEDIA),
        CartaRazza("Shetland Sheepdog", 5, 2, 5, 8, Taglia.MEDIA),
        CartaRazza("Segugio Maremmano", 5, 1, 5, 7, Taglia.MEDIA),
        CartaRazza("Segugio dei Balcani", 5, 1, 4, 6, Taglia.MEDIA),
        CartaRazza("Hokkaido", 6, 2, 7, 9, Taglia.MEDIA),
        CartaRazza("Kai", 5, 1, 5, 7, Taglia.MEDIA),
        CartaRazza("Kishu", 6, 1, 6, 8, Taglia.MEDIA),
        CartaRazza("Canaan Dog", 5, 2, 5, 7, Taglia.MEDIA),
        CartaRazza("Dalmata", 6, 2, 6, 9, Taglia.MEDIA),
        CartaRazza("Basset Hound", 5, 1, 6, 8, Taglia.MEDIA),
        CartaRazza("Drever", 4, 1, 4, 6, Taglia.MEDIA),
        CartaRazza("Siberian Husky", 9, 2, 5, 8, Taglia.MEDIA),
        CartaRazza("Shiba Inu", 5, 1, 6, 8, Taglia.MEDIA),
        CartaRazza("Australian Shepherd", 6, 2, 4, 6, Taglia.MEDIA),
        CartaRazza("Cocker Spaniel Inglese", 5, 2, 5, 7, Taglia.MEDIA),
        CartaRazza("Lagotto Romagnolo", 6, 2, 6, 9, Taglia.MEDIA),
        CartaRazza("Nova Scotia Duck Tolling", 6, 2, 6, 8, Taglia.MEDIA),
        CartaRazza("Whippet", 5, 1, 6, 8, Taglia.MEDIA)
    )

    // TAGLIA GRANDE
    val grandi = listOf(
        CartaRazza("Pastore Tedesco", 7, 2, 7, 10, Taglia.GRANDE, effettoInizio = EffettoInizioGiornata.DOIN1_RENDITA_UNICORIGA),
        CartaRazza("Cane Corso", 8, 2, 8, 11, Taglia.GRANDE),
        CartaRazza("Boxer", 7, 3, 6, 9, Taglia.GRANDE),
        CartaRazza("American Bulldog", 7, 2, 6, 9, Taglia.GRANDE),
        CartaRazza("San Bernardo", 9, 1, 10, 14, Taglia.GRANDE),
        CartaRazza("Terranova", 8, 2, 8, 11, Taglia.GRANDE),
        CartaRazza("Cane da Montagna dei Pirenei", 8, 2, 7, 10, Taglia.GRANDE),
        CartaRazza("Pastore Maremmano", 7, 2, 7, 10, Taglia.GRANDE),
        CartaRazza("Pastore Bergamasco", 7, 2, 6, 9, Taglia.GRANDE),
        CartaRazza("Pastore della Sila", 6, 2, 5, 7, Taglia.GRANDE),
        CartaRazza("Bracco Italiano", 6, 2, 6, 8, Taglia.GRANDE),
        CartaRazza("Bracco Ungherese a Pelo Duro", 6, 2, 7, 9, Taglia.GRANDE),
        CartaRazza("Groenlandese", 7, 2, 8, 11, Taglia.GRANDE),
        CartaRazza("Foxhound", 6, 2, 5, 8, Taglia.GRANDE),
        // Levriero Afgano: costo 7, rendita 1, PV 9/13
        CartaRazza("Levriero Afgano", 7, 1, 9, 13, Taglia.GRANDE),
        CartaRazza("Levriero Polacco", 6, 2, 7, 10, Taglia.GRANDE),
        CartaRazza("Alano", 8, 2, 9, 12, Taglia.GRANDE),
        CartaRazza("Rottweiler", 8, 3, 7, 10, Taglia.GRANDE),
        CartaRazza("Dobermann", 7, 2, 7, 10, Taglia.GRANDE),
        CartaRazza("Bovaro del Bernese", 8, 2, 8, 11, Taglia.GRANDE)
    )

    // Uniamo tutto e aggiungiamo 2 copie per ogni razza per un mazzo corposo
    val tutte = piccole + medie + grandi
    for (r in tutte) {
        repeat(2) {
            // Usiamo copy() per creare istanze distinte
            mazzo.add(r.copy(cani = mutableListOf()))
        }
    }

    mazzo.shuffle()
    return mazzo
}