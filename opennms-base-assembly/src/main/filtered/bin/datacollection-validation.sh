#!/bin/sh - 

JAVA_OPTIONS="-Xmx256m"
opennms_home="${install.dir}"

app_class=org.opennms.netmgt.config.DataCollectionConfigFactory

exec $opennms_home/bin/runjava -r -- $JAVA_OPTIONS \
    -Dopennms.home=$opennms_home \
    -Dopennms.manager.class=$app_class \
    -jar $opennms_home/lib/opennms_bootstrap.jar
