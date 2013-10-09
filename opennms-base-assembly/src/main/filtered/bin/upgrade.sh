#!/bin/sh - 

JAVA_OPTIONS="-Xmx512m"
opennms_home="${install.dir}"

app_class=org.opennms.upgrade.support.Upgrade

exec $opennms_home/bin/runjava -r -- $JAVA_OPTIONS \
    -Dopennms.home=$opennms_home \
    -Dopennms.manager.class=$app_class \
    -jar $opennms_home/lib/opennms_bootstrap.jar $@
