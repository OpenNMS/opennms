#!/usr/bin/env bash
# =====================================================================
# Build script running OpenNMS in Docker environment
#
# Source: https://github.com/opennms-forge/docker-horizon-core-web
# Web: https://www.opennms.org
#
# =====================================================================

# Cause false/positives
# shellcheck disable=SC2086

set -e
set -x

umask 002
export OPENNMS_HOME="/opt/opennms"
export KARAF_HOME="${OPENNMS_HOME}"

OPENNMS_OVERLAY="/opt/opennms-overlay"
OPENNMS_OVERLAY_ETC="/opt/opennms-etc-overlay"
OPENNMS_OVERLAY_JETTY_WEBINF="/opt/opennms-jetty-webinf-overlay"

# Prometheus JMX Exporter Configuration
#
# The JMX exporter allows Prometheus to scrape JMX metrics from the OpenNMS Core applications.
# The Prometheus JMX exporter needs to be enabled and is disabled by default.
#
# Requirements:
# - PROM_JMX_EXPORTER_ENABLED=true
# - All other settings are optional and have sensible defaults
#
# Default behavior:
# - Configuration is managed via environment variables
# - Default config is at /opt/prom-jmx-exporter/config.yaml; override with PROM_JMX_EXPORTER_CONFIG
PROM_JMX_EXPORTER_ENABLED="${PROM_JMX_EXPORTER_ENABLED:-false}" # required
PROM_JMX_EXPORTER_JAR="${PROM_JMX_EXPORTER_JAR:-/opt/prom-jmx-exporter/jmx_prometheus_javaagent.jar}"
PROM_JMX_EXPORTER_PORT="${PROM_JMX_EXPORTER_PORT:-9299}"
PROM_JMX_EXPORTER_DEFAULT_CONFIG="/opt/prom-jmx-exporter/config.yaml"
PROM_JMX_EXPORTER_CONFIG="${PROM_JMX_EXPORTER_CONFIG:-${PROM_JMX_EXPORTER_DEFAULT_CONFIG}}"
# First line of the generated config.yaml; a file at the default path without it is left alone
PROM_JMX_EXPORTER_GENERATED_MARKER="# GENERATED FROM PROM_JMX_* ENVIRONMENT VARIABLES"

if [[ "${PROM_JMX_EXPORTER_ENABLED,,}" == "true" ]]; then
  export JAVA_OPTS="${JAVA_OPTS} -javaagent:${PROM_JMX_EXPORTER_JAR}=${PROM_JMX_EXPORTER_PORT}:${PROM_JMX_EXPORTER_CONFIG}"
fi

# Error codes
E_ILLEGAL_ARGS=126
E_INIT_CONFIG=127

MYID="$(id -u)"
MYUSER="$(getent passwd "${MYID}" | cut -d: -f1)"

export RUNAS="${MYUSER}"

if [ "$MYID" -eq 0 ]; then
  if ! grep -Fxq "RUNAS=${MYUSER}" "${OPENNMS_HOME}/etc/opennms.conf"; then
      echo "RUNAS=${MYUSER}" >> "${OPENNMS_HOME}/etc/opennms.conf"
  fi
  chown "$MYUSER" "${OPENNMS_HOME}/etc/opennms.conf"
fi

# Help function used in error messages and -h option
usage() {
  echo ""
  echo "Docker entry script for OpenNMS service container"
  echo ""
  echo "Overlay Config file:"
  echo "If you want to overwrite the default configuration with your custom config, you can use an overlay config"
  echo "folder in which needs to be mounted to ${OPENNMS_OVERLAY_ETC}."
  echo "Every file in this folder is overwriting the default configuration file in ${OPENNMS_HOME}/etc."
  echo ""
  echo "-f: Start OpenNMS in foreground with existing data and configuration."
  echo "-h: Show this help."
  echo "-i: Initialize or update database and configuration files and do *NOT* start."
  echo "-s: Initialize or update database and configuration files and start OpenNMS."
  echo "-t: Run the config-tester against the configuration files."
  echo ""
}

