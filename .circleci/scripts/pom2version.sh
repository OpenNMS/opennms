#!/bin/sh

POMFILE="$1"; shift

if [ -z "${POMFILE}" ]; then
  echo "usage: $0 <path to pom.xml>"
  echo ""
  exit 1
fi

MYDIR="$(cd "$(dirname "$0")" || exit 1; pwd)"

set +e

XMLLINT="$(command -v xmllint || :)"
PYTHON="$(command -v python || command -v python2 || command -v python3 || :)"

# try xmllint first, we can xpath it
if [ -n "${XMLLINT}" ] && [ -x "${XMLLINT}" ]; then
  exec "${XMLLINT}" --xpath '/*[local-name()="project"]/*[local-name()="version"]/text()' "${POMFILE}"
fi

# fall back to python XML parsing
if [ -n "${PYTHON}" ] && [ -x "${PYTHON}" ]; then
  "${PYTHON}" "${MYDIR}/../../opennms-container/pom2version.py" "${POMFILE}" 2>/dev/null && exit 0
fi

# last resort, parse with shell
sed '/<parent>/,/<\/parent>/d' < "${POMFILE}" > "/tmp/$$.pom"
grep '<version>' "/tmp/$$.pom" | head -n 1 | sed -e 's,^.*<version>[	 ]*,,' -e 's,[	 ]*</version>.*$,,'
