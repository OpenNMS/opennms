#!/bin/sh

VERSION_BUILD='1.18'

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

# load libraries
for script in pid_process arg_process build_classpath check_tools \
	compiler_setup find_jarfile handle_properties java_lint version_compare; do
	source $PREFIX/tools/infrastructure/${script}.sh
done

# load platform-independent settings
for file in $PREFIX/tools/infrastructure/platform_*.sh; do
	source $file
done

TOOL_CLASSES="`PROPERTY_OVERRIDE="$PREFIX/tools/build.properties" get_property opennms.tools.classes`"
BUILD_CLASSES="`get_property 'root.build'`/classes/opennms"
SABLECC_CLASSES="`get_property 'root.build'`/classes/sablecc"

if [ "$QUIET" != "1" ]; then
	echo "------------------------------------------------------------------------------"
	echo "OpenNMS Build ($VERSION_BUILD)"
	echo "------------------------------------------------------------------------------"
	echo ""
fi

# see tools/infrastructure/build_classpath.sh for syntax
CLASSPATH=`build_classpath cp:$CLASSPATH_OVERRIDE dir:$TOOL_CLASSES \
	dir:$BUILD_CLASSES dir:$SABLECC_CLASSES \
	dir:$PREFIX/lib jar:ant141 jar:ant141-optional \
	jar:xerces201 jar:xalan231 jar:regexp120 jar:jdhcp jar:castor-0.9.3.9 \
	jar:castor-0.9.3.9-xml jar:jcifs jar:log4j jar:postgresql \
	jar:jdbc2_0-stdext jar:sablecc-2.17.2 jar:sablecc-anttask-1.1.0 \
	jar:servlet jar:catalina jar:fop0203 jar:bsf220 jar:batik100 \
	jar:jimi100 jar:ldap jar:batik jar:avalon-framework-4.0 jar:logkit-1.0 \
	jar:jasper-compiler jar:mx4j jar:mx4j-tools "cp:$CLASSPATH"` \
	export CLASSPATH
PATH="$JAVA_HOME/bin:$PATH" export PATH

# parse the root.* properties and create relative versions if necessary
for property in `list_properties`; do
	if echo "$property" | grep -E -q '^root\.'; then
		DEFINES="$DEFINES -D"`echo "$property" | sed -e 's#^root\.#relative.#'`'='`get_relative_property $property`' '
		DEFINES="$DEFINES -D${property}="`get_property $property`
	fi
done

# other misc properties used for the build
DEFINES="$DEFINES -Dopennms.version.major="`get_property opennms.version | sed -e 's#\\..*##'`

if [ "$VERBOSE" = "1" ]; then
	echo "------------------------------------------------------------------------------"
	echo "Environment Variables"
	echo "------------------------------------------------------------------------------"
	echo "BUILD \$PREFIX: $PREFIX"
	echo "\$JAVA_HOME:    $JAVA_HOME"
	echo "\$CLASSPATH:    $CLASSPATH"
	echo "\$PATH:	 $PATH"
	echo "\$DEFINES:      $DEFINES"
	echo ""
	echo "------------------------------------------------------------------------------"
	echo "Build Script Info"
	echo "------------------------------------------------------------------------------"
	echo "$VERSION_BUILD"
	echo '2001/11/11 17:42:32'
	echo 'ben'
	echo ""
	echo "------------------------------------------------------------------------------"
	echo "Build Components"
	echo "------------------------------------------------------------------------------"
	for package in $PACKAGES; do
		eval "echo \"${package}: \$VERSION_BUILD_${package}\""
	done
	echo ""

	DEFINES="$DEFINES -Ddebug=true"
fi

# check to make sure the tools are up to date

for ARG; do
	if [ "$ARG" = "clean" ]; then
		TOOL_ARGS=clean
		break
	fi
done

check_tools
RETVAL=$?
if [ "$RETVAL" -gt "0" ]; then
	if [ "$VERBOSE" = "1" ]; then
		echo "*** $RETVAL new tool files -- rebuilding ***"
		echo $JAVA_HOME/bin/java -Droot.source="$PREFIX" -Djava.home="$JAVA_HOME" -cp "$CLASSPATH" \
			-mx256m org.apache.tools.ant.Main $DEFINES -buildfile tools/build.xml $TOOL_ARGS compile.tools
	fi
	$JAVA_HOME/bin/java $DEFINES -Droot.source="$PREFIX" -Djava.home="$JAVA_HOME" -cp "$CLASSPATH" \
		-mx256m org.apache.tools.ant.Main -buildfile tools/build.xml $TOOL_ARGS compile.tools
fi

if [ "$VERBOSE" = "1" ]; then
	DEFINES="$DEFINES -verbose"
	echo $JAVA_HOME/bin/java $DEFINES -Droot.source="$PREFIX" -Djava.home="$JAVA_HOME" -cp "$CLASSPATH" \
		-mx256m org.apache.tools.ant.Main "$@"
fi

$JAVA_HOME/bin/java $DEFINES -Droot.source="$PREFIX" -Djava.home="$JAVA_HOME" -cp "$CLASSPATH" \
	-mx256m org.apache.tools.ant.Main "$@"
