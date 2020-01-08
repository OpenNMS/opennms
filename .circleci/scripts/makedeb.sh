#!/bin/bash

MYDIR="$(dirname "$0")"
MYDIR="$(cd "$MYDIR" || exit 1; pwd)"

if [ -z "$1" ]; then
	echo "usage: $0 <package>"
	echo ""
	exit 1
fi

PACKAGE_NAME="$1"

if [ -n "$GPG_SECRET_KEY" ] && [ -n "$GPG_PASSPHRASE" ]; then
	echo "PGP key found... signing Debian packages"
	# shellcheck disable=SC1090
	. "$MYDIR/configure-signing.sh"
	./makedeb.sh -a -d -s '***REDACTED***' "$PACKAGE_NAME" || exit 1
else
	echo "PGP key not found... skipping Debian package signing"
	./makedeb.sh -a -d "$PACKAGE_NAME" || exit 1
fi

mkdir -p target/debs
mv ../*.deb ../*.changes target/debs/
