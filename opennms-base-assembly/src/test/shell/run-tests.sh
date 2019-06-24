#!/bin/bash

MYDIR="$(dirname "$0")"
MYDIR="$(cd "$MYDIR" || exit 1; pwd)"

cd "$MYDIR" || exit 1

SHELLCHECK="$(command -v shellcheck 2>/dev/null)"
export SHELLCHECK

FAILED=false

if [ -n "$SHELLCHECK" ] && [ -x "$SHELLCHECK" ]; then
  # shellcheck disable=SC2035,SC2044
  for FILE in $(find * -name \*.sh); do
    echo "=== shellcheck $FILE ==="
    # shellcheck disable=SC2046
    if ! "$SHELLCHECK" "$FILE"; then
      FAILED=true
    fi
  done
fi

# shellcheck disable=SC2044
for FILE in $(find "$MYDIR" -name \*.spec.sh); do
  if ! "$MYDIR/execute-test.sh" "$FILE"; then
    FAILED=true
  fi
done

if [ "$FAILED" = true ]; then
  exit 1
fi

exit 0
