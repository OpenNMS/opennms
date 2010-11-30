#!/usr/bin/env bash
#
#  $Id$
#

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

declare -a ARGS
COUNT=0
declare -a DEFINES
DEFINES_COUNT=0
USE_ASSEMBLIES=""
ROOT_FOUND=""
for ARG in "$@"; do
	case $ARG in
		assembly:*)
			USE_ASSEMBLIES="$ARG"
			;;
		-Droot.dir=*)
			ROOT_FOUND="true"
			DEFINES[$DEFINES_COUNT]="$ARG"
			DEFINES_COUNT=`expr $DEFINES_COUNT + 1`
			;;
		-D*)
			DEFINES[$DEFINES_COUNT]="$ARG"
			DEFINES_COUNT=`expr $DEFINES_COUNT + 1`
			;;
		*)
			ARGS[$COUNT]="$ARG"
			COUNT=`expr $COUNT + 1`
			;;
	esac
done

if [ -n "$USE_ASSEMBLIES" ] && [ `grep -c 'OpenNMS Top-Level POM' pom.xml` -gt 0 ]; then
	echo ""
	echo "WARNING: using assemblies from the top-level build are deprecated."
	echo "If you want an assembly of the full build, do the usual"
	echo "'$0 install' and then cd to the opennms-full-assembly/"
	echo "directory and create your assembly from there."
	echo ""
	echo "For now, I'll cheat and do it for you."
	echo ""
	sleep 5
fi

[ -z "$MAVEN_OPTS" ] && MAVEN_OPTS='-XX:PermSize=64M -XX:MaxPermSize=256M -Xmx1G'
export MAVEN_OPTS
[ -z "$MVN" ] && MVN="$PREFIX/maven/bin/mvn"
EXITVAL=0
if [ $COUNT -gt 0 ]; then
	"$MVN" -Droot.dir=$PREFIX -D$MAVEN_SKIP=true "${DEFINES[@]}" "${ARGS[@]}"
	EXITVAL="$?"
fi

if [ $EXITVAL -eq 0 ]; then
	if [ -n "$USE_ASSEMBLIES" ] && [ `grep -c 'OpenNMS Top-Level POM' pom.xml` -gt 0 ]; then
		pushd opennms-full-assembly >/dev/null 2>&1
			if [ "$ROOT_FOUND" = "true" ]; then
				"$MVN" -D$MAVEN_SKIP=true "${DEFINES[@]}" $USE_ASSEMBLIES
			else
				"$MVN" -Droot.dir=$PREFIX -D$MAVEN_SKIP=true "${DEFINES[@]}" $USE_ASSEMBLIES
			fi
		popd >/dev/null 2>&1
	fi
fi

exit $EXITVAL
