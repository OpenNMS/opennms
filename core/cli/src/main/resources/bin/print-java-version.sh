#!/bin/bash

PRINTJAVADIR="$(dirname "$0")"
PRINTJAVADIR="$(cd "$PRINTJAVADIR" || exit 1; pwd)"

# shellcheck disable=SC1090
if [ -e "${PRINTJAVADIR}/_lib.sh" ]; then
	. "${PRINTJAVADIR}/_lib.sh"
fi

usage() {
	cat <<END
usage: $0 [-h] [-s] <java_home>

	-h   this help
	-s   short output (just the major version)

This script will print the version of the JDK specified on the CLI.

END
}

PRINT_HELP=0
PRINT_SHORT=0

while getopts hs OPT; do
	case "${OPT}" in
		h)
			PRINT_HELP=1
			;;
		s)
			PRINT_SHORT=1
			;;
		*)
			;;
	esac
done
shift $((OPTIND -1))

if [ "$PRINT_HELP" -eq 1 ] || [ -z "$1" ]; then
	usage
	exit 1
fi

java_home="$1"; shift
version="$(__onms_get_java_version_string "$java_home")"

if [ "$PRINT_SHORT" -eq 1 ]; then
	version="$(echo "$version" | cut -d. -f1)"
fi

echo "$version"
