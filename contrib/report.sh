#!/bin/bash 

# Produce availability reports on the command line
# Invoke thus:
#
# report.sh [category_name] [format] [monthFormat]
#
# At the time of writing, acceptable values are:
#
# category_name: any category from categories.xml (enclosed in double quotes 
#                if it contains any spaces).
#
# format:        one of HTML, PDF or SVG
#
# monthFormat:   classic or calendar
#
# ie:
#
# ./report.sh "all my nodes" SVG classic
#
# or:
#
# ./report.sh "all my nodes" SVG calendar 
#
# for the new-style reports
#

show_help () {
 
  cat <<END

Usage: $0 [category_name] [format] [monthFormat]
 
END
  return
 
}

VERSION_REPORT='1.13.4.1'

# minimum requirements
MINIMUM_JAVA=1.4.0 export MINIMUM_JAVA

ulimit -s 2048
 
if [ `expr "$0" : '\(.\)'` = "/" ]; then
        PREFIX=`dirname $0` export PREFIX
else
        if [ `expr "$0" : '\(..\)'` = ".." ]; then
                cd `dirname $0`
                PWD_CMD=`which pwd 2>&1 | grep -v "no pwd in"`
                PREFIX=`$PWD_CMD` export PREFIX
                cd -
	elif [ `expr "$0" : '\(.\)'` = "." ] || [ `expr "$0" : '\(.\)'` = "/" ]; then
                PWD_CMD=`which pwd 2>&1 | grep -v "no pwd in"`
                PREFIX=`$PWD_CMD` export PREFIX
        else
                PWD_CMD=`which pwd 2>&1 | grep -v "no pwd in"`
                PREFIX=`$PWD_CMD`"/"`dirname $0` export PREFIX
        fi
fi

OPENNMS_HOME="/opt/OpenNMS"
OPENNMS_WEBAPP="$OPENNMS_HOME/webapps/opennms"

# load libraries
for script in pid_process arg_process build_classpath check_tools \
	find_jarfile handle_properties java_lint \
	ld_path version_compare; do
	source $OPENNMS_HOME/lib/scripts/${script}.sh
done

add_ld_path "$OPENNMS_HOME/lib"

CATNAME="$1"; shift
FORMAT="$1"; shift
MONTHFORMAT="$1";shift

JAVA_CMD="$JAVA_HOME/bin/java"
APP_CLASSPATH=`build_classpath dir:$OPENNMS_HOME/lib/updates \
	jardir:$OPENNMS_HOME/lib/updates "cp:$CLASSPATH_OVERRIDE" \
	dir:$OPENNMS_HOME/etc jardir:$OPENNMS_HOME/lib  \
	"cp:$CLASSPATH"`
APP_VM_PARMS="-Xmx256m -Dopennms.home=$OPENNMS_HOME -Dimage=$OPENNMS_WEBAPP/images/logo.gif -Djava.awt.headless=true"
APP_CLASS="org.opennms.report.availability.AvailabilityReport"
echo "will execute report with:"
echo "JAVA         " $JAVA_CMD
echo "CATEGORY     " $CATNAME
echo "FORMAT       " $FORMAT
echo "MONTH FORMAT " $MONTHFORMAT
echo "CLASSPATH    " $APP_CLASSPATH

if [ -z "$NOEXECUTE" ]; then
	$JAVA_CMD -classpath $APP_CLASSPATH $APP_VM_PARMS -DcatName="$CATNAME" -Dformat="$FORMAT" -DMonthFormat="$MONTHFORMAT" $APP_CLASS "$@"
	exit 0
fi
