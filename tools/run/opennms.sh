#!/bin/bash

show_help () {
 
  cat <<END

Usage: $0 <command> [<service>]
 
  command options: start|stop|status

  service options: all|<a service id from the etc/service-configuration.xml>
                   defaults to all

END
  return
 
}

VERSION_OPENNMS='1.21'
STOP_ATTEMPTS=0
RESTART_TOMCAT=0

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

OPENNMS_HOME="@root.install@"
OPENNMS_PIDFILE="@root.install.pid@"
OPENNMS_INITDIR="@root.install.initdir@"
START_TIMEOUT="180" # number of seconds before timing out on startupp
 
pushd "$OPENNMS_HOME" >/dev/null 2>&1

umask 002

# workaround for doing a status
if [ "$1" = "status" ] || [ "$2" = "status" ]; then
	export STATUS_ONLY="yes"
fi

# load libraries
for script in pid_process arg_process build_classpath check_tools \
	compiler_setup find_jarfile handle_properties java_lint \
	ld_path version_compare; do
	source $OPENNMS_HOME/lib/scripts/${script}.sh
done

# load platform-independent settings
for file in $OPENNMS_HOME/lib/scripts/platform_*.sh; do
	source $file
done

# define needed for grep to find opennms easily
JAVA_CMD="$JAVA_HOME/bin/java"

APP_CLASSPATH=`build_classpath "cp:$CLASSPATH_OVERRIDE" \
	dir:$OPENNMS_HOME/etc jardir:$OPENNMS_HOME/lib`

add_ld_path "$OPENNMS_HOME/lib"

MANAGER_CLASS=org.opennms.netmgt.vmmgr.Manager
MANAGER_OPTIONS="-DOPENNMSLAUNCH -Dopennms.home=$OPENNMS_HOME -Djcifs.properties=$OPENNMS_HOME/etc/jcifs.properties"
if [ -z "$JAVA_HEAP_SIZE" ] ; then
	JAVA_HEAP_SIZE=256
fi
if echo "$JAVA_HEAP_SIZE" | egrep '^[0-9]+$' >/dev/null 2>&1 ; then
	MANAGER_OPTIONS="-Xmx${JAVA_HEAP_SIZE}m $MANAGER_OPTIONS"
fi
if [ -n "$USE_INCGC" -a "$USE_INCGC" = true ] ; then
	MANAGER_OPTIONS="-Xincgc $MANAGER_OPTIONS"
fi
if [ -n "$HOTSPOT" -a "$HOTSPOT" = true ] ; then
	JAVA_CMD="$JAVA_CMD -server"
fi

CONTROLLER_CLASS=org.opennms.netmgt.vmmgr.Manager
CONTROLLER_LOG4J_CONFIG=log4j.properties
CONTROLLER_OPTIONS="-Dopennms.home=$OPENNMS_HOME -Dlog4j.configuration=$CONTROLLER_LOG4J_CONFIG"

if [ -z "$1" ]; then
	show_help
	exit 1
fi

COMMAND=$1
SERVICE=$2

[ "$SERVICE" = "all" ] && SERVICE=""

# where to redirect "start" output
REDIRECT=@root.install.logs@/output.log

###############################################################################
# Run opennms.sh with the "-t" option to enable the Java Platform Debugging
# Architecture. This will open a server socket on port 8001 that can be
# connected to by a remote java debugger. A good choice is JSwat which can 
# be found at http://www.bluemarsh.com
###############################################################################
if [ "$TEST" = "1" ]; then
	echo "- enabling JPDA debugging on port 8001"
	JPDA="-Xdebug -Xnoagent -Djava.compiler=none -Xrunjdwp:transport=dt_socket,server=y,address=8001,suspend=n"
fi

if [ -d /var/tomcat4/webapps/@ant.project.name@/WEB-INF ]; then
	TOMCATDIR=/var/tomcat4/webapps/@ant.project.name@/WEB-INF
fi

if [ `find $OPENNMS_HOME $TOMCATDIR -name \*.rpmnew | wc -l` -gt 0 ]; then
	cat <<END

WARNING!  You have files that end in .rpmnew in your
OPENNMS_HOME ($OPENNMS_HOME) directory.

The format of the original files may have changed since
you modified them before installing a new version.
Please double-check that your configuration files are
up-to-date and delete any leftover .rpmnew files or
OpenNMS will not start.

END
	exit 1
fi

if [ `find $OPENNMS_HOME $TOMCATDIR -name \*.rpmsave | wc -l` -gt 0 ]; then
	cat <<END

WARNING!  You have files that end in .rpmsave in your
OPENNMS_HOME ($OPENNMS_HOME) directory.

The format of the original files may have changed since
you modified them before installing a new version, so
your modified configuration files have been backed up
and replaced.  Please double-check that your changes to
the configuration files are added back into the update
files and delete any leftover .rpmsave files or OpenNMS
will not start.

END
	exit 1
fi

get_url () {
	[ -n "$1" ] || return 1
	URL="$1"
	HTTP=`which curl 2>/dev/null | grep -v -E 'no such|no curl'`
	if [ -n "$HTTP" ]; then
		$HTTP -o - -s "$URL" | $OPENNMS_HOME/bin/parse-status.pl
		return 0
	fi
	HTTP=`which wget 2>/dev/null | grep -v -E 'no such|no wget'`
	if [ -n "$HTTP" ]; then
		$HTTP --quiet -O - "$URL" | $OPENNMS_HOME/bin/parse-status.pl
		return 0
	fi
	HTTP=`which lynx 2>/dev/null | grep -v -E 'no such|no lynx'`
	if [ -n "$HTTP" ]; then
		$HTTP -dump "$URL" | $OPENNMS_HOME/bin/parse-status.pl
		return 0
	fi
	return 1
}

