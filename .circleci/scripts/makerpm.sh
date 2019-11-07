#!/bin/bash

MYDIR="$(dirname "$0")"
MYDIR="$(cd "$MYDIR"; pwd)"

if [ -z "$1" ]; then
	echo "usage: $0 <specfile>"
	echo ""
	exit 1
fi

SPECFILE="$1"

if [ -n "$GPG_SECRET_KEY" ] && [ -n "$GPG_PASSPHRASE" ]; then
	echo "PGP key found... signing RPMs"
	. "$MYDIR/configure-signing.sh"
	./makerpm.sh -a -d -s '***REDACTED***' -S "$SPECFILE"
else
	echo "PGP key not found... skipping RPM signing"
	./makerpm.sh -a -d -S "$SPECFILE"
fi
