#!/bin/bash -e
# =====================================================================
# Build script running OpenNMS in Docker environment
#
# Source: https://github.com/opennms-forge/docker-horizon-core-web
# Web: https://www.opennms.org
#
# =====================================================================

# Cause false/positives
# shellcheck disable=SC2086

umask 002
OPENNMS_HOME="/opt/opennms"

OPENNMS_OVERLAY="/opt/opennms-overlay"
OPENNMS_OVERLAY_ETC="/opt/opennms-etc-overlay"
OPENNMS_OVERLAY_JETTY_WEBINF="/opt/opennms-jetty-webinf-overlay"

# Error codes
E_ILLEGAL_ARGS=126
E_INIT_CONFIG=127

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
  echo "-t: Run the config-tester, e.g -t -h to show help and available options."
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

processConfdTemplates() {
  echo "Processing confd templates using /etc/confd/confd.toml"
  confd -onetime
}

# Initialize database and configure Karaf
initConfigWhenEmpty() {
  if [ ! -d ${OPENNMS_HOME} ]; then
    echo "OpenNMS home directory doesn't exist in ${OPENNMS_HOME}."
    exit ${E_ILLEGAL_ARGS}
  fi

  if [ ! "$(ls --ignore .git --ignore .gitignore -A ${OPENNMS_HOME}/etc)"  ]; then
    echo "No existing configuration in ${OPENNMS_HOME}/etc found. Initialize from etc-pristine."
    cp -r ${OPENNMS_HOME}/share/etc-pristine/* ${OPENNMS_HOME}/etc/ || exit ${E_INIT_CONFIG}
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
    rsync -K -rl ${OPENNMS_OVERLAY}/* ${OPENNMS_HOME}/ || exit ${E_INIT_CONFIG}
  else
    echo "No custom config found in ${OPENNMS_OVERLAY}. Use default configuration."
  fi

  # Overlay etc specific config
  if [ -d "${OPENNMS_OVERLAY_ETC}" ] && [ -n "$(ls -A ${OPENNMS_OVERLAY_ETC})" ]; then
    echo "Apply custom etc configuration from ${OPENNMS_OVERLAY_ETC}."
    cp -r ${OPENNMS_OVERLAY_ETC}/* ${OPENNMS_HOME}/etc || exit ${E_INIT_CONFIG}
  else
    echo "No custom config found in ${OPENNMS_OVERLAY_ETC}. Use default configuration."
  fi

  # Overlay jetty specific config
  if [ -d "${OPENNMS_OVERLAY_JETTY_WEBINF}" ] && [ -n "$(ls -A ${OPENNMS_OVERLAY_JETTY_WEBINF})" ]; then
    echo "Apply custom Jetty WEB-INF configuration from ${OPENNMS_OVERLAY_JETTY_WEBINF}."
    cp -r ${OPENNMS_OVERLAY_JETTY_WEBINF}/* ${OPENNMS_HOME}/jetty-webapps/opennms/WEB-INF || exit ${E_INIT_CONFIG}
  else
    echo "No custom Jetty WEB-INF config found in ${OPENNMS_OVERLAY_JETTY_WEBINF}. Use default configuration."
  fi
}

# Start opennms in foreground
start() {
  local OPENNMS_JAVA_OPTS="--add-modules=java.base,java.compiler,java.datatransfer,java.desktop,java.instrument,java.logging,java.management,java.management.rmi,java.naming,java.prefs,java.rmi,java.scripting,java.security.jgss,java.security.sasl,java.sql,java.sql.rowset,java.xml,jdk.attach,jdk.httpserver,jdk.jdi,jdk.sctp,jdk.security.auth,jdk.xml.dom \
  -Dorg.apache.jasper.compiler.disablejsr199=true
  -Dopennms.home=/opt/opennms
  -XX:+HeapDumpOnOutOfMemoryError
  -Dcom.sun.management.jmxremote.authenticate=true
  -Dcom.sun.management.jmxremote.login.config=opennms
  -Dcom.sun.management.jmxremote.access.file=/opt/opennms/etc/jmxremote.access
  -DisThreadContextMapInheritable=true
  -Dgroovy.use.classvalue=true
  -Djava.io.tmpdir=/opt/opennms/data/tmp
  -Djava.locale.providers=CLDR,COMPAT
  -XX:+StartAttachListener"
  exec ${JAVA_HOME}/bin/java ${OPENNMS_JAVA_OPTS} ${JAVA_OPTS} -jar /opt/opennms/lib/opennms_bootstrap.jar start
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
      processConfdTemplates
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
      processConfdTemplates
      applyOverlayConfig
      configTester -a
      initOrUpdate -dis
      exit
      ;;
    s)
      initConfigWhenEmpty
      processConfdTemplates
      applyOverlayConfig
      configTester -a
      initOrUpdate -dis
      start
      exit
      ;;
    t)
      configTester "${@}"
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
