#!/bin/sh -
#
# Starts the remote poller.
# w00t
#
# Mon Jun 16 15:52:00 EST 2008 - ranger@opennms.org
# - updated to use the installed location
# Tue Dec 12 23:05:42 GMT 2006 - dj@opennms.org

#JAVA_HOME="/usr/java/jdk1.6.0_02"
#RMI_LOCATION="RDU"
#RMI_HOST="demo.opennms.org"
MONITOR_JAR="@install.dir@/lib/opennms-remote-poller.jar"
RMI_PORT=1099


if [ "$JAVA_HOME" = "" ]; then
  echo "Set \$JAVA_HOME and try again."
  exit 1
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

exec nohup $JAVA_HOME/bin/java \
        -Djava.rmi.activation.port="$RMI_PORT" \
        -Dlog4j.logger="DEBUG" \
        -jar "$MONITOR_JAR" \
        "rmi://$RMI_HOST" "$RMI_LOCATION" > $log_file 2>&1 &
