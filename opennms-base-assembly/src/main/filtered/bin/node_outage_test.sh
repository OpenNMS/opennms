#!/bin/bash

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

OPENNMS_HOME="${install.dir}"
LOG4J_CONFIG="log4j.properties"

# load libraries
for script in pid_process arg_process build_classpath check_tools \
	find_jarfile handle_properties java_lint \
	ld_path version_compare; do
	source $OPENNMS_HOME/lib/scripts/${script}.sh
done

add_ld_path "$OPENNMS_HOME/lib"

JAVA_CMD="$JAVA_HOME/bin/java"
APP_CLASSPATH=`build_classpath dir:$OPENNMS_HOME/lib/updates \
	jardir:$OPENNMS_HOME/lib/updates "cp:$CLASSPATH_OVERRIDE" \
	dir:$OPENNMS_HOME/etc jardir:$OPENNMS_HOME/lib "cp:$CLASSPATH"`
APP_VM_PARMS="-Xmx256m -Dopennms.home'$OPENNMS_HOME' -Dlog4j.configuration='$LOG4J_CONFIG'"
APP_CLASS="org.opennms.test.nodeoutage.Tester"

if [ -z "$NOEXECUTE" ]; then
	$JAVA_CMD -classpath $APP_CLASSPATH $APP_VM_PARMS $APP_CLASS "$@"
fi
