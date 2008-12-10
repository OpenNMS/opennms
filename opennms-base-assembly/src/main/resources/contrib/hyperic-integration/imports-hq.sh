#!/bin/sh

WGET_PATH=/usr/bin/wget
OPENNMS_HOME=/opt/opennms
TMP_PATH=/tmp
HYPERIC_URL=http://hq.opennms.org:7080
HYPERIC_USER=hqadmin
HYPERIC_PW=hqadmin

$WGET_PATH --save-cookies="$TMP_PATH/cookies" --keep-session-cookies \
 --post-data="j_username=$HYPERIC_USER&j_password=$HYPERIC_PW" $HYPERIC_URL/j_security_check.do

$WGET_PATH --load-cookies="$TMP_PATH/cookies" --output-document="$OPENNMS_HOME/etc/imports-HQ.xml" \
$HYPERIC_URL/hqu/opennms/exporter/list.hqu
