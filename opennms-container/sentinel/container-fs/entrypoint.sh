#!/usr/bin/env bash
# =====================================================================
# Build script running OpenNMS Sentinel in Docker environment
#
# Source: https://github.com/opennms-forge/docker-sentinel
# Web: https://www.opennms.org
#
# =====================================================================

# Cause false/positives
# shellcheck disable=SC2086

set -eE

# shellcheck disable=SC2064
trap 'rc=$?; echo "[Startup][ERROR] entrypoint failed at line ${LINENO} (exit=${rc})"; exit ${rc}' ERR

umask 002
export SENTINEL_HOME="/opt/sentinel"
export KARAF_HOME="${SENTINEL_HOME}"

SENTINEL_OVERLAY_ETC="/opt/sentinel-etc-overlay"
SENTINEL_OVERLAY="/opt/sentinel-overlay"
FEATURES_BOOT_DIR="${SENTINEL_HOME}/etc/featuresBoot.d"
FEATURES_BOOT_TEMPLATES_DIR="${FEATURES_BOOT_DIR}/templates"

# Prometheus JMX Exporter Configuration
#
# The JMX exporter allows Prometheus to scrape JMX metrics from the OpenNMS Sentinel applications.
# The Prometheus JMX exporter needs to be enabled and is disabled by default.
#
# Requirements:
# - PROM_JMX_EXPORTER_ENABLED=true
# - All other settings are optional and have sensible defaults
#
# Default behavior:
# - Configuration is managed via environment variables, which can be set in the Dockerfile, via docker run -e, or in a docker-compose file.
PROM_JMX_EXPORTER_ENABLED="${PROM_JMX_EXPORTER_ENABLED:-false}" # required
PROM_JMX_EXPORTER_JAR="${PROM_JMX_EXPORTER_JAR:-/opt/prom-jmx-exporter/jmx_prometheus_javaagent.jar}"
PROM_JMX_EXPORTER_PORT="${PROM_JMX_EXPORTER_PORT:-9299}"
PROM_JMX_EXPORTER_CONFIG="${PROM_JMX_EXPORTER_CONFIG:-/opt/prom-jmx-exporter/config.yaml}"

if [[ "${PROM_JMX_EXPORTER_ENABLED,,}" == "true" ]]; then
  export JAVA_OPTS="${JAVA_OPTS} -javaagent:${PROM_JMX_EXPORTER_JAR}=${PROM_JMX_EXPORTER_PORT}:${PROM_JMX_EXPORTER_CONFIG}"
fi

export JAVA_OPTS="$JAVA_OPTS -Djava.locale.providers=CLDR,COMPAT"
export JAVA_OPTS="$JAVA_OPTS $("${SENTINEL_HOME}/bin/_module_opts.sh")"
export JAVA_OPTS="$JAVA_OPTS -Dopennms.home=${SENTINEL_HOME}"
export JAVA_OPTS="$JAVA_OPTS -Djdk.util.zip.disableZip64ExtraFieldValidation=true"

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
  mkdir -p /keystore

  read -r -p "Enter OpenNMS HTTP username: " OPENNMS_HTTP_USER
  read -r -s -p "Enter OpenNMS HTTP password: " OPENNMS_HTTP_PASS
  echo ""

  read -r -p "Enter OpenNMS Broker username: " OPENNMS_BROKER_USER
  read -r -s -p "Enter OpenNMS Broker password: " OPENNMS_BROKER_PASS
  echo ""

  ${SENTINEL_HOME}/bin/scvcli set opennms.http ${OPENNMS_HTTP_USER} ${OPENNMS_HTTP_PASS}
  ${SENTINEL_HOME}/bin/scvcli set opennms.broker ${OPENNMS_BROKER_USER} ${OPENNMS_BROKER_PASS}

  rsync --out-format="%n %C" ${SENTINEL_HOME}/etc/scv.jce /keystore/.
}

function updateConfig() {
    key=$1
    value=$2
    file=$3

    # Omit $value here, in case there is sensitive information
    echo "[Configuring] '$key' in '$file'"

    # If config exists in file, replace it. Otherwise, append to file.
    if grep -E -q "^#?\s*$key\s*=" "$file"; then
        sed -r -i "s@^#?\s*$key\s*=.*@$key=$value@g" "$file" #note that no config values may contain an '@' char
    else
        echo "$key=$value" >> "$file"
    fi
}

