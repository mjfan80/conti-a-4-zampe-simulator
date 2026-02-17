#!/bin/bash

OUTPUT_FILE="repository_context.txt"

# Svuota il file se esiste
> "$OUTPUT_FILE"

echo "==================================================" >> "$OUTPUT_FILE"
echo "STRUTTURA DEL REPOSITORY (Cartella src)" >> "$OUTPUT_FILE"
echo "Data generazione: $(date)" >> "$OUTPUT_FILE"
echo "==================================================" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"

# Crea l'indice dei file
find src -name "*.kt" >> "$OUTPUT_FILE"

echo "" >> "$OUTPUT_FILE"
echo "==================================================" >> "$OUTPUT_FILE"
echo "INIZIO CODICE SORGENTE" >> "$OUTPUT_FILE"
echo "==================================================" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"

# Ciclo per trovare tutti i file .kt e leggerli
find src -name "*.kt" -type f | while read -r file; do
    echo "[INIZIO FILE: $file]" >> "$OUTPUT_FILE"
    echo "--------------------------------------------------" >> "$OUTPUT_FILE"
    cat "$file" >> "$OUTPUT_FILE"
    echo "" >> "$OUTPUT_FILE"
    echo "[FINE FILE: $file]" >> "$OUTPUT_FILE"
    echo "" >> "$OUTPUT_FILE"
    echo "==================================================" >> "$OUTPUT_FILE"
    echo "" >> "$OUTPUT_FILE"
done

echo "Operazione completata! Scarica $OUTPUT_FILE e caricalo su AI Studio."