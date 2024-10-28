#!/bin/bash -e

# keep anything downloaded in the last 4 days
KEEP="+4"

if [ -d "$HOME/.m2/repository" ]; then
  # delete any org.opennms stuff that might accidentally be cached
  find ~/.m2/repository/org/opennms/* -type d -maxdepth 0 \
    | grep -v -E '/(elasticsearch|extremecomponents|jicmp-api|jicmp6-api|jrrd-api|jrrd2-api|lib|maven|newts)$' \
    | xargs rm -rf

  # delete anything older than $KEEP days
  find "$HOME"/.m2/repository -depth -ctime $KEEP -type f -exec rm {} \; >/dev/null

  # delete any directories that are empty (if a directory is not empty, silently continue
  find "$HOME"/.m2/repository -depth -type d -print | while read -r DIR; do
    rmdir "$DIR" 2>/dev/null || :
  done
fi
