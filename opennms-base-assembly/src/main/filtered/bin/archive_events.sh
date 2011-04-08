#!/bin/sh

OPENNMS_HOME="${install.dir}"

if [ -f "$OPENNMS_HOME/etc/opennms.conf" ]; then
	. "$OPENNMS_HOME/etc/opennms.conf"
fi

for FILE in $OPENNMS_HOME/lib/*.jar; do
	CP="$FILE:$CP"
done

exec ${install.bin.dir}/runjava -r -- \
	$ADDITIONAL_MANAGER_OPTIONS -Dopennms.home="$OPENNMS_HOME" -cp "$CP" "$@"

