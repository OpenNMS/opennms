#!/bin/bash

VERSION_BUILD_VERSION_COMPARE='1.2'
PACKAGES="$PACKAGES VERSION_COMPARE"

# format_version()
#   input : version number
#   output: a cleaned up version number for comparisons
#
#   (called from check_version())

format_version () {
	[ -z "$1" ] && echo "format_version: usage: format_version 7.0.3-2" && return 1
 
	local RELEASE_STRING=''
	local VERSION_STRING=`echo $1 | sed -e 's#\.##g' | sed -e 's#-.*$##g'`
	VERSION_STRING=`echo "${VERSION_STRING}000" | sed -e 's/^\(...\).*$/\1/'`
	if echo "$1" | grep -- - >/dev/null 2>&1; then
		RELEASE_STRING=`echo $1 | sed -e 's#^.*\-##g' | sed -e 's#[[:alpha:]]*$##'`
	else
		RELEASE_STRING=1
	fi
	[ -z "$RELEASE_STRING" ] && RELEASE_STRING=1
	#RELEASE_STRING=`echo "000${RELEASE_STRING}" | sed -e 's/^.*\(..........\)$/\1/'`
	echo "${VERSION_STRING}-${RELEASE_STRING}"
}
 
# check_version()
#   input : 2 version number strings
#   output: compare two version numbers, return true
#	   if the second is newer, false if the
#	   first is newer

check_version () {
	[ -z "$2" ] && return 1
 
	local VERSION_FROM=`format_version $1`
	local VERSION_TO=`format_version $2`
 
	local NEWER=`echo -e "${VERSION_FROM}\n${VERSION_TO}" | sort -r | head -1`
	if [ "$NEWER" = "$VERSION_FROM" ]; then
		return
	else
		return 1
	fi
}
