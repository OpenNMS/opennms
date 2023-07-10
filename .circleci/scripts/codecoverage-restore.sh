#!/bin/bash

set -e
set -o pipefail

find ~/code-coverage -type f -name \*.zip | sort -u | while read -r file; do
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
