#!/bin/bash

# workaround for buggy libc's
ulimit -s 2048

PWD_CMD=`which pwd 2>&1 | grep -v "no pwd in" | grep -v "shell built-in command"`
[ -z "$PWD_COMMAND" ] && [ -x /bin/pwd ] && PWD_CMD="/bin/pwd"

if [ `expr "$0" : '\(.\)'` = "/" ]; then
	PREFIX=`dirname $0` export PREFIX
else
	if [ `expr "$0" : '\(..\)'` = ".." ]; then
		cd `dirname $0`
		PREFIX=`$PWD_CMD` export PREFIX
		cd -
	elif [ `expr "$0" : '\(.\)'` = "." ] || [ `expr "$0" : '\(.\)'` = "/" ]; then
		PREFIX=`$PWD_CMD` export PREFIX
	else
		PREFIX=`$PWD_CMD`"/"`dirname $0` export PREFIX
	fi
fi

# Lets start some place standard like the head of the build tree ...
cd $PREFIX

VERBOSE=0
for a in "$@"; do
    if [ x"$a" = x"-debug" -o x"$a" = x"-verbose" ]; then
	VERBOSE=1
	break;
    fi
done

if [ $VERBOSE -gt 0 ]; then
    set -x
fi

exec java -Dant.home=devlib -mx256m -jar devlib/ant-launcher.jar "$@"
