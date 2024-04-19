#!/bin/bash

set -euo pipefail
IFS=$'\n\t'

MYDIR="$(cd "$(dirname "$0")" || exit 1; pwd)"
POM_ARTIFACT="${MYDIR}/pom-artifact.sh"

git grep -l "$@" \
	| sed -E -e 's,src/(main|test)/.*$,pom.xml,' \
	| grep /pom.xml \
	| sort -u \
	| grep -v -E '^pom.xml$' \
	| xargs "${POM_ARTIFACT}" \
	| sort -u \
	| grep -v -E '^ *$' \
	| paste -d, -s -
