#!/usr/bin/env bash

# This file exists in three places in the source tree, namely:
#
#   (core) opennms-base-assembly/src/main/filtered/bin/support_pack.sh
#   (minion) opennms-assemblies/minion/src/main/filtered/bin/support_pack.sh
#   (sentinel) opennms-assemblies/sentinel/src/main/filtered/bin/support_pack.sh
#
# If it is updated, it should be updated in all three locations.

DOJAVA="true"
DOOS="true"
DOPACKAGE="true"
DOEVENTS="true"
DOTHREADS="true"
DOLSOF="true"
DOCONFIG="true"
DOLOGS="true"
TEMPDIR="/tmp"
PG_USER="opennms"
PG_PASS="opennms"
PG_HOST="127.0.0.1"
DATETIME=$(date +%Y%m%d%H%M%S)
OPENNMS_HOME="/opt/nothing_yet"
MINION_HOME="/opt/nothing_yet"
SENTINEL_HOME="/opt/nothing_yet"
PACK_DIR="${TEMPDIR}/support-pack-$DATETIME"
PSQL=$(/usr/bin/env which psql 2>/dev/null || echo "false")
TAR=$(/usr/bin/env which tar 2>/dev/null || echo "false")
JAVA_MIN_VERSION="17.0"
JAVA_MAX_VERSION="17.999"

usage(){ echo -e >&2 "
Usage: $(basename $0) [-j -o -p -e -t -c -l -x -u -s]
   -j Collect java version (Default: true)
   -o Collect OS information (Default: true)
   -p Collect OpenNMS package versions (Default: true)
   -e Collect database information (Default: true)
   -t Collect thread dumps (Default: true)
   -c Collect OpenNMS configuration (Default: true)
   -l Collect OpenNMS logs (Default: true)
   -x set TEMPDIR (Default /tmp)
   -u PostgreSQL username (Default: opennms)
   -s PostgreSQL server IP (Default: 127.0.0.1)
   -h | --help Display this help
Everything collected is copied into TEMPDIR/opennms-support-TIMESTAMP and packaged (.tar.gz) for upload.
To exclude a specific item, set the option as any value other than true. Example: -j FALSE, -o no
"; }

if [ "$TAR" == "false" ]; then
   echo "Cannot create a support pack without 'tar', please install the tar package."
   exit 1
fi
if [ "$USER" != "root" ]; then
   echo "Please run this script as root."
   exit 1
fi

VALID_ARGS=$(getopt -o j:o:p:e:t:f:c:l:x:u:s:h --long help -- "$@")
if [[ $? -ne 0 ]]; then
    exit 1;
fi

eval set -- "$VALID_ARGS"
while [ : ]; do
  case "$1" in
    -j)
        DOJAVA="$2"
        shift 2
        ;;
    -o)
        DOOS="$2"
        shift 2
        ;;
    -p)
        DOPACKAGE="$2"
        shift 2
        ;;
    -e)
        DOEVENTS="$2"
        shift 2
        ;;
    -t)
        DOTHREADS="$2"
        shift 2
        ;;
    -c)
        DOCONFIG="$2"
        shift 2
        ;;
    -l)
        DOLOGS="$2"
        shift 2
        ;;
    -x)
        TEMPDIR="$2"
        shift 2
        ;;
    -u)
        PG_USER="$2"
        shift 2
        ;;
    -w)
        PG_PASS="$2"
        shift 2
        ;;
    -s)
        PG_HOST="$2"
        shift 2
        ;;
    -h | --help)
        usage
        exit 0
        ;;
    :)
        usage
        exit 1
        ;;
    ?)
        usage
        exit 1
        ;;

    *)
        break
        ;;
  esac
done

echo "Be aware that generating a support pack can take several minutes!"

#What are we?
if [ -f /etc/redhat-release ]; then  #RPM
   FAMILY="rpm";
   OPENNMS_HOME="/opt/opennms"
   MINION_HOME="/opt/minion"
   SENTINEL_HOME="/opt/sentinel"
