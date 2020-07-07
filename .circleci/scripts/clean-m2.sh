#!/bin/bash -e

# keep anything downloaded in the last 14 days
KEEP="+14"

if [ -d "$HOME/.m2/repository" ]; then
  # delete anything older than $KEEP days
  find "$HOME"/.m2/repository -depth -ctime $KEEP -type f -exec rm {} \; >/dev/null

  # delete any directories that are empty (if a directory is not empty, silently continue0
  find "$HOME"/.m2/repository -depth -type d -print | while read -r DIR; do
    rmdir "$DIR" 2>/dev/null || :
  done
fi
