#!/bin/bash

if [ -n "$1" ] ; then

	rootdir=/home/weave/OpenNMS/opennms-new

	unset CLASSPATH
	for i in $rootdir/tests/classes $rootdir/work/jars/*.jar $rootdir/lib/*.jar ; do
		extendpath CLASSPATH $i
	done
	export CLASSPATH

	java org.opennms.test.jms.JmsEventReceiver

else
	konsole -T "JMS Event Queue Receiver" -e $0 run 1>/dev/null 2>&1 &
fi