initOrUpdate() {
  if [[ -f "${OPENNMS_HOME}"/etc/configured ]]; then
    echo "System is already configured. Enforce init or update by delete the ${OPENNMS_HOME}/etc/configured file."
  else
    echo "Find and set Java environment for running OpenNMS in ${OPENNMS_HOME}/etc/java.conf."
    "${OPENNMS_HOME}"/bin/runjava -s

    echo "Run OpenNMS install command to initialize or upgrade the database schema and configurations."
    ${JAVA_HOME}/bin/java -Dopennms.home="${OPENNMS_HOME}" -Dlog4j.configurationFile="${OPENNMS_HOME}"/etc/log4j2-tools.xml -cp "${OPENNMS_HOME}/lib/opennms_bootstrap.jar" org.opennms.bootstrap.InstallerBootstrap "${@}" || exit ${E_INIT_CONFIG}

    # If Newts is used initialize the keyspace with a given REPLICATION_FACTOR which defaults to 1 if unset
    if [[ "${OPENNMS_TIMESERIES_STRATEGY}" == "newts" ]]; then
      ${JAVA_HOME}/bin/java -Dopennms.manager.class="org.opennms.netmgt.newts.cli.Newts" -Dopennms.home="${OPENNMS_HOME}" -Dlog4j.configurationFile="${OPENNMS_HOME}"/etc/log4j2-tools.xml -jar ${OPENNMS_HOME}/lib/opennms_bootstrap.jar init -r ${REPLICATION_FACTOR-1} || exit ${E_INIT_CONFIG}
    else
      echo "The time series strategy ${OPENNMS_TIMESERIES_STRATEGY} is selected, skip Newts keyspace initialisation. If unset defaults to rrd to use RRDTool."
    fi
  fi
}

configTester() {
  echo "Run config tester to validate existing configuration files."
  ${JAVA_HOME}/bin/java -Dopennms.manager.class="org.opennms.netmgt.config.tester.ConfigTester" -Dopennms.home="${OPENNMS_HOME}" -Dlog4j.configurationFile="${OPENNMS_HOME}"/etc/log4j2-tools.xml -jar ${OPENNMS_HOME}/lib/opennms_bootstrap.jar "${@}" || exit ${E_INIT_CONFIG}
}

# Optional third argument: the legacy confd-era variable name, so the error names what the user may actually have set
validateBool() {
  local name="$1" value="$2" legacy="$3"
  local label="${name}${legacy:+ (or legacy ${legacy})}"
  if [[ ! "${value,,}" =~ ^(true|false)$ ]]; then
    echo "ERROR: ${label}='${value}' is not a valid boolean. Expected 'true' or 'false'." >&2
    exit ${E_INIT_CONFIG}
  fi
  # Normalise the variable to lowercase (True/TRUE -> true); the confd-era parsers were case-insensitive
  printf -v "${name}" '%s' "${value,,}"
}

validateInt() {
  local name="$1" value="$2" legacy="$3"
  local label="${name}${legacy:+ (or legacy ${legacy})}"
  if [[ ! "$value" =~ ^[0-9]+$ ]]; then
    echo "ERROR: ${label}='${value}' is not a valid non-negative integer." >&2
    exit ${E_INIT_CONFIG}
  fi
}

validateAddress() {
  local name="$1" value="$2"
  # Accept wildcard, IPv4, or IPv6
  if [[ "$value" != "*" && ! "$value" =~ ^[0-9a-fA-F:.]+$ ]]; then
    echo "ERROR: ${name}='${value}' is not a valid address. Expected '*' or an IP address." >&2
    exit ${E_INIT_CONFIG}
  fi
}

