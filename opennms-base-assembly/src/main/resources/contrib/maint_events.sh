#!/bin/sh

# host is required if not connecting of Unix domain socket
PG_HOST="localhost"
PG_PORT=5432
PG_DB=opennms
PG_ARCH_TABLE=event_archives
PG_TXID_SEQ=tx_ids
PG_USER=opennms
PG_PASSWORD=

# Archival period
if [ -z "$1" ]; then
  ARCHIVAL_TIME=" now() - interval '9 weeks'"
else
  # expects input in the form: "2008/09/10"
  ARCHIVAL_TIME="'$1'::timestamp"
fi

#
# Handy function for running SQL statements
#
function runsql() {
	echo "$1"
	#time psql -p $PG_PORT -U $PG_USER $PG_DB -c "begin; $1 commit;"
	time psql -h $PG_HOST -p $PG_PORT -U $PG_USER $PG_DB -c "$1"
}

#
# Create everything we need
#
function runSetup() {
  runsql "CREATE SEQUENCE $PG_TXID_SEQ;"
  runsql "insert into events (eventid, eventuei, eventtime, eventsource, eventcreatetime, eventseverity, eventlog, eventdisplay, systemid) values (0, 'uei.opennms.org/internal/archivedEvent', now(), 'Script', 'localhost', now(), 1, 'N', 'N', '00000000-0000-0000-0000-000000000000');"
  runsql "UPDATE $PG_ARCH_TABLE SET eventtime = now() WHERE eventid = 0";
  runsql "CREATE TABLE $PG_ARCH_TABLE (LIKE events);"
  runsql "ALTER TABLE $PG_ARCH_TABLE ADD COLUMN txid bigint;"
  runsql "CREATE INDEX "$PG_ARCH_TABLE"_txid ON $PG_ARCH_TABLE (txid);"
  runsql "CREATE UNIQUE INDEX "$PG_ARCH_TABLE"_eventid ON $PG_ARCH_TABLE (eventid);"
  runsql "CREATE INDEX "$PG_ARCH_TABLE"_eventid_txid ON $PG_ARCH_TABLE (eventid,txid);"
  runsql "CREATE INDEX "$PG_ARCH_TABLE"_eventtime ON $PG_ARCH_TABLE (eventtime);"
}

#
# Maintenance
#
function runDbMaint() {
  # One time maintenance things
  #runsql "DELETE FROM outages where iflostservice < '2007/1/1'::timestamp;"

  # Table trimming
  runsql "DELETE FROM events e WHERE e.eventid IN (SELECT o.svcregainedeventid FROM outages o WHERE o.svcregainedeventid IS NOT NULL AND  (ifregainedservice - iflostservice)::interval < interval '35 seconds');"

  runsql "DELETE FROM notifications WHERE pagetime < now() - interval '3 months';"
  runsql "DELETE 
            FROM events 
            WHERE NOT EXISTS (
           SELECT svclosteventid 
             FROM outages 
            WHERE svclosteventid = events.eventid
	    UNION
           SELECT svcregainedeventid 
             FROM outages 
            WHERE svcregainedeventid = events.eventid
            UNION
           SELECT eventid 
             FROM notifications 
            WHERE eventid = events.eventid)
              AND eventtime < now() - interval '6 weeks';"

  # Routine maintenance if autovacuum isn't running
  #runsql "VACUUM;"
  #runsql "VACUUM ANALYZE;"
  #runsql "VACUUM events;"
  #runsql "REINDEX TABLE events;"
}

#
# Archive process
#
function doWork() {
  # DB info (Currently only Unix Domain Socket)

  # Get a transaction number
  nextval=`psql -p $PG_PORT -U $PG_USER $PG_DB -c "select nextval('tx_ids')" | sed -n '3p'`; 
  nextval=`echo $nextval`
  echo "Next Value: $nextval"

  # Move all old events into archival table and reset foreign keys
  runsql "INSERT INTO $PG_ARCH_TABLE SELECT *, $nextval FROM events WHERE eventtime < $ARCHIVAL_TIME;"
  runsql "UPDATE outages SET svclosteventid = 0 FROM $PG_ARCH_TABLE WHERE outages.svclosteventid = $PG_ARCH_TABLE.eventid AND $PG_ARCH_TABLE.txID = $nextval;"
  runsql "UPDATE outages SET svcregainedeventid = 0 FROM $PG_ARCH_TABLE WHERE outages.svcregainedeventid = $PG_ARCH_TABLE.eventid AND $PG_ARCH_TABLE.txID = $nextval;"
  runsql "UPDATE notifications SET eventid = 0 FROM $PG_ARCH_TABLE WHERE notifications.eventid = $PG_ARCH_TABLE.eventid AND $PG_ARCH_TABLE.txID = $nextval;"
  runsql "UPDATE alarms SET lasteventid = 0 FROM $PG_ARCH_TABLE WHERE alarms.lasteventid = $PG_ARCH_TABLE.eventid AND $PG_ARCH_TABLE.txID = $nextval;"
  runsql  "DELETE FROM events USING $PG_ARCH_TABLE WHERE $PG_ARCH_TABLE.eventid = events.eventid AND $PG_ARCH_TABLE.txID = $nextval;"
}

runSetup
runDbMaint
doWork

# done
