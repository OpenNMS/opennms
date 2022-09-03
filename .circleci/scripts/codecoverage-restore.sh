#!/bin/sh -e
cd ~/project
for file in ~/code-coverage/target-*.zip; do
    FILENAME="$(basename "$file")"
    NUMBER="$(echo "$FILENAME" | cut -d. -f1 | cut -d- -f2-)"
    echo "* unpacking coverage reports $NUMBER:"
    unzip -qo "$file"
    find . -type d -name failsafe-reports -o -name surefire-reports | while read DIR; do
      mkdir -p "${DIR}-${NUMBER}"
      mv "${DIR}"/* "${DIR}-${NUMBER}/"
      rm -rf "${DIR}"
    done
done
