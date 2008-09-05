#!/bin/bash

echo This file deploys the opennms Qos RX and TX files

echo Description:
echo "       This script takes the build files from openoss OSS/J adaptor and deploys them in opennms"
echo "       This file also copies example configuration files into OpenNMS to set up configuration "
echo "              for testing and driving opennms interface"
echo "       Requires that JBOSS_HOME is already set as the installed directory of jboss-4.0.2 server"
echo "                        - to do so use JBOSS_HOME="/usr/share/jboss4" # or wherever jboss is
echo "                        -              export JBOSS_HOME
echo "       Requires that QOS_BUILD_HOME is set as the directory of the qos implimentation"
echo "       Requires that OPEN_NMS_HOME is set as installed directory of OpenNMS"
echo "              this is normally /opt/OpenNMS"



#################################
# setting up jar names
#################################
# VERSION - the version suffix for the Jars to be moved to opennms
#VERSION=2.1.0-SNAPSHOT
#echo Information: VERSION set to $VERSION

# opennms-qosdaemon jar name
#OPENNMS_QOSDAEMON_NAME=opennms-qosd-$VERSION.jar
#echo opennms-qosdaemon jar name set to: $OPENNMS_QOSDAEMON_NAME

# OSSbeans-qos-ejb jar name
#OSSBEANS_QOS_EJB_NAME=OSSbeans-qos-ejb-$VERSION.jar
#echo OSSbeans-qos-ejb jar name set to: $OSSBEANS_QOS_EJB_NAME

# OSSbeans-xml jar name
#OSSBEANS_XML_NAME=OSSbeans-xml-$VERSION.jar
#echo OSSbeans-xml jar name set to: $OSSBEANS_XML_NAME

# OSSbeans-qos-ear ear name
#OSSBEANS_QOS_EAR_NAME=OSSbeans-qos-ear-$VERSION.ear
#echo OSSbeans-qos-ear ear name set to: $OSSBEANS_QOS_EAR_NAME
  
echo Information: Setting up and checking copy locations:
#################################
# local directories to copy from
#################################
# QOS_BUILD_HOME - to be changed to where QosD is built on your machine 
#QOS_BUILD_HOME=./../..
#echo Information: QOS_BUILD_HOME set to $QOS_BUILD_HOME


# OPEN_NMS_CONFIG_HOME- the location of files to be deployed to OpenNMS from this script
# this allows configurations to be updated to match previous and latest releases of OpenNMS
# now expects configuration to be in directory
OPEN_NMS_CONFIG_HOME=./
echo Information: OPEN_NMS_CONFIG_HOME set to $OPEN_NMS_CONFIG_HOME

####################################################
# check the remote directories to copy installation
# note that we check for pre existing symbolic links
####################################################

# set OPEN_NMS_HOME - The address of the installed OpenNMS system
#################################################################
OPEN_NMS_HOME=/opt/opennms
echo Information: OPEN_NMS_HOME set to $OPEN_NMS_HOME
if [ -d "$OPEN_NMS_HOME" ]; then
    echo Information: $OPEN_NMS_HOME directory exists - using as home directory for opennms
else
    echo Error: OPEN_NMS_HOME OpenNMS installation $OPEN_NMS_HOME does not exist
    echo please install OpenNMS to $OPEN_NMS_HOME 
    exit
fi 
if [ -w "$OPEN_NMS_HOME" ]; then  # test for write permissions for this user
    echo Information: You have write permissions for $OPEN_NMS_HOME 
else
    echo Error: You do not have write permissions for $OPEN_NMS_HOME - suggest you run as sudo 
    exit
fi 
echo

# check JBOSS_HOME directory
#############################
# if JBOSS_HOME is not set, use /opt/jboss as location of jboss 4.x server

if [ -d "$JBOSS_HOME" ]; then
    echo Information: JBOSS_HOME is already set to: $JBOSS_HOME
else
    JBOSS_HOME="/opt/jboss"  # Default location for jboss , if no JBOSS_HOME specificed.
    export JBOSS_HOME
    echo Information: JBOSS_HOME was not set so using default JBOSS_HOME: $JBOSS_HOME
fi  
if [ -d "$JBOSS_HOME" ]; then
    echo Information: $JBOSS_HOME directory exists - using as home directory for jboss
else
    echo Error: JBOSS_HOME symbolic link to jboss at $JBOSS_HOME does not exist
    echo please create symbolic link at $JBOSS_HOME to jboss 4 installation 
    exit
fi 
if [ -h "$JBOSS_HOME" ]; then
    echo Information: $JBOSS_HOME is a symbolic link
else
    echo Information: $JBOSS_HOME is not a symbolic link
fi 
if [ -w "$JBOSS_HOME" ]; then  # test for write permissions for this user
    echo Information: You have write permissions for $JBOSS_HOME 
else
    echo Error: You do not have write permissions for $JBOSS_HOME - suggest you run as sudo 
    exit
fi 


# echo following commands to prompt
set -x

# starting to copy files
########################


