#!/bin/bash

# This script will start opennms
# in in a configuration that has been
# duplicated from the primary opennms
# system.

# Must first stop OpenNMS that is running and
# monitoring the primary server.

SYNC_USER=opennms
SYNC_HOME=~$SYNC_USER/failover
PRIMARY_NMS=nms-01
STANDBY_NMS=`hostname`
OPENNMS_ETC=/etc/opennms
OPENNMS_ETC_STANDBY=/etc/opennms-standby 
OPENNMS_ETC_FAILOVER=/etc/opennms-failover
OPENNMS_RRD=/var/lib/opennms/rrd
OPENNMS_RRD_STANDBY=/var/lib/opennms/rrd-standby
OPENNMS_RRD_FAILOVER=/var/lib/opennms/rrd-failover
OPENNMS_BIN=/usr/share/opennms/bin

if [ -f $SYNC_HOME/etc/sync-envvars ]; then
        . $SYNC_HOME/etc/sync-envvars
fi

case "$1" in

  start)
    echo "Stopping automatic synchronization..."
    $SYNC_HOME/scripts/sync-state.sh off

    if [ $? -ne 0 ]
    then
      echo "ERROR:Failed to stop synchronization cron job; not failing over!"
      exit 1
    fi

    echo "Stopping OpenNMS in preparation of failover..."
    $OPENNMS_BIN/opennms stop
    if [ $? -ne 0 ]
    then
      echo "ERROR:Failed to stop standby OpenNMS instance; not failing over!"
      $SYNC_HOME/scripts/sync-state.sh on
      exit 1
    fi 

    echo "Changing the $OPENNMS_ETC symbolic link to $OPENNMS_ETC_FAILOVER..."
    rm $OPENNMS_ETC
    if [ $? -ne 0 ]
    then
      echo "ERROR:Failed to remove symbolic link to the $OPENNMS_ETC; not failing over!\nVerify OpenNMS state."
      $SYNC_HOME/scripts/sync-state.sh on
      exit 1
    fi

    ln -s $OPENNMS_ETC_FAILOVER $OPENNMS_ETC
    if [ $? -ne 0 ]
    then
      echo "ERROR:Failed to create symbolic link to the $OPENNMS_ETC_FAILOVER; not failing over!\nVerify OpenNMS state."
      $SYNC_HOME/scripts/sync-state.sh on
      exit 1
    fi

    echo "Changing the $OPENNMS_RRD symbolic link to $OPENNMS_RRD_FAILOVER..."
    rm $OPENNMS_RRD
    if [ $? -ne 0 ]
    then
      echo "ERROR:Failed to remove symbolic link to the $OPENNMS_RRD; not failing over!\nVerify OpenNMS state."
      $SYNC_HOME/scripts/sync-state.sh on
      exit 1
    fi

    ln -s $OPENNMS_RRD_FAILOVER $OPENNMS_RRD
    if [ $? -ne 0 ]
    then
      echo "ERROR:Failed to create symbolic link to the $OPENNMS_RRD_FAILOVER; not failing over!\nVerify OpenNMS state."
      $SYNC_HOME/scripts/sync-state.sh on
      exit 1
    fi

    echo "Setting failover data sources configuration..."
    cp -p ~$SYNC_HOME/etc/opennms-datasources-failover.xml $OPENNMS_ETC/opennms-datasources.xml

    echo "Restarting OpenNMS..."
    $OPENNMS_BIN/opennms start
    ;;
  stop)

    echo "Stopping OpenNMS in failover mode, returning to standby mode..."
    $OPENNMS_BIN/opennms stop
    if [ $? -ne 0 ]
    then
      echo "ERROR:Failed to stop failover OpenNMS instance; not failing over!"
      $SYNC_HOME/scripts/sync-state.sh off
      exit 1
    fi 

    echo "Changing the $OPENNMS_ETC symbolic link to $OPENNMS_ETC_STANDBY..."
    rm $OPENNMS_ETC
    if [ $? -ne 0 ]
    then
      echo "ERROR:Failed to remove symbolic link to the $OPENNMS_ETC; not starting standby mode!\nVerify OpenNMS state."
      $SYNC_HOME/scripts/sync-state.sh off
      exit 1
    fi

    ln -s $OPENNMS_ETC_STANDBY $OPENNMS_ETC
    if [ $? -ne 0 ]
    then
      echo "ERROR:Failed to create symbolic link to the $OPENNMS_ETC_STANDBY; not starting standby mode!\nVerify OpenNMS state."
      $SYNC_HOME/scripts/sync-state.sh off
      exit 1
    fi

    echo "Changing the $OPENNMS_RRD symbolic link to $OPENNMS_RRD_STANDBY..."
    rm $OPENNMS_RRD
    if [ $? -ne 0 ]
    then
      echo "ERROR:Failed to remove symbolic link to the $OPENNMS_RRD; not starting in standby mode!\nVerify OpenNMS state."
      $SYNC_HOME/scripts/sync-state.sh off
      exit 1
    fi

    ln -s $OPENNMS_RRD_STANDBY $OPENNMS_RRD
    if [ $? -ne 0 ]
    then
      echo "ERROR:Failed to create symbolic link to the $OPENNMS_RRD_STANDBY; not starting in standby mode!\nVerify OpenNMS state."
      $SYNC_HOME/scripts/sync-state.sh off
      exit 1
    fi
    ln -s $OPENNMS_RRD_STANDBY $OPENNMS_RRD

#    echo "Chaning failover data sources configuration to stand-by configuration..."
#    cp -p $SYNC_USER/failover/etc/opennms-datasources-standby.xml $OPENNMS_ETC/opennms-datasources.xml

    echo "Restarting OpenNMS..."
    $OPENNMS_BIN/opennms start

    echo "Restarting automatic synchronization..."
    $SYNC_HOME/scripts/sync-state.sh on
    ;;
  *)
    echo "Usage: $0 {start|stop}"
    exit 1
    ;;
esac
