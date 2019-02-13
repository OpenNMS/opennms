#!/bin/bash

TESTFILE="$1"
TESTNAME="$(echo "$TESTFILE" | sed -e 's,^.*/spec/,,' -e 's,.spec.sh$,,')"
DIRNAME="$(dirname "$TESTFILE")"

cd "$DIRNAME" || exit 1

echo "=== running test: $TESTNAME ==="
exec "$TESTFILE"