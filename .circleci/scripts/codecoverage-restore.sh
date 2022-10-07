#!/bin/bash

set -e
set -o pipefail

for file in ~/code-coverage/*.zip; do
    FILENAME="$(basename "$file")"
    NUMBER="$(echo "$FILENAME" | cut -d. -f1 | cut -d- -f2-)"
    echo "* unpacking coverage reports $NUMBER:"
    unzip -qo "$file"
    find . -type d '!' -path './.git/*' \( -name failsafe-reports -o -name surefire-reports -o -name jacoco -o -name coverage \) | while read -r DIR; do
      mkdir -p "${DIR}-${NUMBER}"
      mv "${DIR}"/* "${DIR}-${NUMBER}/"
      rm -rf "${DIR}"
    done
done
