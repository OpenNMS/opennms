#!/bin/sh -
#
# Starts the remote poller.
# w00t
#
# Thu Sep 18 08:54:00 EST 2008 - ranger@opennms.org
# - updated to use runjava
# Mon Jun 16 15:52:00 EST 2008 - ranger@opennms.org
# - updated to use the installed location
# Tue Dec 12 23:05:42 GMT 2006 - dj@opennms.org

OPENNMS_HOME="@install.dir@"
JAVA_CONF="$OPENNMS_HOME/etc/java.conf"

#JAVA_HOME=""
#RMI_LOCATION="RDU"
#RMI_HOST="demo.opennms.org"
MONITOR_JAR="$OPENNMS_HOME/bin/remote-poller.jar"
RMI_PORT=1099

if [ -f "$JAVA_CONF" ]; then
        JAVA_EXE="`cat $JAVA_CONF`"
fi

if [ "$JAVA_EXE" = "" ]; then
        if [ "$JAVA_HOME" = "" ] || [ ! -x "$JAVA_HOME/bin/java" ]; then
                echo "ERROR: $JAVA_CONF file not found, and \$JAVA_HOME is not set to a valid JDK."
                echo "Try running $OPENNMS_HOME/bin/runjava, or set JAVA_HOME."
                exit 1
        else
                JAVA_EXE="$JAVA_HOME/bin/java"
        fi
fi

if [ "$RMI_LOCATION" = "" ]; then
        echo "Set \$RMI_LOCATION and try again."
        exit 1
fi
if [ "$RMI_HOST" = "" ]; then
        echo "Set the RMI host in this script."
        exit 1
fi

#log_file="poll.log.`date '+%Y%m%d-%H%M%S'`"
#log_file="/tmp/poll.log"
log_file="/dev/null"

exec nohup $JAVA_EXE \
        -Djava.rmi.activation.port="$RMI_PORT" \
        -Dlog4j.logger="DEBUG" \
        -jar "$MONITOR_JAR" \
        --url="rmi://$RMI_HOST" --location="$RMI_LOCATION" > $log_file 2>&1 &
