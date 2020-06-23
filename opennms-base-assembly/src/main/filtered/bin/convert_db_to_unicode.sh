#!/bin/bash

if [ "`id -u -n`" "!=" "root" ]; then
	echo "You must run this utility as root!"
	exit 1
fi

if [ -z "$3" ]; then
	echo "usage: $0 <postgres_user> <opennms_user> <database_name> [sql_file]"
	exit 2
fi

PG_USER="$1"; shift
DB_USER="$1"; shift
DATABASE="$1"; shift
SQL_FILE="$1"; shift
LOG_FILE="/tmp/unicode-convert.log"

if [ -z "$SQL_FILE" ]; then
	SQL_FILE=${install.etc.dir}/create.sql
fi

FILE="/tmp/pg_dump-${DATABASE}"

echo "------------------------------------------------------------------------------" >> $LOG_FILE

sleep 1

print() {
	echo -e "- $@... \c";
	echo "`date` $@" >> $LOG_FILE
}

print "dumping data to $FILE"
pg_dump -U $PG_USER -a -D $DATABASE > $FILE 2>>$LOG_FILE
if [ $? -ne 0 ]; then
	echo "failed"
	exit 10
fi
echo "ok"

sleep 1

print "dropping old database"
dropdb -U $PG_USER $DATABASE >>$LOG_FILE 2>&1
if [ $? -ne 0 ]; then
	echo "failed"
	exit 20
fi
echo "ok"

print "creating new unicode database"
createdb -U $PG_USER -E UNICODE $DATABASE >>$LOG_FILE 2>&1
if [ $? -ne 0 ]; then
	echo "failed"
	exit 30
fi
echo "ok"

print "recreating tables..."
psql -U $DB_USER -f $SQL_FILE $DATABASE >>$LOG_FILE 2>&1
if [ $? -ne 0 ]; then
	echo "failed"
	exit 40
fi
echo "ok"

print "restoring data..."
psql -U $DB_USER -f $SQL_FILE $DATABASE >>$LOG_FILE 2>&1
if [ $? -ne 0 ]; then
	echo "failed"
	exit 50
fi
echo "ok"