function parseEnvironment() {
    IFS=$'\n'

    for VAR in $(env)
    do
        env_var=$(echo "$VAR" | cut -d= -f1)

        if [[ $env_var =~ ^KAFKA_IPC_ ]]; then
            ipc_name=$(echo "$env_var" | cut -d_ -f3- | tr '[:upper:]' '[:lower:]' | tr _ .)
            updateConfig "$ipc_name" "${!env_var}" "${SENTINEL_HOME}/etc/org.opennms.core.ipc.sink.kafka.cfg"
            updateConfig "$ipc_name" "${!env_var}" "${SENTINEL_HOME}/etc/org.opennms.core.ipc.sink.kafka.consumer.cfg"
        fi

        if [[ $env_var =~ ^ELASTICSEARCH_ ]]; then
            es_name=$(echo "$env_var" | cut -d_ -f2- | tr '[:upper:]' '[:lower:]' | tr _ .)
            case "$es_name" in
              url)            es_key="elasticUrl" ;;
              index.strategy) es_key="elasticIndexStrategy" ;;
              replicas)       es_key="settings.index.number_of_replicas" ;;
              conn.timeout)   es_key="connTimeout" ;;
              read.timeout)   es_key="readTimeout" ;;
              *)              es_key="$es_name" ;;
            esac
            updateConfig "$es_key" "${!env_var}" "${SENTINEL_HOME}/etc/org.opennms.features.flows.persistence.elastic.cfg"
        fi

        if [[ $env_var == "OPENNMS_INSTANCE_ID" ]]; then
            updateConfig "org.opennms.instance.id" "${!env_var}" "${SENTINEL_HOME}/etc/custom.system.properties"
        fi
    done
}

function applyFeatureBootTemplates() {
    local managed_boot_files=(
      "sentinel-ipc.boot"
    )
    local boot_file
    for boot_file in "${managed_boot_files[@]}"; do
      rm -f "${FEATURES_BOOT_DIR}/${boot_file}"
    done

    apply_template() {
        local name="$1"
        cp "${FEATURES_BOOT_TEMPLATES_DIR}/${name}" "${FEATURES_BOOT_DIR}/${name}"
        echo "[Features] Enabled: ${name}"
    }

    case "${SENTINEL_IPC:-}" in
      kafka)
        echo "[Features] IPC strategy set to Kafka."
        apply_template "sentinel-ipc.boot"
        ;;
      jms|"")
        echo "[Features] IPC strategy set to JMS (default)."
        ;;
      *)
        echo "[Features] Unknown IPC strategy '${SENTINEL_IPC}', using defaults."
        ;;
    esac
}

initConfig() {
    if [ ! -d ${SENTINEL_HOME} ]; then
        echo "OpenNMS Sentinel home directory doesn't exist in ${SENTINEL_HOME}."
        exit ${E_ILLEGAL_ARGS}
    fi

    if [ ! -f ${SENTINEL_HOME}/etc/configured ]; then
        # Create SSH Key-Pair to use with the Karaf Shell
        mkdir -p "${SENTINEL_HOME}/.ssh" && \
            chmod 700 "${SENTINEL_HOME}/.ssh" && \
            ssh-keygen -t rsa -f "${SENTINEL_HOME}/.ssh/id_rsa" -q -N "" && \
            echo "sentinel=$(cat "${SENTINEL_HOME}/.ssh/id_rsa.pub" | awk '{print $2}'),viewer" > "${SENTINEL_HOME}/etc/keys.properties" && \
            echo "_g_\\:admingroup = group,admin,manager,viewer,systembundles,ssh" >> "${SENTINEL_HOME}/etc/keys.properties" && \
            chmod 600 "${SENTINEL_HOME}/.ssh/id_rsa"

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

        # Configure datasource
        DB_CONFIG=${SENTINEL_HOME}/etc/org.opennms.netmgt.distributed.datasource.cfg
        echo "datasource.url = jdbc:postgresql://${POSTGRES_HOST}:${POSTGRES_PORT}/${POSTGRES_DB}" > ${DB_CONFIG}
        echo "datasource.username = ${POSTGRES_USER}" >> ${DB_CONFIG}
        echo "datasource.password = ${POSTGRES_PASSWORD}" >> ${DB_CONFIG}
        echo "datasource.databaseName = ${POSTGRES_DB}" >> ${DB_CONFIG}

        parseEnvironment
        applyFeatureBootTemplates

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
    rsync -r --out-format="%n %C" ${SENTINEL_OVERLAY_ETC}/* ${SENTINEL_HOME}/etc/. || exit ${E_INIT_CONFIG}
  else
    echo "No custom config found in ${SENTINEL_OVERLAY_ETC}. Use default configuration."
  fi
  # Overlay for all of the sentinel dir
  if [ -d "$SENTINEL_OVERLAY" ] && [ -n "$(ls -A ${SENTINEL_OVERLAY})" ]; then
    echo "Apply custom configuration from ${SENTINEL_OVERLAY}."
    rsync -r --out-format="%n %C" ${SENTINEL_OVERLAY}/* ${SENTINEL_HOME}/. || exit ${E_INIT_CONFIG}
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
  if [[ "$JACOCO_AGENT_ENABLED" -gt 0 ]]; then
    export JAVA_OPTS="$JAVA_OPTS -javaagent:${SENTINEL_HOME}/agent/jacoco-agent.jar=output=none,jmx=true,excludes=org.drools.*"
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
