#!/bin/bash -e
# =====================================================================
# Build script running OpenNMS Sentinel in Docker environment
#
# Source: https://github.com/opennms-forge/docker-sentinel
# Web: https://www.opennms.org
#
# =====================================================================

# Cause false/positives
# shellcheck disable=SC2086

umask 002
SENTINEL_OVERLAY_ETC="/opt/sentinel-etc-overlay"
SENTINEL_OVERLAY="/opt/sentinel-overlay"

export KARAF_OPTS="-Djava.locale.providers=CLDR,COMPAT"

# Error codes
E_ILLEGAL_ARGS=126

# Help function used in error messages and -h option
usage() {
    echo ""
    echo "Docker entry script for OpenNMS Sentinel service container"
    echo ""
    echo "-c: Start Sentinel and use environment credentials to register Sentinel on OpenNMS."
    echo "    WARNING: Credentials can be exposed via docker inspect and log files. Please consider to use -s option."
    echo "-s: Initialize a keystore file with credentials in /keystore/scv.jce."
    echo "    Mount /keystore to your local system or a volume to save the keystore file."
    echo "    You can mount the keystore file to ${SENTINEL_HOME}/etc/scv.jce and just use -f to start the Sentinel."
    echo "-f: Initialize and start OpenNMS Sentinel in foreground."
    echo "-d: Same as -f, but starts the OpenNMS Sentinel in debug mode"
    echo "-h: Show this help."
    echo ""
}

useEnvCredentials(){
  echo "WARNING: Credentials can be exposed via docker inspect and log files. Please consider to use a keystore file."
  echo "         You can initialize a keystore file with the -s option."
  ${SENTINEL_HOME}/bin/scvcli set opennms.http ${OPENNMS_HTTP_USER} ${OPENNMS_HTTP_PASS}
  ${SENTINEL_HOME}/bin/scvcli set opennms.broker ${OPENNMS_BROKER_USER} ${OPENNMS_BROKER_PASS}
}

setCredentials() {
  # Directory to initialize a new keystore file which can be mounted to the local host
  if [ -d /keystore ]; then
    mkdir /keystore
  fi

  read -r -p "Enter OpenNMS HTTP username: " OPENNMS_HTTP_USER
  read -r -s -p "Enter OpenNMS HTTP password: " OPENNMS_HTTP_PASS
  echo ""

  read -r -p "Enter OpenNMS Broker username: " OPENNMS_BROKER_USER
  read -r -s -p "Enter OpenNMS Broker password: " OPENNMS_BROKER_PASS
  echo ""

  ${SENTINEL_HOME}/bin/scvcli set opennms.http ${OPENNMS_HTTP_USER} ${OPENNMS_HTTP_PASS}
  ${SENTINEL_HOME}/bin/scvcli set opennms.broker ${OPENNMS_BROKER_USER} ${OPENNMS_BROKER_PASS}

  cp ${SENTINEL_HOME}/etc/scv.jce /keystore
}

