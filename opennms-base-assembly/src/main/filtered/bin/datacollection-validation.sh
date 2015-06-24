#!/bin/sh - 

JAVA_OPTIONS="-Xmx256m"
OPENNMS_HOME="${install.dir}"
OPENNMS_BINDIR="${install.bin.dir}"

APP_CLASS=org.opennms.netmgt.config.DataCollectionConfigFactory

exec "$OPENNMS_BINDIR"/runjava -r -- $JAVA_OPTIONS \
	-Dopennms.home="$OPENNMS_HOME" \
	-Dopennms.manager.class="$APP_CLASS" \
	-Dlog4j.configurationFile="$OPENNMS_HOME"/etc/log4j2-tools.xml \
	-jar $OPENNMS_HOME/lib/opennms_bootstrap.jar
