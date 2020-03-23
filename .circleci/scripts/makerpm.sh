#!/bin/bash

MYDIR="$(dirname "$0")"
MYDIR="$(cd "$MYDIR" || exit 1; pwd)"

if [ -z "$1" ]; then
	echo "usage: $0 <specfile>"
	echo ""
	exit 1
fi

SPECFILE="$1"

# shellcheck disable=SC1090
. "${MYDIR}/lib.sh"

"$MYDIR/configure-signing.sh"

./makerpm.sh -a -d -M "${ONMS_MAJOR_REVISION}" -m "${ONMS_MINOR_REVISION}" -u "${ONMS_MICRO_REVISION}" -S "$SPECFILE" || exit 1