case "$COMMAND" in
	start|spawn)
		if [ `list_opennms_pids | wc -l` -ge 1 ]; then
			if `$0 status 2>&1 | grep 'running'` >/dev/null 2>&1; then
				echo `date`": Trying to start OpenNMS but it's already running."
				exit 0
			else
				echo "OpenNMS is partially running."
				echo "If you have just attempted starting OpenNMS, please try again in a few"
				echo "moments, otherwise, at least one service probably had issues starting."
				echo "Check your logs in @root.install.logs@ for errors."
				exit 1
			fi
		else
			if [ `$PS | grep TOMCATLAUNCH | grep -v grep | wc -l` -ge 1 ]; then
				# Tomcat's already running... we need to restart it
				# after OpenNMS is started
				RESTART_TOMCAT=1
			fi

			if [ "$SERVICE" = "" ]; then
				APP_VM_PARMS="$JPDA $MANAGER_OPTIONS"
				APP_CLASS="$MANAGER_CLASS"
				APP_PARMS_BEFORE=""

			else
				APP_VM_PARMS="$CONTROLLER_OPTIONS"
				APP_CLASS="$CONTROLLER_CLASS"
				APP_PARMS_BEFORE="start $SERVICE"
			fi
			if [ -z "$NOEXECUTE" ]; then
				fork $OPENNMS_PIDFILE $REDIRECT $JAVA_CMD -classpath $APP_CLASSPATH $APP_VM_PARMS $APP_CLASS $APP_PARMS_BEFORE "$@" $APP_PARMS_AFTER
			fi
			if [ "$RESTART_TOMCAT" = 1 ]; then
				if [ -d "$OPENNMS_INITDIR" ] && [ -x "$OPENNMS_INITDIR/tomcat4" ]; then
					$OPENNMS_INITDIR/tomcat4 restart
				else
					echo "warning: unable to find the tomcat4 init."
					echo "You will need to restart it yourself or availability"
					echo "data may not update correctly."
				fi
			fi
		fi

		;;
	pause)
		if [ `list_opennms_pids | wc -l` -ge 1 ]; then
			APP_VM_PARMS="$CONTROLLER_OPTIONS"
			APP_CLASS="$CONTROLLER_CLASS"
			APP_PARMS_BEFORE="pause $SERVICE"
			if [ -z "$NOEXECUTE" ]; then
				$JAVA_CMD -classpath $APP_CLASSPATH $APP_VM_PARMS $APP_CLASS $APP_PARMS_BEFORE "$@" $APP_PARMS_AFTER
			fi
		else
			echo "OpenNMS is not running."
		fi
		;;
	resume)
		if [ `list_opennms_pids | wc -l` -ge 1 ]; then
			APP_VM_PARMS="$CONTROLLER_OPTIONS"
			APP_CLASS="$CONTROLLER_CLASS"
			APP_PARMS_BEFORE="resume $SERVICE"
			if [ -z "$NOEXECUTE" ]; then
				$JAVA_CMD -classpath $APP_CLASSPATH $APP_VM_PARMS $APP_CLASS $APP_PARMS_BEFORE "$@" $APP_PARMS_AFTER
			fi
		else
			echo "OpenNMS is not running."
		fi
		;;
	stop)
		if [ `list_opennms_pids | wc -l` -ge 1 ]; then
			while [ "$STOP_ATTEMPTS" -lt 5 ]; do
				if [ `list_opennms_pids | wc -l` -ge 1 ]; then
					APP_VM_PARMS="$CONTROLLER_OPTIONS"
					APP_CLASS="$CONTROLLER_CLASS"
					APP_PARMS_BEFORE="stop $SERVICE"
					if [ -z "$NOEXECUTE" ]; then
						$JAVA_CMD -classpath $APP_CLASSPATH $APP_VM_PARMS $APP_CLASS $APP_PARMS_BEFORE "$@" $APP_PARMS_AFTER
					fi
					sleep 5
				fi
				let STOP_ATTEMPTS="($STOP_ATTEMPTS+1)"
			done
			if [ `list_opennms_pids | wc -l` -ge 1 ]; then
				exit 1
			fi
		else
			echo `date`": trying to stop OpenNMS but it's already stopped."
		fi
		;;
	kill)
		while [ `list_opennms_pids | wc -l` -ge 1 ]; do
			get_url "http://localhost:8181/invoke?objectname=OpenNMS:Name=FastExit&operation=doSystemExit"
			sleep 5
			kill -9 `list_opennms_pids` >/dev/null 2>&1
		done
		;;
	status)
		if [ `list_opennms_pids | wc -l` -ge 1 ]; then
			get_url "http://localhost:8181/invoke?objectname=OpenNMS:Name=FastExit&operation=status"
			[ $? ] && exit 0
			APP_VM_PARMS="$CONTROLLER_OPTIONS"
			APP_CLASS="$CONTROLLER_CLASS"
			APP_PARMS_BEFORE="status $SERVICE"
			if [ -z "$NOEXECUTE" ]; then
				$JAVA_CMD -classpath $APP_CLASSPATH $APP_VM_PARMS $APP_CLASS $APP_PARMS_BEFORE "$@" $APP_PARMS_AFTER 2>&1 | $OPENNMS_HOME/bin/parse-status.pl
			fi
		else
			echo "OpenNMS is not running."
		fi
		;;
	*)
		echo ""
		echo "ERROR: unknown command \"$COMMAND\""
		show_help
		exit 1
		;;
esac

popd >/dev/null 2>&1

exit 0
