#!/bin/sh
# opennms_IFOpenOSS3
# ------------------
# This script runs a client which connects to the jboss message queue which this OpenNMS is writing to and listens
# for messages from this opennms. 
# The client receives and displays OSS/J events on the consol terminal
# The class run is called SentinalIF and can also be used as an interface to the www.sidonis.com statewise product
#
# Notes:
# 1. jar class path must not contain spaces. (Seperators = : in linux, ; in Windows)
# 2. if you are using the programs as clients running on machines other than localhost, 
# the -Djava.naming.provider.url needs to point to the remote machine not localhost. 
# 3. on Fedora core, the HOSTS file must also contain the name of the remote machine 
# for RMI to work. This can easily be changed by using the KDE menu item 
# System Tools>Network Device Controll> and selecting 'Configure'. ( you need to be su to do this )
# 4. This client will only work if the qosd libraries are installed in opennms

#################################
# setting up jar names
#################################
# VERSION - the version suffix for the test jars
VERSION=2.1.0
echo Information: VERSION set to $VERSION

# OSSbeans-qos-ejb jar name
OSSBEANS_QOS_EJB_NAME=OSSbeans-qos-ejb-$VERSION.jar
echo OSSbeans-qos-ejb jar name set to: $OSSBEANS_QOS_EJB_NAME

# OSSbeans-xml jar name
OSSBEANS_XML_NAME=OSSbeans-xml-$VERSION.jar
echo OSSbeans-xml jar name set to: $OSSBEANS_XML_NAME

# OSSbeans-qos-ear ear name
OSSBEANS_QOS_EAR_NAME=OSSbeans-qos-ear-$VERSION.ear
echo OSSbeans-qos-ear ear name set to: $OSSBEANS_QOS_EAR_NAME

############################################################
# running program
############################################################

# This file runs the OSS/J client test program
# Requires that OPENNMS_LIB is set as the directory where the qos project is built on your machine 
OPENNMS_LIB=/opt/opennms/lib
OPENNMS_ETC=/opt/opennms/etc

echo running openoss ossj openoss sentinal interface 
echo OPENNMS_LIB set to $OPENNMS_LIB
echo OPENNMS_ETC set to $OPENNMS_ETC

# echo commands to prompt
set -x

#  -Djava.naming.provider.url=jnp://jbossjmsserver1:1099 \
#  -Djava.naming.factory.initial=org.jnp.interfaces.NamingContextFactory  \
#  -Djava.naming.factory.url.pkgs=org.jboss.naming \

java -Djava.security.policy=$OPENNMS_ETC/rmi.policy \
 -DpropertiesFile=./qosclientOpenOSS3.properties \
 -classpath "$OPENNMS_LIB/$OSSBEANS_XML_NAME\
:$OPENNMS_LIB/xbean-2.1.0.jar\
:$OPENNMS_LIB/stax-api-1.0.1.jar\
:$OPENNMS_LIB/jbossall-client.jar\
:$OPENNMS_LIB/$OSSBEANS_QOS_EJB_NAME" \
org.openoss.ossj.tests.SentinalIF $@


#########
# NOTES
#########
# Note $@ passes all command line arguements directly to program - could also use $1 $2 $3 etc
# could alternatively use the following comand as jar set up to run org.openoss.ossj.tests.SentinalIF
# -jar $OPENNMS_LIB/$OSSBEANS_QOS_EJB_NAME 

# NOTE -  using JBOSS installed client-all.jar as the complete library referenced here 
# is not on MAVEN
#:$OPENNMS_LIB/jboss-client-4.0.2.jar\
#:$OPENNMS_LIB/jboss-common-4.0.2.jar\
#:$OPENNMS_LIB/jboss-j2ee-4.0.2.jar\
#:$OPENNMS_LIB/jbossmq-client-4.0.2.jar\
#:$OPENNMS_LIB/jnp-client-4.0.2.jar\
#:$OPENNMS_LIB/jbossx-client-4.0.2.jar\