#########################################
# FILES FOR OPENNMS SERVICE CONFIGURATION
#########################################


# files to copy to opennms to set up the log4j perameters for deamon and services file

cp $OPEN_NMS_HOME/etc/rmi.policy $OPEN_NMS_HOME/etc/rmi.policy_back 
cp $OPEN_NMS_CONFIG_HOME/opennms/rmi.policy                   $OPEN_NMS_HOME/etc

cp $OPEN_NMS_HOME/etc/log4j.properties                        $OPEN_NMS_HOME/etc/log4j.properties_back
cp $OPEN_NMS_CONFIG_HOME/opennms/log4j.properties             $OPEN_NMS_HOME/etc

cp $OPEN_NMS_HOME/etc/service-configuration.xml $OPEN_NMS_HOME/etc/service-configuration_back.xml
cp $OPEN_NMS_CONFIG_HOME/opennms/service-configuration.xml    $OPEN_NMS_HOME/etc

# opennms.conf contains additional JVM -D settings which point to qosd.properties qosdrx.properties etc.

cp $OPEN_NMS_HOME/etc/opennms.conf  $OPEN_NMS_HOME/etc/opennms.conf_back
cp $OPEN_NMS_CONFIG_HOME/opennms/opennms.conf                 $OPEN_NMS_HOME/etc


#########################################
# FILES FOR OSS/J TX (spring) interface on OpenNMS
#########################################

echo code files to copy to opennms after build with opennms not running - these set up the bean and daemon
cp $OPEN_NMS_CONFIG_HOME/opennms/qosd.properties			$OPEN_NMS_HOME/etc

# QoSD-configuration.xml tells the QosD which opennms events to run on
cp $OPEN_NMS_CONFIG_HOME/opennms/QoSD-configuration.xml			$OPEN_NMS_HOME/etc


#########################################
# FILES FOR OSS/J RX interface on OpenNMS
#########################################

echo code files to copy to opennms after build with opennms not running - these set up the bean and daemon
cp $OPEN_NMS_CONFIG_HOME/opennms/qosdrx.properties			$OPEN_NMS_HOME/etc
cp $OPEN_NMS_CONFIG_HOME/opennms/QoSDrxOssBeanRunnerSpringContext.xml	$OPEN_NMS_HOME/etc

#####################################################
# FILES setting up opennms alarm and event management
#####################################################
echo
echo vacuumd file is used to set up opennms automations to drive ossj interface
cp $OPEN_NMS_HOME/etc/vacuumd-configuration.xml $OPEN_NMS_HOME/etc/vacuumd-configuration_back.xml 
cp $OPEN_NMS_CONFIG_HOME/opennms_fault_config/vacuumd-configuration.xml     $OPEN_NMS_HOME/etc

echo ossj_events.xml contains events specifically to drive opennms interface
echo this must be referenced in eventconf.xml
cp $OPEN_NMS_CONFIG_HOME/opennms_fault_config/events/ossj_events.xml 	    $OPEN_NMS_HOME/etc/events

echo
echo note ossj_events.xml must be configured in eventconf.xml for ossj interface to work
echo eventconf must reference the required trap handling configuration files in OPEN_NMS_HOME/etc/events
cp $OPEN_NMS_HOME/etc/eventconf.xml $OPEN_NMS_HOME/etc/eventconf_back.xml
cp $OPEN_NMS_CONFIG_HOME/opennms_fault_config/eventconf.xml                 $OPEN_NMS_HOME/etc 

###################################
# set up opennms inventory importer
###################################
cp $OPEN_NMS_HOME/etc/model-importer.properties $OPEN_NMS_HOME/etc/model-importer.properties_back
cp $OPEN_NMS_CONFIG_HOME/opennms_fault_config/inventory/model-importer.properties        $OPEN_NMS_HOME/etc 

########################################
# FILES TO DEPLOY configuration on JBOSS
########################################
echo
echo jar files to copy from jboss server - dependancies for opennms interface


# USING THIS SETTING AS MAVEN DOES NOT SUPPORT ALL LIBRARIES NEEDED ( and this is simpler )
cp $JBOSS_HOME/client/jbossall-client.jar               	$OPEN_NMS_HOME/lib
cp $JBOSS_HOME/client/jnp-client.jar               		$OPEN_NMS_HOME/lib

echo
echo files to deploy to jboss server after build
cp $OPEN_NMS_CONFIG_HOME/jboss/openoss_qos_jboss_start.sh    $JBOSS_HOME/bin
cp $OPEN_NMS_CONFIG_HOME/jboss/qosbean.properties            $JBOSS_HOME/server/default/conf/props
cp $OPEN_NMS_CONFIG_HOME/jboss/uil2-service.xml              $JBOSS_HOME/server/default/deploy/jms
cp $OPEN_NMS_CONFIG_HOME/jboss/log4j.xml                     $JBOSS_HOME/server/default/conf
cp $OPEN_NMS_CONFIG_HOME/jboss/openoss-jms-service.xml       $JBOSS_HOME/server/default/deploy/jms


