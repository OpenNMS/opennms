#!/bin/bash

SHUNITDIR="$(dirname "$0")"
SHUNITDIR="$(cd "$SHUNITDIR" || exit 1; pwd)"

WORKDIR="$1"; shift
if [ -z "$WORKDIR" ]; then
  WORKDIR="$SHUNITDIR"
fi

cd "$WORKDIR" || exit 1

SHELLCHECK="$(command -v shellcheck 2>/dev/null)"

export SHUNITDIR WORKDIR SHELLCHECK

FAILED=false

if [ -n "$SHELLCHECK" ] && [ -x "$SHELLCHECK" ]; then
  # shellcheck disable=SC2035,SC2044
  for FILE in $(find "$WORKDIR" -name \*.sh); do
    echo "=== shellcheck $FILE ==="
    # shellcheck disable=SC2046
    if ! "$SHELLCHECK" "$FILE"; then
      FAILED=true
    fi
  done
fi

# shellcheck disable=SC2044
for FILE in $(find "$WORKDIR" "$SHUNITDIR" -name \*.spec.sh | sort -u); do
  if ! "$SHUNITDIR/execute-test.sh" "$FILE"; then
    FAILED=true
  fi
done

if [ "$FAILED" = true ]; then
  exit 1
fi

exit 0