renderPromJmxExporterConfig() {
  (
    export PROM_JMX_START_DELAY_SECONDS="${PROM_JMX_START_DELAY_SECONDS:-0}"
    export PROM_JMX_LOWERCASE_OUTPUT_NAME="${PROM_JMX_LOWERCASE_OUTPUT_NAME:-true}"
    export PROM_JMX_LOWERCASE_OUTPUT_LABEL_NAMES="${PROM_JMX_LOWERCASE_OUTPUT_LABEL_NAMES:-true}"
    export PROM_JMX_AUTO_EXCLUDE_OBJECT_NAME_ATTRIBUTES="${PROM_JMX_AUTO_EXCLUDE_OBJECT_NAME_ATTRIBUTES:-true}"

    validateInt  PROM_JMX_START_DELAY_SECONDS                  "$PROM_JMX_START_DELAY_SECONDS"
    validateBool PROM_JMX_LOWERCASE_OUTPUT_NAME                "$PROM_JMX_LOWERCASE_OUTPUT_NAME"
    validateBool PROM_JMX_LOWERCASE_OUTPUT_LABEL_NAMES         "$PROM_JMX_LOWERCASE_OUTPUT_LABEL_NAMES"
    validateBool PROM_JMX_AUTO_EXCLUDE_OBJECT_NAME_ATTRIBUTES  "$PROM_JMX_AUTO_EXCLUDE_OBJECT_NAME_ATTRIBUTES"

    {
      echo "${PROM_JMX_EXPORTER_GENERATED_MARKER}"
      envsubst < "/opt/opennms/container-fs/etc/templates/prom-jmx-exporter-config.yaml.tmpl"
    } > "${PROM_JMX_EXPORTER_CONFIG}"
  )
}

