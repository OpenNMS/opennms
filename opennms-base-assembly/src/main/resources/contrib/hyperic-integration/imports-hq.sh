#!/bin/sh

WGET_PATH=/usr/bin/wget
OPENNMS_HOME=/opt/opennms
TMP_PATH=/tmp
#HYPERIC_URL=http://hqadmin:hqadmin@127.0.0.1:7080
HYPERIC_HOST=127.0.0.1
HYPERIC_PORT=7080
HYPERIC_PATH=/hqu/opennms/modelExport/list.hqu
HYPERIC_USER=hqadmin
HYPERIC_PW=hqadmin
HYPERIC_URL=http://$HYPERIC_USER:$HYPERIC_PW@$HYPERIC_HOST:$HYPERIC_PORT

#$WGET_PATH --save-cookies="$TMP_PATH/cookies" --keep-session-cookies --post-data="j_username=$HYPERIC_USER&j_password=$HYPERIC_PW" $HYPERIC_URL/j_security_check.do

#$WGET_PATH --load-cookies="$TMP_PATH/cookies" --output-document="$OPENNMS_HOME/etc/imports/imports-HQ.xml" $HYPERIC_URL/hqu/opennms/exporter/list.hqu

$WGET_PATH --auth-no-challenge --output-document="$OPENNMS_HOME/etc/imports/pending/HQ.xml" $HYPERIC_URL/$HYPERIC_PATH
