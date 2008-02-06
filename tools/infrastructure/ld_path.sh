#!/bin/bash

VERSION_LD_PATH='$Revision$'
PACKAGES="$PACKAGES LD_PATH"

# add_ld_path ()
#   input : a path to add to the (DY)LD_LIBRARY_PATH
#   output: true if successful

add_ld_path () {
	if [ -z "$1" ]; then
		echo "add_ld_path requires an argument!"
		return 1
	fi

	case "`uname`" in
		Darwin)
			if echo "$DYLD_LIBRARY_PATH" | grep -v "$OPENNMS_HOME/lib" >/dev/null 2>&1; then
				DYLD_LIBRARY_PATH="$DYLD_LIBRARY_PATH:$OPENNMS_HOME/lib"
				export DYLD_LIBRARY_PATH
			fi
			return
			;;
		*)
			if echo "$LD_LIBRARY_PATH" | grep -v "$OPENNMS_HOME/lib" >/dev/null 2>&1; then
				LD_LIBRARY_PATH="$LD_LIBRARY_PATH:$OPENNMS_HOME/lib"
				export LD_LIBRARY_PATH
			fi
			return
			;;
	esac

	return 1
}
