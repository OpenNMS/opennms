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

OPENNMS_HOME=/opt/opennms

OPENNMS_DATASOURCES_TPL=/root/opennms-datasources.xml.tpl
OPENNMS_DATASOURCES_CFG=${OPENNMS_HOME}/etc/opennms-datasources.xml
OPENNMS_OVERLAY=/opt/opennms-overlay
OPENNMS_OVERLAY_ETC=/opt/opennms-etc-overlay
OPENNMS_OVERLAY_JETTY_WEBINF=/opt/opennms-jetty-webinf-overlay

OPENNMS_UPGRADE_GUARD=${OPENNMS_HOME}/etc/do-upgrade
OPENNMS_CONFIGURED_GUARD=${OPENNMS_HOME}/etc/configured

OPENNMS_KARAF_TPL=/root/org.apache.karaf.shell.cfg.tpl
OPENNMS_KARAF_CFG=${OPENNMS_HOME}/etc/org.apache.karaf.shell.cfg

OPENNMS_NEWTS_TPL=/root/newts.properties.tpl
OPENNMS_NEWTS_PROPERTIES=${OPENNMS_HOME}/etc/opennms.properties.d/newts.properties

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
  echo "-f: Start OpenNMS in foreground with an existing configuration."
  echo "-h: Show this help."
  echo "-i: Initialize Java environment, database and pristine OpenNMS configuration files and do *NOT* start OpenNMS."
  echo "    The database and config file initialization is skipped when a configured file exist."
  echo "-s: Initialize environment like -i and start OpenNMS in foreground."
  echo "-n: Initialize newts (cassandra) as well the initialisations steps in -i above."
  echo "    Initialization is skipped when a configured file exist."
  echo "-c: Initialize environment like -n and start OpenNMS in foreground using newts (cassandra)."
  echo "-t:  options: Run the config-tester, default is -h to show usage."
  echo ""
}

doInitOrUpgrade() {
  if [ -f ${OPENNMS_UPGRADE_GUARD} ]; then
    echo "Enforce config and database update."
    rm -rf ${OPENNMS_CONFIGURED_GUARD}
    ${OPENNMS_HOME}/bin/runjava -s
    ${OPENNMS_HOME}/bin/install -dis
    rm -rf ${OPENNMS_UPGRADE_GUARD}
    rm -rf ${OPENNMS_OVERLAY_ETC}/do-upgrade
    rm -rf ${OPENNMS_OVERLAY}/etc/do-upgrade
  fi
}

