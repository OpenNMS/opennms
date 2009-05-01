#!/bin/bash

if `ps auxwww >/dev/null 2>&1`; then
	PS="ps auxwww"
elif `ps -ef >/dev/null 2>&1`; then
	PS="ps -ef"
else
	echo "I don't know how to run PS on your system!"
	exit 1
fi
export PS

# list_opennms_pids ()
#   input : none
#   output: a list of process IDs from the OpenNMS VM

list_opennms_pids () {
        $PS \
		| grep OPENNMSLAUNCH \
		| grep -v grep \
		| awk '{print $2}'
	if [ -f ${install.pid.file} ]; then
		$PS \
			| grep -v grep \
			| awk '{print $2}' \
			| grep "^`cat ${install.pid.file}`$"
	fi
	return $?
}

fork () {
	PIDFILE="$1"; shift
	OUTPUT="$1"; shift
	echo "------------------------------------------------------------------------------" >> "$OUTPUT"
	echo $@ >> "$OUTPUT"
	$@ >>"$OUTPUT" 2>&1 &
	echo $! > "$PIDFILE"
	disown
}

####################################################################
# Function: show_wait
#
# Parameters:
#       $1      The number of seconds to sleep
#
# Description:
#       A pointless function for doing a spiffy wait spin =)
####################################################################

show_wait () {
        var=$1
        shift
        if [ -z "$var" ]; then
                var=1
        fi

        if [ -z "$WAIT_CHAR" ]; then
                export WAIT_CHAR="-"
        fi

        if [ "$WAIT_CHAR" = "-" ]; then
                echo -en "\b-"
                export WAIT_CHAR="\\"
        elif [ "$WAIT_CHAR" = "\\" ]; then
                echo -en "\b\\"
                export WAIT_CHAR="|"
        elif [ "$WAIT_CHAR" = "|" ]; then
                echo -en "\b|"
                export WAIT_CHAR="/"
        elif [ "$WAIT_CHAR" = "/" ]; then
                echo -en "\b/"
                export WAIT_CHAR="-"
        fi

        sleep $var
        return 0
}

# Check if $pid (could be plural) are running
checkpid() {
	while [ -n "$1" ]; do
	   [ -d /proc/$1 ] && return 0
	   shift
	done
	return 1
}

