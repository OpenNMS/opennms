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

if [ -n "$GPG_SECRET_KEY" ] && [ -n "$GPG_PASSPHRASE" ]; then
	echo "PGP key found... signing RPMs"
	# shellcheck disable=SC1090
	. "$MYDIR/configure-signing.sh"
	./makerpm.sh -a -d -s '***REDACTED***' -m "${ONMS_MINOR_REVISION}" -u "${ONMS_MICRO_REVISION}" -S "$SPECFILE" || exit 1
else
	echo "PGP key not found... skipping RPM signing"
	./makerpm.sh -a -d -m "${ONMS_MINOR_REVISION}" -u "${ONMS_MICRO_REVISION}" -S "$SPECFILE" || exit 1
fi