processEnvConfig() {
  echo "Processing environment variable configuration"

  local CONTAINER_CONFIG_ETC="/opt/opennms/container-fs/etc"

  # Copy static config; Karaf resolves ${env:VAR:-default} in .cfg files at load time
  mkdir -p "${OPENNMS_HOME}/etc/opennms.properties.d"
  cp "${CONTAINER_CONFIG_ETC}/org.apache.karaf.shell.cfg" "${OPENNMS_HOME}/etc/"

  # Remove legacy confd-generated property files to prevent stale/duplicate settings
  rm -f "${OPENNMS_HOME}/etc/opennms.properties.d/"_confd.*.properties

  # Process Newts/Cassandra properties from template with defaults for unset variables
  (
    # Don't trace this subshell, it would print OPENNMS_CASSANDRA_PASSWORD to the log
    set +x
    export OPENNMS_CASSANDRA_HOSTNAME="${OPENNMS_CASSANDRA_HOSTNAME:-hostname}"
    export OPENNMS_CASSANDRA_KEYSPACE="${OPENNMS_CASSANDRA_KEYSPACE:-newts}"
    export OPENNMS_CASSANDRA_PORT="${OPENNMS_CASSANDRA_PORT:-9042}"
    export OPENNMS_CASSANDRA_USERNAME="${OPENNMS_CASSANDRA_USERNAME:-cassandra}"
    export OPENNMS_CASSANDRA_PASSWORD="${OPENNMS_CASSANDRA_PASSWORD:-cassandra}"
    export OPENNMS_CASSANDRA_DATACENTER="${OPENNMS_CASSANDRA_DATACENTER:-datacenter1}"

    validateInt OPENNMS_CASSANDRA_PORT "$OPENNMS_CASSANDRA_PORT"

    envsubst < "${CONTAINER_CONFIG_ETC}/templates/_container.newts.properties.tmpl" \
              > "${OPENNMS_HOME}/etc/opennms.properties.d/_container.newts.properties"
  )

  # Process timeseries/RRD properties from template with defaults for unset variables
  (
    export OPENNMS_RRD_STOREBYFOREIGNSOURCE="${OPENNMS_RRD_STOREBYFOREIGNSOURCE:-true}"
    export OPENNMS_TIMESERIES_STRATEGY="${OPENNMS_TIMESERIES_STRATEGY:-rrd}"
    export OPENNMS_RRD_INTERFACEJAR="${OPENNMS_RRD_INTERFACEJAR:-/usr/share/java/jrrd2.jar}"
    export OPENNMS_RRD_STRATEGYCLASS="${OPENNMS_RRD_STRATEGYCLASS:-org.opennms.netmgt.rrd.rrdtool.MultithreadedJniRrdStrategy}"
    export OPENNMS_LIBRARY_JRRD2="${OPENNMS_LIBRARY_JRRD2:-/usr/lib/jni/libjrrd2.so}"

    validateBool OPENNMS_RRD_STOREBYFOREIGNSOURCE "$OPENNMS_RRD_STOREBYFOREIGNSOURCE"

    envsubst < "${CONTAINER_CONFIG_ETC}/templates/_container.timeseries.properties.tmpl" \
              > "${OPENNMS_HOME}/etc/opennms.properties.d/_container.timeseries.properties"
  )

  # Process service-configuration.xml from template with defaults for unset variables
  (
    export CORE_SERVICE_ALARMD_ENABLED="${CORE_SERVICE_ALARMD_ENABLED:-true}"
    export CORE_SERVICE_BSMD_ENABLED="${CORE_SERVICE_BSMD_ENABLED:-true}"
    export CORE_SERVICE_TICKETER_ENABLED="${CORE_SERVICE_TICKETER_ENABLED:-true}"
    export CORE_SERVICE_CORRELATOR_ENABLED="${CORE_SERVICE_CORRELATOR_ENABLED:-false}"
    export CORE_SERVICE_QUEUED_ENABLED="${CORE_SERVICE_QUEUED_ENABLED:-true}"
    export CORE_SERVICE_ACTIOND_ENABLED="${CORE_SERVICE_ACTIOND_ENABLED:-true}"
    export CORE_SERVICE_NOTIFD_ENABLED="${CORE_SERVICE_NOTIFD_ENABLED:-true}"
    export CORE_SERVICE_SCRIPTD_ENABLED="${CORE_SERVICE_SCRIPTD_ENABLED:-true}"
    export CORE_SERVICE_RTCD_ENABLED="${CORE_SERVICE_RTCD_ENABLED:-true}"
    export CORE_SERVICE_POLLERD_ENABLED="${CORE_SERVICE_POLLERD_ENABLED:-true}"
    export CORE_SERVICE_SNMPPOLLER_ENABLED="${CORE_SERVICE_SNMPPOLLER_ENABLED:-false}"
    export CORE_SERVICE_ENHANCEDLINKD_ENABLED="${CORE_SERVICE_ENHANCEDLINKD_ENABLED:-true}"
    export CORE_SERVICE_COLLECTD_ENABLED="${CORE_SERVICE_COLLECTD_ENABLED:-true}"
    export CORE_SERVICE_DISCOVERY_ENABLED="${CORE_SERVICE_DISCOVERY_ENABLED:-true}"
    export CORE_SERVICE_VACUUMD_ENABLED="${CORE_SERVICE_VACUUMD_ENABLED:-true}"
    export CORE_SERVICE_EVENTTRANSLATOR_ENABLED="${CORE_SERVICE_EVENTTRANSLATOR_ENABLED:-true}"
    export CORE_SERVICE_PASSIVESTATUSD_ENABLED="${CORE_SERVICE_PASSIVESTATUSD_ENABLED:-true}"
    export CORE_SERVICE_STATSD_ENABLED="${CORE_SERVICE_STATSD_ENABLED:-true}"
    export CORE_SERVICE_PROVISIOND_ENABLED="${CORE_SERVICE_PROVISIOND_ENABLED:-true}"
    export CORE_SERVICE_ACKD_ENABLED="${CORE_SERVICE_ACKD_ENABLED:-true}"
    export CORE_SERVICE_JETTYSERVER_ENABLED="${CORE_SERVICE_JETTYSERVER_ENABLED:-true}"
    export CORE_SERVICE_KARAFSTARTUPMONITOR_ENABLED="${CORE_SERVICE_KARAFSTARTUPMONITOR_ENABLED:-true}"
    export CORE_SERVICE_SYSLOGD_ENABLED="${CORE_SERVICE_SYSLOGD_ENABLED:-false}"
    export CORE_SERVICE_TELEMETRYD_ENABLED="${CORE_SERVICE_TELEMETRYD_ENABLED:-true}"
    export CORE_SERVICE_TRAPD_ENABLED="${CORE_SERVICE_TRAPD_ENABLED:-true}"
    export CORE_SERVICE_PERSPECTIVEPOLLER_ENABLED="${CORE_SERVICE_PERSPECTIVEPOLLER_ENABLED:-true}"

    validateBool CORE_SERVICE_ALARMD_ENABLED              "$CORE_SERVICE_ALARMD_ENABLED"
    validateBool CORE_SERVICE_BSMD_ENABLED                "$CORE_SERVICE_BSMD_ENABLED"
    validateBool CORE_SERVICE_TICKETER_ENABLED            "$CORE_SERVICE_TICKETER_ENABLED"
    validateBool CORE_SERVICE_CORRELATOR_ENABLED          "$CORE_SERVICE_CORRELATOR_ENABLED"
    validateBool CORE_SERVICE_QUEUED_ENABLED              "$CORE_SERVICE_QUEUED_ENABLED"
    validateBool CORE_SERVICE_ACTIOND_ENABLED             "$CORE_SERVICE_ACTIOND_ENABLED"
    validateBool CORE_SERVICE_NOTIFD_ENABLED              "$CORE_SERVICE_NOTIFD_ENABLED"
    validateBool CORE_SERVICE_SCRIPTD_ENABLED             "$CORE_SERVICE_SCRIPTD_ENABLED"
    validateBool CORE_SERVICE_RTCD_ENABLED                "$CORE_SERVICE_RTCD_ENABLED"
    validateBool CORE_SERVICE_POLLERD_ENABLED             "$CORE_SERVICE_POLLERD_ENABLED"
    validateBool CORE_SERVICE_SNMPPOLLER_ENABLED          "$CORE_SERVICE_SNMPPOLLER_ENABLED"
    validateBool CORE_SERVICE_ENHANCEDLINKD_ENABLED       "$CORE_SERVICE_ENHANCEDLINKD_ENABLED"
    validateBool CORE_SERVICE_COLLECTD_ENABLED            "$CORE_SERVICE_COLLECTD_ENABLED"
    validateBool CORE_SERVICE_DISCOVERY_ENABLED           "$CORE_SERVICE_DISCOVERY_ENABLED"
    validateBool CORE_SERVICE_VACUUMD_ENABLED             "$CORE_SERVICE_VACUUMD_ENABLED"
    validateBool CORE_SERVICE_EVENTTRANSLATOR_ENABLED     "$CORE_SERVICE_EVENTTRANSLATOR_ENABLED"
    validateBool CORE_SERVICE_PASSIVESTATUSD_ENABLED      "$CORE_SERVICE_PASSIVESTATUSD_ENABLED"
    validateBool CORE_SERVICE_STATSD_ENABLED              "$CORE_SERVICE_STATSD_ENABLED"
    validateBool CORE_SERVICE_PROVISIOND_ENABLED          "$CORE_SERVICE_PROVISIOND_ENABLED"
    validateBool CORE_SERVICE_ACKD_ENABLED                "$CORE_SERVICE_ACKD_ENABLED"
    validateBool CORE_SERVICE_JETTYSERVER_ENABLED         "$CORE_SERVICE_JETTYSERVER_ENABLED"
    validateBool CORE_SERVICE_KARAFSTARTUPMONITOR_ENABLED "$CORE_SERVICE_KARAFSTARTUPMONITOR_ENABLED"
    validateBool CORE_SERVICE_SYSLOGD_ENABLED             "$CORE_SERVICE_SYSLOGD_ENABLED"
    validateBool CORE_SERVICE_TELEMETRYD_ENABLED          "$CORE_SERVICE_TELEMETRYD_ENABLED"
    validateBool CORE_SERVICE_TRAPD_ENABLED               "$CORE_SERVICE_TRAPD_ENABLED"
    validateBool CORE_SERVICE_PERSPECTIVEPOLLER_ENABLED   "$CORE_SERVICE_PERSPECTIVEPOLLER_ENABLED"

    envsubst < "${CONTAINER_CONFIG_ETC}/templates/service-configuration.xml.tmpl" \
              > "${OPENNMS_HOME}/etc/service-configuration.xml"
  )

  # Process prom-jmx-exporter config from template; only scalar knobs are exposed.
  # To customise includeObjectNames/excludeObjectNames/rules, mount a full YAML and
  # set PROM_JMX_EXPORTER_CONFIG to its path.
  # Only render when the exporter is enabled and reads the default path, and never replace
  # a file there that we didn't generate (e.g. one the user mounted).
  if [[ "${PROM_JMX_EXPORTER_ENABLED,,}" == "true" && "${PROM_JMX_EXPORTER_CONFIG}" == "${PROM_JMX_EXPORTER_DEFAULT_CONFIG}" ]]; then
    if [[ -e "${PROM_JMX_EXPORTER_CONFIG}" ]] && ! head -n 1 "${PROM_JMX_EXPORTER_CONFIG}" | grep -qF "${PROM_JMX_EXPORTER_GENERATED_MARKER}"; then
      echo "Using existing ${PROM_JMX_EXPORTER_CONFIG}; PROM_JMX_* variables are ignored."
    else
      renderPromJmxExporterConfig
    fi
  fi

  # Process trapd-configuration.xml from template with defaults for unset variables.
  # Falls back to the legacy confd-era names (e.g. OPENNMS_TRAPD_NEWSUSPECTONTRAP) so existing deployments keep working.
  (
    export OPENNMS_TRAPD_ADDRESS="${OPENNMS_TRAPD_ADDRESS:-*}"
    export OPENNMS_TRAPD_PORT="${OPENNMS_TRAPD_PORT:-1162}"
    export OPENNMS_TRAPD_NEW_SUSPECT_ON_TRAP="${OPENNMS_TRAPD_NEW_SUSPECT_ON_TRAP:-${OPENNMS_TRAPD_NEWSUSPECTONTRAP:-false}}"
    export OPENNMS_TRAPD_INCLUDE_RAW_MESSAGE="${OPENNMS_TRAPD_INCLUDE_RAW_MESSAGE:-${OPENNMS_TRAPD_INCLUDERAWMESSAGE:-false}}"
    export OPENNMS_TRAPD_THREADS="${OPENNMS_TRAPD_THREADS:-0}"
    export OPENNMS_TRAPD_QUEUE_SIZE="${OPENNMS_TRAPD_QUEUE_SIZE:-${OPENNMS_TRAPD_QUEUESIZE:-10000}}"
    export OPENNMS_TRAPD_BATCH_SIZE="${OPENNMS_TRAPD_BATCH_SIZE:-${OPENNMS_TRAPD_BATCHSIZE:-1000}}"
    export OPENNMS_TRAPD_BATCH_INTERVAL="${OPENNMS_TRAPD_BATCH_INTERVAL:-${OPENNMS_TRAPD_BATCHINTERVAL:-500}}"

    validateAddress  OPENNMS_TRAPD_ADDRESS             "$OPENNMS_TRAPD_ADDRESS"
    validateInt      OPENNMS_TRAPD_PORT                "$OPENNMS_TRAPD_PORT"
    validateBool     OPENNMS_TRAPD_NEW_SUSPECT_ON_TRAP "$OPENNMS_TRAPD_NEW_SUSPECT_ON_TRAP" OPENNMS_TRAPD_NEWSUSPECTONTRAP
    validateBool     OPENNMS_TRAPD_INCLUDE_RAW_MESSAGE "$OPENNMS_TRAPD_INCLUDE_RAW_MESSAGE" OPENNMS_TRAPD_INCLUDERAWMESSAGE
    validateInt      OPENNMS_TRAPD_THREADS             "$OPENNMS_TRAPD_THREADS"
    validateInt      OPENNMS_TRAPD_QUEUE_SIZE          "$OPENNMS_TRAPD_QUEUE_SIZE"          OPENNMS_TRAPD_QUEUESIZE
    validateInt      OPENNMS_TRAPD_BATCH_SIZE          "$OPENNMS_TRAPD_BATCH_SIZE"          OPENNMS_TRAPD_BATCHSIZE
    validateInt      OPENNMS_TRAPD_BATCH_INTERVAL      "$OPENNMS_TRAPD_BATCH_INTERVAL"      OPENNMS_TRAPD_BATCHINTERVAL

    envsubst < "${CONTAINER_CONFIG_ETC}/templates/trapd-configuration.xml.tmpl" \
              > "${OPENNMS_HOME}/etc/trapd-configuration.xml"
  )
}

