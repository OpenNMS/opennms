#!/bin/bash

set -e

ARGS=("-Prun-expensive-tasks")

if [ -f "$OPENNMS_SETTINGS_XML" ]; then
  ARGS+=(-s "$OPENNMS_SETTINGS_XML")
fi

if [ -z "$OPENNMS_ENABLE_SNAPSHOTS" ] || [ "$OPENNMS_ENABLE_SNAPSHOTS" = 1 ]; then
  ARGS+=(-Denable.snapshots=true -DupdatePolicy=always)
else
  ARGS+=(-Denable.snapshots=false -DupdatePolicy=never)
fi

case "${CIRCLE_BRANCH}" in
  "master"*|"release-"*|develop)
    ARGS+=(-Dbuild.type=production)
  ;;
esac

echo "${ARGS[*]}"
