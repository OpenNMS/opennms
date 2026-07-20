#!/bin/sh - 

JAVA_OPTIONS="-Xmx256m"
if [ -z "${OPENNMS_HOME}" ]; then
  _script="$0"
  case "$_script" in
    */*) ;;
    *) _script="$(command -v "$_script" 2>/dev/null || echo "$_script")" ;;
  esac
  OPENNMS_HOME="$(cd "$(dirname "$_script")/.." && pwd)"
  unset _script
fi
OPENNMS_BINDIR="${OPENNMS_HOME}/bin"

APP_CLASS=org.opennms.netmgt.config.DataCollectionConfigFactory

exec "$OPENNMS_BINDIR"/runjava -r -- $JAVA_OPTIONS \
	-Dopennms.home="$OPENNMS_HOME" \
	-Dopennms.manager.class="$APP_CLASS" \
	-Dlog4j.configurationFile="$OPENNMS_HOME"/etc/log4j2-tools.xml \
	-jar $OPENNMS_HOME/lib/opennms_bootstrap.jar
