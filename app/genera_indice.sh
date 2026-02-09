#!/bin/bash

BASE_URL="https://github.com/mjfan80/conti-a-4-zampe-simulator/tree/main/app/src"

echo "# Indice codice – Conti a 4 Zampe" > INDEX.md
echo "" >> INDEX.md

find app/src/main/kotlin -name "*.kt" | sort | while read file; do
  CLASS=$(basename "$file" .kt)
  DIR=$(dirname "$file")
  URL="$BASE_URL/$file"

  echo "## $DIR" >> INDEX.md
  echo "- $CLASS" >> INDEX.md
  echo "  $URL" >> INDEX.md
  echo "" >> INDEX.md
done
