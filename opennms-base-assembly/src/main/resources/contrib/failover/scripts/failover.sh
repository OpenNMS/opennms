#!/bin/bash
## sub definition
syncOn() {
        TEMP=$(mktemp)
        /usr/bin/crontab -l > $TEMP
        /bin/sed -i -e '/^\s*$/d'  $TEMP
        /bin/grep sync.sh $TEMP >/dev/null
        if [ $? != 0 ]; then
                cat  >> $TEMP <<AAAAA
0 0 * * *       /opt/opennms/contrib/failover/scripts/sync.sh
AAAAA
                /usr/bin/crontab -u root $TEMP
                service crond restart
        fi
        return $?
}
## start work
syncOn
/etc/rc.d/init.d/postgresql-9.1 start
OPENNMS_DUMP=/var/opennms/rrd
PGSQL_DATA_DIR=/var/lib/pgsql/9.1/data
PGSQL_TRIGGER_FILE=$PGSQL_DATA_DIR/trigger.txt # This file triggers the recovery
PGSQL_RECOVERY_FILE=$PGSQL_DATA_DIR/recovery.done # This file sinals recovery is done ( recovery.conf is renamed to recovery.done )
LOG=/opt/opennms/logs/failover.log

stopOpenNMS() {
        service jmp-watchdog stop
        service jmp-opennms stop
}

checkForRecoveryDone() {
 	START_TIMEOUT=30
        STATUS_WAIT=10
        STATUS_ATTEMPTS=0
        while [ $STATUS_ATTEMPTS -lt $START_TIMEOUT ]; do
                if [ -e $PGSQL_RECOVERY_FILE ]; then
                        echo "PGSQL recovery complete"  >> $LOG
                        break
                else
                        echo "PGSQL recovery in process.."  >> $LOG
                        echo "$PGSQL_DATA_DIR/recovery.conf will be renamed to $PGSQL_DATA_DIR/recovery.done when complete"  >> $LOG
                        sleep $STATUS_WAIT
                        STATUS_ATTEMPTS=`expr $STATUS_ATTEMPTS + 1`
                fi
        done

	# Did we complete recovery?
        if [ ! -e $PGSQL_RECOVERY_FILE ]; then
        	echo "ERROR: PGSQL recovery FAILED, opennms will not be started"  >> $LOG
		service jmp-watchdog start
		exit;
	fi
}

# MAIN

stopOpenNMS
touch $PGSQL_TRIGGER_FILE
checkForRecoveryDone
service jmp-watchdog start
/etc/rc.d/init.d/opennms start

