#!/bin/bash

MYDIR="$(dirname "$0")"
MYDIR="$(cd "$MYDIR"; pwd)"

if [ -z "$1" ]; then
	echo "usage: $0 <package>"
	echo ""
	exit 1
fi

PACKAGE_NAME="$1"

if [ -n "$GPG_SECRET_KEY" ] && [ -n "$GPG_PASSPHRASE" ]; then
	echo "PGP key found... signing Debian packages"
	. "$MYDIR/configure-signing.sh"
	./makedeb.sh -a -d -s '***REDACTED***' "$PACKAGE_NAME"
else
	echo "PGP key not found... skipping Debian package signing"
	./makedeb.sh -a -d "$PACKAGE_NAME"
fi
