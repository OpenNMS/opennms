#!/bin/bash

. ./provision.sh

export BASE_URL=http://localhost:8980/opennms/rest
export PROV_GROUP=testGroup

if [ -z "$OPENNMS_HOME" ]; then
	echo "You must set \$OPENNMS_HOME"
	exit 1
fi

doNodeAdd() {
	id=$1
	echo "* ADDING NODE $id"
	addNodeToRequisition "$BASE_URL" "$PROV_GROUP" "$id" "node$id" "172.16.1.$id"
	synchRequisition "$BASE_URL" "$PROV_GROUP"
	sleep 1
	showTimestamps "doNodeAdd $1"
	sleep 1
}

showTimestamps() {
	echo "* TIMESTAMPS: $1"
	find "${OPENNMS_HOME}/etc/imports" -name "${PROV_GROUP}*" | sort -u | while read FILE; do STAMP=`grep date-stamp= "$FILE" | sed -e 's,^.*date-stamp=",,' -e 's,".*$,,'`; echo "  - $FILE: $STAMP"; done
}

cat <<END >"${OPENNMS_HOME}/etc/snmp-config.xml"
<?xml version="1.0"?>
<snmp-config port="161" retry="0" timeout="15000" read-community="public" version="v2c" max-vars-per-pdu="10" />
END

sudo "${OPENNMS_HOME}/bin/opennms" restart

createEmptyRequisition "$BASE_URL" "$PROV_GROUP"
synchRequisition "$BASE_URL" "$PROV_GROUP"
sleep 10

doNodeAdd 1
doNodeAdd 2
doNodeAdd 3

LAST_COUNT=0
CURRENT_COUNT=0
while [ $CURRENT_COUNT -ge $LAST_COUNT ]; do
	LAST_COUNT=$CURRENT_COUNT
	CURRENT_COUNT=`ls "${OPENNMS_HOME}"/etc/imports/pending/$PROV_GROUP.xml.* | wc -l`
	echo "- LAST = $LAST_COUNT, CURRENT = $CURRENT_COUNT"
	sleep 5
done

showTimestamps "before adding node 4"
doNodeAdd 4

sleep 5
showTimestamps "after adding node 4"

echo ""
echo "HEY, dummy, go check them files"