initConfig() {
    if [ ! -d ${SENTINEL_HOME} ]; then
        echo "OpenNMS Sentinel home directory doesn't exist in ${SENTINEL_HOME}."
        exit ${E_ILLEGAL_ARGS}
    fi

    if [ ! -f ${SENTINEL_HOME}/etc/configured ]; then
        # Expose Karaf Shell
        sed -i "/^sshHost/s/=.*/= 0.0.0.0/" ${SENTINEL_HOME}/etc/org.apache.karaf.shell.cfg

        # Expose the RMI registry and server
        sed -i "/^rmiRegistryHost/s/=.*/= 0.0.0.0/" ${SENTINEL_HOME}/etc/org.apache.karaf.management.cfg
        sed -i "/^rmiServerHost/s/=.*/= 0.0.0.0/" ${SENTINEL_HOME}/etc/org.apache.karaf.management.cfg

        # Set Sentinel location and connection to OpenNMS instance
        SENTINEL_CONFIG=${SENTINEL_HOME}/etc/org.opennms.sentinel.controller.cfg
        echo "location = ${SENTINEL_LOCATION}" > ${SENTINEL_CONFIG}
        echo "id = ${SENTINEL_ID:=$(uuidgen)}" >> ${SENTINEL_CONFIG}
        echo "broker-url = ${OPENNMS_BROKER_URL}" >> ${SENTINEL_CONFIG}
        echo "http-url = ${OPENNMS_HTTP_URL}" >> ${SENTINEL_CONFIG}

        # Configure datasource
        DB_CONFIG=${SENTINEL_HOME}/etc/org.opennms.netmgt.distributed.datasource.cfg
        echo "datasource.url = jdbc:postgresql://${POSTGRES_HOST}:${POSTGRES_PORT}/${POSTGRES_DB}" > ${DB_CONFIG}
        echo "datasource.username = ${POSTGRES_USER}" >> ${DB_CONFIG}
        echo "datasource.password = ${POSTGRES_PASSWORD}" >> ${DB_CONFIG}
        echo "datasource.databaseName = ${POSTGRES_DB}" >> ${DB_CONFIG}

        # Mark as configured
        echo "Configured $(date)" > ${SENTINEL_HOME}/etc/configured
    else
        echo "OpenNMS Sentinel is already configured, skipped."
    fi
}

applyOverlayConfig() {
  # Overlay etc specific config
  if [ -d "${SENTINEL_OVERLAY_ETC}" ] && [ -n "$(ls -A ${SENTINEL_OVERLAY_ETC})" ]; then
    echo "Apply custom etc configuration from ${SENTINEL_OVERLAY_ETC}."
    cp -r ${SENTINEL_OVERLAY_ETC}/* ${SENTINEL_HOME}/etc || exit ${E_INIT_CONFIG}
  else
    echo "No custom config found in ${SENTINEL_OVERLAY_ETC}. Use default configuration."
  fi
  # Overlay for all of the sentinel dir
  if [ -d "$SENTINEL_OVERLAY" ] && [ -n "$(ls -A ${SENTINEL_OVERLAY})" ]; then
    echo "Apply custom configuration from ${SENTINEL_OVERLAY}."
    cp -r ${SENTINEL_OVERLAY}/* ${SENTINEL_HOME}/ || exit ${E_INIT_CONFIG}
  else
    echo "No custom config found in ${SENTINEL_OVERLAY}. Use default configuration."
  fi 
}

applyKarafDebugLogging() {
  if [ -n "$KARAF_DEBUG_LOGGING" ]; then
    echo "Updating Karaf debug logging"
    for log in $(sed "s/,/ /g" <<< "$KARAF_DEBUG_LOGGING"); do
      logUnderscored=${log//./_}
      echo "log4j2.logger.${logUnderscored}.level = DEBUG" >> "$SENTINEL_HOME"/etc/org.ops4j.pax.logging.cfg
      echo "log4j2.logger.${logUnderscored}.name = $log" >> "$SENTINEL_HOME"/etc/org.ops4j.pax.logging.cfg
    done
  fi
}

start() {
    export KARAF_EXEC="exec"
    cd ${SENTINEL_HOME}/bin
    exec ./karaf server ${SENTINEL_DEBUG}
}

# Evaluate arguments for build script.
if [[ "${#}" == 0 ]]; then
    usage
    exit ${E_ILLEGAL_ARGS}
fi

# Evaluate arguments for build script.
while getopts csdfh flag; do
    case ${flag} in
        c)
            useEnvCredentials
            initConfig
            applyOverlayConfig
            applyKarafDebugLogging
            start
            ;;
        s)
            setCredentials
            ;;
        d)
            SENTINEL_DEBUG="debug"
            initConfig
            applyOverlayConfig
            applyKarafDebugLogging
            start
            ;;
        f)
            initConfig
            applyOverlayConfig
            applyKarafDebugLogging
            start
            ;;
        h)
            usage
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
    echo "Error: Too many arguments: ${*}."
    usage
    exit ${E_ILLEGAL_ARGS}
fi