# Initialize database and configure Karaf
initConfigWhenEmpty() {
  if [ ! -d ${OPENNMS_HOME} ]; then
    echo "OpenNMS home directory doesn't exist in ${OPENNMS_HOME}."
    exit ${E_ILLEGAL_ARGS}
  fi

  if [ ! "$(ls --ignore .git --ignore .gitignore -A ${OPENNMS_HOME}/etc)"  ]; then
    echo "No existing configuration in ${OPENNMS_HOME}/etc found. Initialize from etc-pristine."
    rsync -r --out-format="%n %C" ${OPENNMS_HOME}/share/etc-pristine/* ${OPENNMS_HOME}/etc/. || exit ${E_INIT_CONFIG}
  fi

  if [[ ! -d /opennms-data/mibs ]]; then
    echo "Mibs data directory does not exist, create directory in /opennms-data/mibs"
    mkdir /opennms-data/mibs || exit ${E_INIT_CONFIG}
  else
    echo "Use existing Mibs data directory."
  fi

  if [[ ! -d /opennms-data/reports ]]; then
    echo "Reports data directory does not exist, create directory in /opennms-data/reports"
    mkdir /opennms-data/reports || exit ${E_INIT_CONFIG}
  else
    echo "Use existing Reports data directory."
  fi

  if [[ ! -d /opennms-data/rrd ]]; then
    echo "RRD data directory does not exist, create directory in /opennms-data/rrd"
    mkdir /opennms-data/rrd || exit ${E_INIT_CONFIG}
  else
    echo "Use existing RRD data directory."
  fi
}

applyOverlayConfig() {
  # Overlay relative to the root of the install dir
  if [ -d "${OPENNMS_OVERLAY}" ] && [ -n "$(ls -A ${OPENNMS_OVERLAY})" ]; then
    echo "Apply custom configuration from ${OPENNMS_OVERLAY}."
    # Use rsync so that we can overlay files into directories that are symlinked
    rsync -K -rl --out-format="%n %C" ${OPENNMS_OVERLAY}/* ${OPENNMS_HOME}/. || exit ${E_INIT_CONFIG}
  else
    echo "No custom config found in ${OPENNMS_OVERLAY}. Use default configuration."
  fi

  # Overlay etc specific config
  if [ -d "${OPENNMS_OVERLAY_ETC}" ] && [ -n "$(ls -A ${OPENNMS_OVERLAY_ETC})" ]; then
    echo "Apply custom etc configuration from ${OPENNMS_OVERLAY_ETC}."
    rsync -r --out-format="%n %C" ${OPENNMS_OVERLAY_ETC}/* ${OPENNMS_HOME}/etc/. || exit ${E_INIT_CONFIG}
  else
    echo "No custom config found in ${OPENNMS_OVERLAY_ETC}. Use default configuration."
  fi

  # Overlay jetty specific config
  if [ -d "${OPENNMS_OVERLAY_JETTY_WEBINF}" ] && [ -n "$(ls -A ${OPENNMS_OVERLAY_JETTY_WEBINF})" ]; then
    echo "Apply custom Jetty WEB-INF configuration from ${OPENNMS_OVERLAY_JETTY_WEBINF}."
    rsync -r --out-format="%n %C" ${OPENNMS_OVERLAY_JETTY_WEBINF}/* ${OPENNMS_HOME}/jetty-webapps/opennms/WEB-INF/. || exit ${E_INIT_CONFIG}
  else
    echo "No custom Jetty WEB-INF config found in ${OPENNMS_OVERLAY_JETTY_WEBINF}. Use default configuration."
  fi

  applyLegacyConfdOverlays
}

# processEnvConfig removed every _confd.*.properties, so any still present came from an overlay.
# Under confd an overlaid _confd.X.properties replaced the generated file, so keep that behaviour:
# drop the matching _container.X.properties, which would otherwise sort after it and win.
applyLegacyConfdOverlays() {
  local propsD="${OPENNMS_HOME}/etc/opennms.properties.d"
  local legacy name
  for legacy in "${propsD}"/_confd.*.properties; do
    [[ -e "${legacy}" ]] || continue
    name="${legacy##*/_confd.}"
    echo "WARNING: Overlay supplies legacy ${legacy##*/}; it replaces _container.${name}. Rename it to _container.${name} or another name." >&2
    rm -f "${propsD}/_container.${name}"
  done
}