fi
if [ -f /etc/os-release ]; then
   if [ $(grep -ci ubuntu /etc/os-release) -ge 1 -o $(grep -ci debian /etc/os-release) -ge 1 ]; then  #deb
      FAMILY="deb";
      OPENNMS_HOME="/opt/opennms"
      MINION_HOME="/opt/minion"
      SENTINEL_HOME="/opt/sentinel"
   fi
fi 

# IS there an existing pack_dir?
if [ -e $PACK_DIR ]; then
   echo "$PACK_DIR exists; Will remove previous support pack dir, OK? (y/n)"
   read -n1 -r yesno
   if [ "$yesno" != "y" ]
   then
     echo "Exiting, clean up manually."
     exit 0
   fi
   rm -rf $PACK_DIR
fi
mkdir $PACK_DIR || exit "Could not create $PACK_DIR"

#Does jstack actually exist in the same location as the java binary we're using?
if [ -e $OPENNMS_HOME/etc/java.conf ]; then
   JSTACK="$(dirname $(<$OPENNMS_HOME/etc/java.conf))/jstack"
   if [ ! -e $JSTACK ]; then
      JSTACK="false";
   fi
elif [ -e $MINION_HOME/bin/find-java.sh ]; then
   JSTACK="$($MINION_HOME/bin/find-java.sh ${JAVA_MIN_VERSION} ${JAVA_MAX_VERSION})/bin/jstack"
   if [ ! -e $JSTACK ]; then
      JSTACK="false";
   fi
elif [ -e $SENTINEL_HOME/bin/find-java.sh ]; then
   JSTACK="$($SENTINEL_HOME/bin/find-java.sh ${JAVA_MIN_VERSION} ${JAVA_MAX_VERSION})/bin/jstack"
   if [ ! -e $JSTACK ]; then
      JSTACK="false";
   fi
fi

#Find a PID from pidfile for some running OpenNMS component's JVM
if [ -e /var/run/opennms/opennms.pid ]; then
   OPENNMS_PID=$(</var/run/opennms/opennms.pid);
elif [ -e $MINION_HOME/data/log/minion.pid ]; then
   OPENNMS_PID=$(<$MINION_HOME/data/log/minion.pid);
elif [ -e $SENTINEL_HOME/data/log/sentinel.pid ]; then
   OPENNMS_PID=$(<$SENTINEL_HOME/data/log/sentinel.pid);
else
   echo "Can't get a pid from pidfile for any running OpenNMS component, some collections may fail!"
fi

#Java
if [ $DOJAVA == "true" ]; then
   echo "Collecting Java info..."
   if [ -e $OPENNMS_HOME/etc/java.conf ]; then
      $(<$OPENNMS_HOME/etc/java.conf) -version &>> $PACK_DIR/java-out.txt
   elif [ -e $MINION_HOME/bin/find-java.sh ]; then
      FOUND_JDK=$($MINION_HOME/bin/find-java.sh ${JAVA_MIN_VERSION} ${JAVA_MAX_VERSION})
      $FOUND_JDK/bin/java -version &>> $PACK_DIR/java-out.txt
   elif [ -e $SENTINEL_HOME/bin/find-java.sh ]; then
      FOUND_JDK=$($SENTINEL_HOME/bin/find-java.sh ${JAVA_MIN_VERSION} ${JAVA_MAX_VERSION})
      $FOUND_JDK/bin/java -version &>> $PACK_DIR/java-out.txt
   fi
fi

