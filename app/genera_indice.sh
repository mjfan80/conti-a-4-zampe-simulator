#!/bin/bash

REPO_URL="https://github.com/mjfan80/conti-a-4-zampe-simulator"
BRANCH="main"
SRC_ROOT="app/src/main/kotlin"

OUTPUT="INDEX.md"

echo "# Indice codice – Conti a 4 Zampe" > "$OUTPUT"
echo "" >> "$OUTPUT"

find "$SRC_ROOT" -name "*.kt" | sort | while read -r file; do
  # path relativo al repo
  REL_PATH="${file#./}"

  # URL GitHub corretto
  URL="$REPO_URL/blob/$BRANCH/$REL_PATH"

  CLASS=$(basename "$file" .kt)
  DIR=$(dirname "$REL_PATH")

  echo "## $DIR" >> "$OUTPUT"
  echo "- $CLASS" >> "$OUTPUT"
  echo "  $URL" >> "$OUTPUT"
  echo "" >> "$OUTPUT"
done
