#!/bin/bash

VERSION_BUILD_FIND_JARFILE='1.2'
PACKAGES="$PACKAGES FIND_JARFILE"
JAR_SEARCH_PATH="$OPENNMS_HOME/lib/updates $PREFIX/lib $OPENNMS_HOME/lib /usr/share/java"

# set_jarfile ()
#   input : jarfile name, one or more directories
#   output: sets an environment variable that is the
#           upper-case version of the jarfile name

set_jarfile () {
	local JARFILE="$1"
	shift
 
	local UCASE=`echo ${JARFILE} | tr '[:lower:]' '[:upper:]'`
	local JARLOC=`find_jarfile $JARFILE`
	if [ -z "$JARLOC" ]; then
		return 1
	else
		eval "$UCASE=\"$JARLOC\""
		return
	fi
}

find_jarfile () {
	local JARFILE="$1"
	shift

	if [ -z "$@" ]; then
		for dir in $JAR_SEARCH_PATH; do
			if [ -f "$dir/${JARFILE}.jar" ]; then
				echo "${dir}/${JARFILE}.jar"
				return
			fi
		done
	else
		for dir in "$@"; do
			if [ -f "$dir/${JARFILE}.jar" ]; then
				echo "${dir}/${JARFILE}.jar"
				return
			fi
		done
	fi

	return 1
}
