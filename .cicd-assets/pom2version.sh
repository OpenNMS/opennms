#!/usr/bin/env bash
set +e
trap 's=$?; echo >&2 "$0: Error on line "$LINENO": $BASH_COMMAND"; exit $s' ERR

POMFILE="$1"; shift

if [ -z "${POMFILE}" ]; then
  echo "usage: $0 <path to pom.xml>"
  echo ""
  exit 1
fi

MYDIR="$(cd "$(dirname "$0")" || exit 1; pwd)"

# last resort, parse with shell
sed '/<parent>/,/<\/parent>/d' < "${POMFILE}" > "/tmp/$$.pom"
grep '<version>' "/tmp/$$.pom" | head -n 1 | sed -e 's,^.*<version>[	 ]*,,' -e 's,[	 ]*</version>.*$,,'
