#!/bin/sh

# export data from other server
# pg_hba.conf on $PRIMARY_NMS must be changed
# to allow connections from $STANDBY_NMS.  Also
# postgresql.conf on $PRIMARY_NMS must be changed
# so that the postmaster process is configured
# to listen on all IP interfaces including
# localhost.

SYNC_USER=opennms
SYNC_HOME=~$SYNC_USER/failover/
PRIMARY_NMS=nms-01
STANDBY_NMS=`host`
OPENNMS_ETC=/etc/opennms
OPENNMS_ETC_STANDBY=/etc/opennms-standby 
OPENNMS_ETC_FAILOVER=/etc/opennms-failover
OPENNMS_RRD=/var/lib/opennms/rrd
OPENNMS_RRD_STANDBY=/var/lib/opennms/rrd-standby
OPENNMS_RRD_FAILOVER=/var/lib/opennms/rrd-failover
FAILOVER_DB=opennms_failover

if [ -f $SYNC_HOME/bin/sync-envvars ]; then
        . $SYNC_HOME/etc/sync-envvars
fi

# export db data
dbSync()
{
  echo "Exporting data from $PRIMARY_NMS..."
  if ! test -d "${SYNC_HOME}/db"
  then
    mkdir $SYNC_HOME/db
  fi

  pg_dump -i -h $PRIMARY_NMS -U opennms opennms > $SYNC_HOME/db/opennms.sql

  if [ $? -eq 0 ]
  then
    echo "Dropping and recreating $FAILOVER_DB Database locally..."
    psql -U opennms template1 -c "DROP DATABASE $FAILOVER_DB;" 
    psql -U opennms template1 -c "CREATE DATABASE $FAILOVER_DB ENCODING 'unicode';"
  else
    exit 1
  fi

  if [ $? -eq 0 ]
  then
    echo "Importing data exported from $PRIMARY_NMS into recreated opennms_failover DB..."
    psql -U opennms $FAILOVER_DB < $SYNC_HOME/db/opennms.sql
  else
    exit 1
  fi
}


# Now it is important that the opennms configuration files
# rsync'd from $PRIMARY_NMS
etcSync()
{
  echo "Rsync'ing configuration files from $PRIMARY_NMS..."
  rsync -e 'ssh -i ~$SYNC_USER/.ssh/id-rsa-key' -avz $SYNC_USER@$PRIMARY_NMS:$OPENNMS_ETC* $OPENNMS_ETC_FAILOVER/
}


# Sync the RRD files
rrdSync()
{
    echo "Rsync'ing RRD data files from $PRIMARY_NMS..."
    rsync -e 'ssh -i ~$SYNC_USER/.ssh/id-rsa-key' -azv $SYNC_USER@$PRIMARY_NMS:$OPENNMS_RRD/* $OPENNMS_RRD_FAILOVER/
}

# Synchronize the DB
dbSync
if [ $? -ne 0 ]
then
  echo "Failed db sync of $PRIMARY_NMS!"
  exit 1
fi

# Synchronize the failover $OPENNMS_ETC_FAILOVER with the $PRIMARY_NMS:$OPENNMS_ETC/
etcSync
if [ $? -ne 0 ]
then
  echo "Failed etc sync of $PRIMARY_NMS!"
  exit 1
fi

# Synchronize the failover $OPENNMS_RRD_FAILOVER/ with the primary $OPENNMS_RRD/
rrdSync
if [ $? -ne 0 ]
then
  echo "Failed etc sync of $PRIMARY_NMS!"
  exit 1
fi