# Initialize database and configure Karaf
initConfig() {
  if [ ! -d ${OPENNMS_HOME} ]; then
    echo "OpenNMS home directory doesn't exist in ${OPENNMS_HOME}."
    exit ${E_ILLEGAL_ARGS}
  fi

  if [ ! "$(ls --ignore .git --ignore .gitignore --ignore ${OPENNMS_DATASOURCES_CFG} --ignore ${OPENNMS_KARAF_CFG} -A ${OPENNMS_HOME}/etc)"  ]; then
    echo "No existing configuration in ${OPENNMS_HOME}/etc found. Initialize from etc-pristine."
    cp -r ${OPENNMS_HOME}/share/etc-pristine/* ${OPENNMS_HOME}/etc/ || exit ${E_INIT_CONFIG}
  fi

  if [ ! -f ${OPENNMS_CONFIGURED_GUARD} ]; then
    echo "Initialize database and Karaf configuration and do install or upgrade the database schema."
    envsubst < ${OPENNMS_DATASOURCES_TPL} > ${OPENNMS_DATASOURCES_CFG}
    envsubst < ${OPENNMS_KARAF_TPL} > ${OPENNMS_KARAF_CFG}
    ${OPENNMS_HOME}/bin/runjava -s || exit ${E_INIT_CONFIG}
    ${OPENNMS_HOME}/bin/install -dis || exit ${E_INIT_CONFIG}
  fi
}

# run after initConfig to add cassandra/newts configuration
initNewtsConfig() {
  #re-initialising existing tables has no effect in newts so don't worry about guard
  echo "Initialize newts configuration and install newts keyspace in cassandra if not already initialised."
  envsubst < ${OPENNMS_NEWTS_TPL} > ${OPENNMS_NEWTS_PROPERTIES}
  ${OPENNMS_HOME}/bin/newts init || exit ${E_INIT_CONFIG}
}

applyOverlayConfig() {
  # Overlay relative to the root of the install dir
  if [ -d "${OPENNMS_OVERLAY}" ] && [ -n "$(ls -A ${OPENNMS_OVERLAY})" ]; then
    echo "Apply custom configuration from ${OPENNMS_OVERLAY}."
    cp -r ${OPENNMS_OVERLAY}/* ${OPENNMS_HOME}/ || exit ${E_INIT_CONFIG}
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

applyKarafDebugLogging() {
  if [ -n "${KARAF_DEBUG_LOGGING}" ]; then
    echo "Updating Karaf debug logging"
    for log in $(sed "s/,/ /g" <<< "${KARAF_DEBUG_LOGGING}"); do
      logUnderscored=${log//./_}
      echo "log4j2.logger.${logUnderscored}.level = DEBUG" >> "${OPENNMS_HOME}"/etc/org.ops4j.pax.logging.cfg
      echo "log4j2.logger.${logUnderscored}.name = $log" >> "${OPENNMS_HOME}"/etc/org.ops4j.pax.logging.cfg
    done
  fi
}

# Start opennms in foreground
start() {
  local OPENNMS_JAVA_OPTS="-Djava.endorsed.dirs=/opt/opennms/lib/endorsed \
  -Dopennms.home=/opt/opennms \
  -Dcom.sun.management.jmxremote.authenticate=true \
  -Dcom.sun.management.jmxremote.login.config=opennms \
  -Dcom.sun.management.jmxremote.access.file=/opt/opennms/etc/jmxremote.access \
  -DisThreadContextMapInheritable=true \
  -Dgroovy.use.classvalue=true \
  -Djava.io.tmpdir=/opt/opennms/data/tmp \
  -XX:+HeapDumpOnOutOfMemoryError"
  exec java ${OPENNMS_JAVA_OPTS} ${JAVA_OPTS} -jar /opt/opennms/lib/opennms_bootstrap.jar start
}

testConfig() {
  shift
  if [ "${#}" == "0" ]; then
    ${OPENNMS_HOME}/bin/config-tester -h
  else
    ${OPENNMS_HOME}/bin/config-tester "${@}" || exit ${E_INIT_CONFIG}
  fi
}

# Evaluate arguments for build script.
if [[ "${#}" == 0 ]]; then
  usage
  exit ${E_ILLEGAL_ARGS}
fi

# Evaluate arguments for build script.
while getopts "fhisnct" flag; do
  case ${flag} in
    f)
      applyOverlayConfig
      applyKarafDebugLogging
      testConfig -t -a
      start
      exit
      ;;
    h)
      usage
      exit
      ;;
    i)
      initConfig
      applyOverlayConfig
      applyKarafDebugLogging
      testConfig -t -a
      doInitOrUpgrade
      exit
      ;;
    s)
      initConfig
      applyOverlayConfig
      applyKarafDebugLogging
      testConfig -t -a
      doInitOrUpgrade
      start
      exit
      ;;
    n)
      echo "configuring opennms to use newts cassandra"
      initConfig
      initNewtsConfig
      applyOverlayConfig
      applyKarafDebugLogging
      testConfig -t -a
      doInitOrUpgrade
      exit
      ;;
    c)
      echo "starting opennms with newts cassandra"
      initConfig
      initNewtsConfig
      applyOverlayConfig
      applyKarafDebugLogging
      testConfig -t -a
      doInitOrUpgrade
      start
      exit
      ;;
    t)
      testConfig "${@}"
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
