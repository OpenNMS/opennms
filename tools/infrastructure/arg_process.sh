#!/bin/bash

VERSION_BUILD_ARG_PROCESS='1.1.1.1'
PACKAGES="$PACKAGES ARG_PROCESS"

# check_arg ()
#   input : an argument
#   output: true if argument processing should
#	   continue, false if not

check_arg () {
	if [ -n "$NOMOREARGS" ]; then
		return 1
	fi
	case "$1" in
		"--")
			NOMOREARGS="TRUE"
			return
			;;
		"-q")
			export QUIET=1
			return
			;;
		"-t")
			export TEST=1
			return
			;;
		"-v")
			export VERBOSE=1
			return
			;;
		"-h")
			show_help
			exit
			;;
		*)
			return 1
			;;
	esac
}
 
if echo "$1" | grep -- - >/dev/null 2>&1; then
	for ARG
	do
		if check_arg "$ARG"; then
			shift
		else
			break
		fi
	done
fi
