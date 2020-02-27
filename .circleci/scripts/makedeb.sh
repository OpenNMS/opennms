#!/bin/bash

MYDIR="$(dirname "$0")"
MYDIR="$(cd "$MYDIR" || exit 1; pwd)"

if [ -z "$1" ]; then
	echo "usage: $0 <package>"
	echo ""
	exit 1
fi

PACKAGE_NAME="$1"

# shellcheck disable=SC1090
. "${MYDIR}/lib.sh"

"$MYDIR/configure-signing.sh"

./makedeb.sh -a -d -M "${ONMS_MAJOR_REVISION}" -m "${ONMS_MINOR_REVISION}" -u "${ONMS_MICRO_REVISION}" "$PACKAGE_NAME" || exit 1

mkdir -p target/debs
mv ../*.deb ../*.changes target/debs/
