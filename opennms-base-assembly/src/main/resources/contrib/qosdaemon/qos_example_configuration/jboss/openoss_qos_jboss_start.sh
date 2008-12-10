#!/bin/sh
# echo following commands to prompt
set -x
# openoss start up file Created by Craig Gallen 19th April 2005
# this file starts jboss with options for use by openoss
# basic file does the following sh /opt/jboss/bin/run.sh -c default -Djava.rmi.server.hostname=locahost
# Added vm options -Djava.awt.headless=true so that BUTE code would also run in server 1-4-05
echo "====================================================================================="
echo "open oss startup script"
echo "starting with java VM options -Djava.awt.headless=true "
echo "starting with java VM options -Djava.awt.headless=true "echo "starting with jboss options -Djava.rmi.server.hostname=localhost "
echo "====================================================================================="

JAVA_OPTS=" $JAVA_OPTS  -Djava.awt.headless=true "

# note in file system have a symbolic link between /opt/jboss and the actual directory
JBOSS_HOME="/opt/jboss"
export JAVA_OPTS
sh $JBOSS_HOME/bin/run.sh -Djava.rmi.server.hostname=jbossjmsserver1 -DqosbeanpropertiesFile=/opt/jboss/server/default/conf/props/qosbean.properties
