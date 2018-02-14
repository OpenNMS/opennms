#!/bin/sh -
#
# Starts the remote poller.
# w00t
#
# Thu Jun 9 14:10:42 EST 2016 - ranger@opennms.org
# - updated to detect "REMOTE_POLLER_HOME" based on the script
# Thu Sep 18 08:54:00 EST 2008 - ranger@opennms.org
# - updated to use runjava
# Mon Jun 16 15:52:00 EST 2008 - ranger@opennms.org
# - updated to use the installed location
# Tue Dec 12 23:05:42 GMT 2006 - dj@opennms.org

REMOTE_POLLER_HOME="$(dirname "$0")"
REMOTE_POLLER_HOME="$(cd "$REMOTE_POLLER_HOME/.." || exit 1; pwd)"

JAVA_CONF="$REMOTE_POLLER_HOME/etc/java.conf"

MONITOR_JAR="$REMOTE_POLLER_HOME/bin/remote-poller.jar"
RMI_PORT=1099
JVM_ARGS="-Xmx384m"
EXTRA_ARGS=""
GUI=false

if [ -f "$JAVA_CONF" ]; then
	JAVA_EXE="`cat $JAVA_CONF`"
fi

printHelp() {
	echo "usage: $0 [-h] [-j java_exe] -u <URI> -l <location> [-g] [-n username] [-p password]"
	echo ""
	echo "	-h this help"
	echo "	-u URI to the remote host"
	echo "	   ex: http://opennms-host.com:8980/opennms-remoting"
	echo "	   ex: rmi://opennms-host.com/"
	echo "	-l location name for this poller"
	echo "	-g start the remote poller GUI"
	echo "	-i disable ICMP"
	echo "	-j override Java executable"
	echo "	-n the userName to connect as"
	echo "	-p the Password to connect with"
	echo ""
}

while getopts ":D:u:l:dgij:n:p:" OPT
do
	case $OPT in
		h)
			printHelp
			exit 1
			;;
		u)
			REMOTE_URI="$OPTARG"
			;;
		l)
			REMOTE_LOCATION="$OPTARG"
			;;
		g)
			GUI=true
			EXTRA_ARGS="$EXTRA_ARGS --gui"
			;;
		i)
			EXTRA_ARGS="$EXTRA_ARGS --disable-icmp"
			;;
		j)
			JAVA_EXE="$OPTARG"
			;;
		D)
			JVM_ARGS="$JVM_ARGS -D$OPTARG"
			;;
		d|n|p)
			EXTRA_ARGS="$EXTRA_ARGS -$OPT $OPTARG"
			;;
	esac
done

if [ "$GUI" = "false" ]; then
	JVM_ARGS="$JVM_ARGS -Djava.awt.headless=true"
fi

# backwards compatibility with when it was RMI_*
if [ -z "$REMOTE_LOCATION" ]; then
	REMOTE_LOCATION="$RMI_LOCATION"
fi
if [ -z "$REMOTE_URI" ]; then
	REMOTE_URI="$RMI_HOST"
fi

if [ "$JAVA_EXE" = "" ]; then
	if [ "$JAVA_HOME" = "" ] || [ ! -x "$JAVA_HOME/bin/java" ]; then
		echo "ERROR: $JAVA_CONF file not found, and \$JAVA_HOME is not set to a valid JDK."
		echo "Try running $REMOTE_POLLER_HOME/bin/runjava, or set JAVA_HOME."
		exit 1
	else
		JAVA_EXE="$JAVA_HOME/bin/java"
	fi
fi

if [ "$REMOTE_LOCATION" = "" ]; then
	echo "Error: you must set the location name."
	printHelp
	exit 1
fi
if [ "$REMOTE_URI" = "" ]; then
	echo "Error: you must set the remote URI."
	exit 1
fi

#log_file="poll.log.`date '+%Y%m%d-%H%M%S'`"
#log_file="/tmp/poll.log"
log_file="/dev/null"

if [ -n "$REMOTE_USERNAME" ]; then
	EXTRA_ARGS="$EXTRA_ARGS -n $REMOTE_USERNAME"
fi
if [ -n "$REMOTE_PASSWORD" ]; then
	EXTRA_ARGS="$EXTRA_ARGS -p $REMOTE_PASSWORD"
fi

#echo nohup $JAVA_EXE \
#	$JVM_ARGS \
#	-Djava.rmi.activation.port="$RMI_PORT" \
#	-jar "$MONITOR_JAR" \
#	--url="$REMOTE_URI" \
#	--location="$REMOTE_LOCATION" \
#	$EXTRA_ARGS \
#	"$@"

if [ -n "$PIDFILE" ]; then
	nohup $JAVA_EXE \
		$JVM_ARGS \
		-Djava.rmi.activation.port="$RMI_PORT" \
		-jar "$MONITOR_JAR" \
		--url="$REMOTE_URI" \
		--location="$REMOTE_LOCATION" \
		$EXTRA_ARGS \
		> $log_file 2>&1 &
	echo "$!" > $PIDFILE
else
	exec nohup $JAVA_EXE \
		$JVM_ARGS \
		-Djava.rmi.activation.port="$RMI_PORT" \
		-jar "$MONITOR_JAR" \
		--url="$REMOTE_URI" \
		--location="$REMOTE_LOCATION" \
		$EXTRA_ARGS \
		> $log_file 2>&1 &
fi
