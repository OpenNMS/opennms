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

# load libraries
for script in pid_process arg_process \
	compiler_setup find_jarfile handle_properties java_lint version_compare; do
	source $PREFIX/tools/infrastructure/${script}.sh
done

# load platform-independent settings
for file in $PREFIX/tools/infrastructure/platform_*.sh; do
	source $file
done

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
	echo "\$PATH:	 $PATH"
	echo "\$DEFINES:      $DEFINES"
	echo ""

	DEFINES="$DEFINES -Ddebug=true"
fi

if [ "$VERBOSE" = "1" ]; then
	DEFINES="$DEFINES -verbose"
	echo $JAVA_HOME/bin/java $DEFINES -Dant.home=devlib -Droot.source="$PREFIX" -Djava.home="$JAVA_HOME" -cp "devlib/ant-launcher.jar" \
		-mx256m org.apache.tools.ant.launch.Launcher "$@"
fi

$JAVA_HOME/bin/java $DEFINES -Dant.home=devlib -Droot.source="$PREFIX" -Djava.home="$JAVA_HOME" -cp "devlib/ant-launcher.jar" \
	-mx256m org.apache.tools.ant.launch.Launcher "$@"