#OS
if [ "$DOOS" == "true" ]; then
   echo "Collecting OS info..."
   echo "############################ uname ############################" >> $PACK_DIR/system.txt
   uptime &>> $PACK_DIR/system.txt; echo >> $PACK_DIR/system.txt
   echo "############################ uname ############################" >> $PACK_DIR/system.txt
   uname -a &>> $PACK_DIR/system.txt; echo >> $PACK_DIR/system.txt
   echo "############################ cpuinfo ############################" >> $PACK_DIR/system.txt
   cat /proc/cpuinfo >> $PACK_DIR/system.txt; echo >> $PACK_DIR/system.txt
   echo "############################ df ############################" >> $PACK_DIR/system.txt
   df -h &>> $PACK_DIR/system.txt; echo >> $PACK_DIR/system.txt
   echo "############################ free ############################" >> $PACK_DIR/system.txt
   free -mt &>> $PACK_DIR/system.txt; echo >> $PACK_DIR/system.txt
   echo "############################ top ############################" >> $PACK_DIR/system.txt
   top -Em -b -w 256 -n 1 &>> $PACK_DIR/system.txt; echo >> $PACK_DIR/system.txt
   for i in opennms minion sentinel; do
      echo "############################ status $i ############################" >> $PACK_DIR/system.txt
      systemctl status $i &>> $PACK_DIR/system.txt; echo >> $PACK_DIR/system.txt
   done
   echo "############################ fd ############################" >> $PACK_DIR/system.txt
   ls -l /proc/$OPENNMS_PID/fd | wc -l &>> $PACK_DIR/system.txt; echo >> $PACK_DIR/system.txt
   echo "############################ os-release ############################" >> $PACK_DIR/system.txt
   cat /etc/os-release >> $PACK_DIR/system.txt; echo >> $PACK_DIR/system.txt
   echo "############################ sysctl ############################" >> $PACK_DIR/system.txt
   sysctl -a &>> $PACK_DIR/system.txt; echo >> $PACK_DIR/system.txt
   echo "############################ chrony ############################" >> $PACK_DIR/system.txt
   chronyc tracking &>> $PACK_DIR/system.txt; echo >> $PACK_DIR/system.txt
   if [ -e $OPENNMS_HOME/bin/opennms ]; then
      echo "############################ opennms status ############################" >> $PACK_DIR/system.txt
      $OPENNMS_HOME/bin/opennms -v status &>> $PACK_DIR/system.txt; echo >> $PACK_DIR/system.txt
   fi
fi

#Packages
if [ "$DOPACKAGE" == "true" ]; then
   echo "Collecting package info..."
   for i in opennms opennms-core opennms-webapp-jetty opennms-webapp-hawtio opennms-minion opennms-sentinel meridian meridian-core meridian-webapp-jetty meridian-webapp-hawtio meridian-minion meridian-sentinel; do
      echo "#### Checking for $i" >> $PACK_DIR/packages.txt
      if [ "$FAMILY" == "rpm" ]; then
         rpm -qi $i &>> $PACK_DIR/packages.txt; echo >>  $PACK_DIR/packages.txt;
      fi
      if [ "$FAMILY" == "deb" ]; then
         apt info $i &>> $PACK_DIR/packages.txt; echo >>  $PACK_DIR/packages.txt;
      fi
      echo &>> $PACK_DIR/packages.txt
   done
fi 

#Events
if [ $DOEVENTS == "true" ]; then
   if [ !  -e $PSQL -o "$PSQL" == "false" ]; then
      echo "Event and database statistics requested, but I cannot find 'psql' in your \$PATH! Skipping..."
   else
      echo "Collecting database info..."
      echo "This prompt is for the password of the '$PG_USER' PostgreSQL user. It is not stored or transmitted in any way."
      echo "Please provide the password, or Ctrl+C and disable database collection (-e false)"
      $($PSQL -U $PG_USER -W -h $PG_HOST \
      -c "select version();" \
      -c "select count(*) as eventcount from events;" \
      -c "select count(*) as alarmcount from alarms;" \
      -c "select count(*) as nodecount from node;" \
      -c "select min(eventtime) as oldest_event from events; select min(firsteventtime) as oldest_alarm from alarms;" \
      -c "select location,count(*) from monitoringsystems group by location;" \
      -c "SELECT now() AS before_query; SELECT eventuei, count(*) AS total FROM events GROUP BY eventuei ORDER BY total DESC LIMIT 50; SELECT now() AS after_query;" \
      -c "select alarmtype,count(*) as total from alarms group by alarmtype order by total desc" \
      -c "select table_name, pg_size_pretty(pg_total_relation_size(quote_ident(table_name))) as size_pretty, pg_total_relation_size(quote_ident(table_name)) as size_raw from information_schema.tables where table_schema = 'public' order by size_raw desc;"  &>> $PACK_DIR/database.txt)
   fi	
