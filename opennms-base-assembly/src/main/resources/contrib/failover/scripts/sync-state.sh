#!/bin/sh
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

case "$!" in

  on)
    echo "Enabling cron..."
    crontab -u $SYNC_USER ~$SYNC_HOME/etc/sync-on.cron
    crontab -l -u $SYNC_USER
    ;;
  off)
    echo "Enabling cron..."
    crontab -u $SYNC_USER ~$SYNC_HOME/etc/sync-on.cron
    crontab -l -u $SYNC_USER
    ;;
  *)
    echo "Usage: $0 {on|off}"
    exit 1
    ;;
esac
