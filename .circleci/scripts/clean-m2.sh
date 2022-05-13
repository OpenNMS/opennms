#!/bin/bash -e

# keep anything downloaded in the last 7 days
KEEP="+7"

# remove directory by path (space separated)
PATH_TO_DELETE="org/opennms/integration/api"

if [ -d "$HOME/.m2/repository" ]; then
  # delete anything older than $KEEP days
  find "$HOME"/.m2/repository -depth -ctime $KEEP -type f -exec rm {} \; >/dev/null

  # delete any directories that are empty (if a directory is not empty, silently continue0
  find "$HOME"/.m2/repository -depth -type d -print | while read -r DIR; do
    rmdir "$DIR" 2>/dev/null || :
  done

  # delete dependencies that are highly possible to change
  for P in `echo "$PATH_TO_DELETE"`
  do
    if [ -n "${P}"  ]; then
      find "$HOME"/.m2/repository -path "*${P}" -type d -exec rm -rf {} \;  >/dev/null
    fi
  done
fi
