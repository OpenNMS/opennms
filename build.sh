#!/bin/sh -
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
		orig_dir="`pwd`"
		cd `dirname $0`
		PREFIX=`$PWD_CMD` export PREFIX
		cd "$orig_dir"
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

if [ -z "$MAVEN_SKIP" ]; then
    MAVEN_SKIP=maven.test.skip.exec
fi

if [ -n "$JAVA_HOME" ]; then
	PATH="$JAVA_HOME/bin:$PATH"
	export PATH
fi

[ -z "$MAVEN_OPTS" ] && MAVEN_OPTS=-Xmx1g
export MAVEN_OPTS
[ -z "$MVN" ] && MVN="$PREFIX/maven/bin/mvn"
"$MVN" -Droot.dir=$PREFIX -D$MAVEN_SKIP=true "$@"
