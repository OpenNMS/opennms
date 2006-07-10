#!/bin/sh -
#
#  $Id$
#

# workaround for buggy libc's
#
# Disabled Thu Oct 21 00:45:21 EDT 2004 by djgregor to see if this fixes
# the signal 11 problems that people have been seeing (and with hopes that
# it doesn't trigger other problems--i.e.: the problem that caused it to be
# added in the first place).  If this causes problems on certain
# architectures, it might be good to identify them and add specific cases
# for those architectures in this script.  If you find a problem, file a
# new bug and reference bug #959.  Thanks, - djg.
#
### ulimit -s 2048

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

#exec ${JAVA_HOME:+"${JAVA_HOME}/bin/"}java $ANT_OPTS -mx256m \
#    -jar devlib/ant/lib/ant-launcher.jar "$@"

export M2_OPTS=-Xmx256m
$PREFIX/devlib/maven-2.0.4/bin/mvn -Droot.dir=$PREFIX -Dmaven.test.skip=true "$@"
