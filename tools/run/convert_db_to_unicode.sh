#!/bin/sh

if [ "`id -u`" -ne "0" ]; then
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
	SQL_FILE=@root.install@/etc/create.sql
fi

FILE="/tmp/pg_dump-${DATABASE}.gz"

echo "------------------------------------------------------------------------------" >> $LOG_FILE

print() {
	echo -e "- $@... \c";
	echo "`date` $@" >> $LOG_FILE
}

print "dumping data to $FILE"
su $PG_USER -c "pg_dump -a -D $DATABASE | gzip -c > $FILE" 2>>$LOG_FILE
if [ $? -ne 0 ]; then
	echo "failed"
	exit 10
fi
echo "ok"

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
cat $SQL_FILE | psql -U $DB_USER $DATABASE >>$LOG_FILE 2>&1
if [ $? -ne 0 ]; then
	echo "failed"
	exit 40
fi
echo "ok"

print "restoring data..."
gzip -dc $FILE | psql -U $DB_USER $DATABASE >>$LOG_FILE 2>&1
if [ $? -ne 0 ]; then
	echo "failed"
	exit 50
fi
echo "ok"