fi

#Thread dump
if [ $DOTHREADS == "true" ]; then
   echo "Collecting thread dumps..."
   if [ !  -e $JSTACK -o "$JSTACK" == "false" ]; then
      echo "Thread dumps requested, but I cannot find jstack in $(dirname $(<$OPENNMS_HOME/etc/java.conf)). Do you need to install a -devel package?"
   else 
      # three thread dumps, 30 seconds apart
      # OPENNMS_PID is the pid from whatever existing [core|minion|sentinel] pidfile we found
      for i in 1 2 3; do
         $($JSTACK $OPENNMS_PID &>> $PACK_DIR/thread_dump_$i.txt)
         #so we don't wait an extra interval for nothing
         if [ $i -lt 3 ]; then echo "sleeping between thread dumps..."; sleep 30; fi 
      done
   fi
fi

#configuration
if [ $DOCONFIG == "true" ]; then
   #config-tester
   if [ -e $OPENNMS_HOME/bin/config-tester ]; then
      echo "Collecting OpenNMS config-tester output..."
      $($OPENNMS_HOME/bin/config-tester -a &>> $PACK_DIR/config-tester.txt)
   fi
   #config-diff
   if [ -e $OPENNMS_HOME/bin/config-diff.sh -a -e $OPENNMS_HOME/etc -a -e $OPENNMS_HOME/share/etc-pristine ]; then
      echo "Collecting OpenNMS config-diff output..."
      $($OPENNMS_HOME/bin/config-diff.sh -p $OPENNMS_HOME/share/etc-pristine -r $OPENNMS_HOME/etc &>> $PACK_DIR/config-diff.txt)
   fi
   # core
   if [ -e $OPENNMS_HOME/etc ]; then
      echo "Collecting OpenNMS configuration..."
      $($TAR -chf $PACK_DIR/opennms_config_$DATETIME.tar $OPENNMS_HOME/etc &> /dev/null)
   fi
   # minion
   if [ -e $MINION_HOME/etc ]; then
      echo "Collecting Minion configuration..."
      $($TAR -chf $PACK_DIR/minion_config_$DATETIME.tar $MINION_HOME/etc &> /dev/null)
   fi
   # sentinel
   if [ -e $SENTINEL_HOME/etc ]; then
      echo "Collecting Sentinel configuration..."
      $($TAR -chf $PACK_DIR/sentinel_config_$DATETIME.tar $SENTINEL_HOME/etc &> /dev/null)
   fi
fi

#Log files
if [ $DOCONFIG == "true" ]; then
   # core
   if [ -e $OPENNMS_HOME/logs ]; then
      echo "Collecting OpenNMS logs..."
      $($TAR -chf $PACK_DIR/opennms_logs_$DATETIME.tar $OPENNMS_HOME/logs &> /dev/null)
   fi
   # minion
   if [ -e $MINION_HOME/data/log ]; then
      echo "Collecting Minion logs..."
      $($TAR -chf $PACK_DIR/minion_logs_$DATETIME.tar $MINION_HOME/data/log &> /dev/null)
   fi
   # sentinel
   if [ -e $SENTINEL_HOME/data/log ]; then
      echo "Collecting Sentinel logs..."
      $($TAR -chf $PACK_DIR/sentinel_logs_$DATETIME.tar $SENTINEL_HOME/data/log &> /dev/null)
   fi
fi

#Pack everything up
echo "Generating support pack..."
$($TAR -zcf $TEMPDIR/opennms_supportpack_$DATETIME.tar.gz $PACK_DIR &> /dev/null) && rm -rf $PACK_DIR; echo "Support pack '$TEMPDIR/opennms_supportpack_$DATETIME.tar.gz' created." || echo "Failed to create support pack, you may need to clean up $PACK_DIR manually."

exit 0
