#!/bin/sh

# export data from other server
# pg_hba.conf on $PRIMARY_NMS must be changed
# to allow connections from $STANDBY_NMS.  Also
# postgresql.conf on $PRIMARY_NMS must be changed
# so that the postmaster process is configured
# to listen on all IP interfaces including
# localhost.

SYNC_USER=opennms
SYNC_HOME=/opt/opennms/contrib/failover/
PRIMARY_NMS=`hostname`
OPENNMS_DUMP=/var/opennms/rrd
FAILOVER_DB=opennms


# export db data
dbSync()
{
  echo "Exporting data from $PRIMARY_NMS..."
  if ! test -d "${OPENNMS_DUMP}"
  then
    mkdir $OPENNMS_DUMP
  fi
  
  if [ -f $OPENNMS_DUMP/opennms.sql ] ; then
	/bin/mv $OPENNMS_DUMP/opennms.sql $OPENNMS_DUMP/opennms.sql.bk
  fi

  pg_dump -i -h localhost -U opennms opennms > $OPENNMS_DUMP/opennms.sql

  if [ $? -eq 0 ]
  then
    echo "dump $FAILOVER_DB Database success..."
  else
    exit 1
  fi

}


# Now it is important that the opennms configuration files
# rsync'd from $PRIMARY_NMS
cfgSync ()
{
  echo "csync2 configuration and rrd  files from $PRIMARY_NMS..."
  /usr/sbin/csync2 -G opennms -x >> /var/log/csync2.log 2>&1
  return 0
}


# Synchronize the DB
dbSync
if [ $? -ne 0 ]
then
  echo "Failed db sync of $PRIMARY_NMS!"
  exit 1
fi

# Synchronize the failover $OPENNMS_ETC_FAILOVER with the $PRIMARY_NMS:$OPENNMS_ETC/
cfgSync
if [ $? -ne 0 ]
then
  echo "Failed etc sync of $PRIMARY_NMS!"
  exit 1
fi

