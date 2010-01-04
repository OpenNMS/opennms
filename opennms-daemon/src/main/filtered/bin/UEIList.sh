#!/bin/bash 

show_help () {
 
  cat <<END

Usage: $0
 
END
  return
 
}

VERSION_UEILIST='$Revision: 13193 $'

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

OPENNMS_HOME="${install.dir}"

# load libraries
for script in pid_process arg_process build_classpath check_tools \
	find_jarfile handle_properties java_lint \
	ld_path version_compare; do
	source $OPENNMS_HOME/lib/scripts/${script}.sh
done

# load platform-independent settings
for file in $OPENNMS_HOME/lib/scripts/platform_*.sh; do
	source $file
done

add_ld_path "$OPENNMS_HOME/lib"

JAVA_CMD="$JAVA_HOME/bin/java"
APP_CLASSPATH=`build_classpath dir:$OPENNMS_HOME/lib/updates \
	jardir:$OPENNMS_HOME/lib/updates "cp:$CLASSPATH_OVERRIDE" \
	dir:$OPENNMS_HOME/etc jardir:$OPENNMS_HOME/lib  \
	"cp:$CLASSPATH"`
APP_VM_PARMS="-Dopennms.home=$OPENNMS_HOME"
APP_CLASS="org.opennms.netmgt.config.UEIList"

if [ -z "$NOEXECUTE" ]; then
	$JAVA_CMD -classpath $APP_CLASSPATH $APP_VM_PARMS $APP_CLASS
	exit 0
fi
