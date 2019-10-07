#!/bin/bash

EXECUTEDIR="$(dirname "$0")"
EXECUTEDIR="$(cd "$EXECUTEDIR" || exit 1; pwd)"

if [ -z "$SHUNITDIR" ]; then
  SHUNITDIR="$EXECUTEDIR"
fi
export SHUNITDIR

TESTFILE="$1"
TESTNAME="$(echo "$TESTFILE" | sed -e 's,^.*/spec/,,' -e 's,.spec.sh$,,')"
DIRNAME="$(dirname "$TESTFILE")"

cd "$DIRNAME" || exit 1

echo "=== running test: $TESTNAME ==="
exec "$TESTFILE"