# Start opennms in foreground
start() {
  local OPENNMS_JAVA_OPTS="$("${OPENNMS_HOME}/bin/_module_opts.sh") \
  -Dorg.apache.jasper.compiler.disablejsr199=true
  -Dopennms.home=${OPENNMS_HOME}
  -Dopennms.pidfile=${OPENNMS_HOME}/logs/opennms.pid
  -XX:+HeapDumpOnOutOfMemoryError
  -Dcom.sun.management.jmxremote.authenticate=true
  -Dcom.sun.management.jmxremote.login.config=opennms
  -Dcom.sun.management.jmxremote.access.file=${OPENNMS_HOME}/etc/jmxremote.access
  -DisThreadContextMapInheritable=true
  -Djdk.attach.allowAttachSelf=true
  -Djdk.util.zip.disableZip64ExtraFieldValidation=true
  -Dgroovy.use.classvalue=true
  -Djava.io.tmpdir=${OPENNMS_HOME}/tmp
  -Dactivemq.data=${OPENNMS_HOME}/var/activemq
  -Djava.locale.providers=CLDR,COMPAT
  -XX:+StartAttachListener"
  exec ${JAVA_HOME}/bin/java ${OPENNMS_JAVA_OPTS} ${JAVA_OPTS} -jar ${OPENNMS_HOME}/lib/opennms_bootstrap.jar start
}

# Evaluate arguments for build script.
if [[ "${#}" == 0 ]]; then
  usage
  exit ${E_ILLEGAL_ARGS}
fi

# Evaluate arguments for build script.
while getopts "fhist" flag; do
  case ${flag} in
    f)
      processEnvConfig
      applyOverlayConfig
      configTester -a
      start
      exit
      ;;
    h)
      usage
      exit
      ;;
    i)
      initConfigWhenEmpty
      processEnvConfig
      applyOverlayConfig
      configTester -a
      initOrUpdate -dis
      exit
      ;;
    s)
      initConfigWhenEmpty
      processEnvConfig
      applyOverlayConfig
      configTester -a
      initOrUpdate -dis
      start
      exit
      ;;
    t)
      shift $((OPTIND - 1))
      configTester -a
      exit
      ;;
    *)
      usage
      exit ${E_ILLEGAL_ARGS}
      ;;
  esac
done

# Strip of all remaining arguments
shift $((OPTIND - 1));

# Check if there are remaining arguments
if [[ "${#}" -gt 0 ]]; then
  echo "Error: To many arguments: ${*}."
  usage
  exit ${E_ILLEGAL_ARGS}
fi
