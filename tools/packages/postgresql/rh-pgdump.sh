#!/bin/bash

# Tries to upgrade from 6.x 

if [ "$#" -ne 1 ]; then
    echo "Syntax: oldpgdump <targetfile>"
    exit 1
fi

target="$1"


PGDATA=/var/lib/pgsql

#the old files

BINDIR=/usr/lib/pgsql/backup

export BINDIR PGDATA

# Postgresql can't be running - do it this way,
# since the current system is 7.0


/sbin/service postgresql stop > /dev/null 2>&1 /dev/null || :

verbose=1


if [ ! -d ${BINDIR} ]
then
	echo There are no binaries for dumping database format ${installed} >&2
	exit 5
fi

/usr/bin/perl -pi -e "s/ -help/ --help/" $BINDIR/pg_dumpall_new



pm_pid=`ps ax | grep -s postmaster | grep -v grep | awk '{print $1}'`
if [ -n "$pm_pid" ]
then
	kill $pm_pid
fi

OPTIONS=

# American or European date format
if [ "${PGDATESTYLE}" != American ]
then
    OPTIONS="-o -e"
fi

POSTMASTER=${BINDIR}/postmaster
POSTGRES=${BINDIR}/postgres
PORT="-p 5431"    # change the port to stop normal users connecting

${POSTMASTER} -S -D ${PGDATA} ${AUTH} ${PORT} ${OPTIONS}

new_pm_pid=`ps ax | grep -s postmaster | grep -v grep | awk '{print $1}'`
if [ A${new_pm_pid} = A ]
then
	echo "Failed to start the postmaster" 2>&1
	exit 7
fi
if [ A${new_pm_pid} = A${pm_pid} ]
then
	echo "Failed to stop the running postmaster" 2>&1
	exit 6
fi

if [ -n "${verbose}" ]
then
	if [ -n "$upgrade" ]
	then
		echo "Dumping the database structure to ${target}" >&2
	else
		echo "Dumping the database to ${target}" >&2
	fi
fi
echo "-- postgresql-dump ${upgrade} on `date` from version ${installed}" >$target
/usr/lib/pgsql/backup/pg_dumpall_new $dump_options >>$target
echo "-- postgresql-dump ${upgrade} completed on `date`" >>$target

if [ -n "${verbose}" ]
then
	echo "Killing the postmaster" >&2
fi
kill $new_pm_pid



# Dump the database....

#dump_options=""

#echo "-- postgresql-dump ${upgrade} on `date` from version ${installed}" >$target
#/usr/lib/pgsql/backup/pg_dumpall $dump_options >>$target
#echo "-- postgresql-dump ${upgrade} completed on `date`" >>$target

#if [ -n "${verbose}" ]
#then
#	echo "Killing the postmaster" >&2
#fi
#kill $new_pm_pid
