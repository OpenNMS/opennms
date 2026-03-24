#!/usr/bin/env bash
# =====================================================================
# Entrypoint for the Minion container image
# =====================================================================

# Cause false/positives
# shellcheck disable=SC2086

set -eE

trap 'rc=$?; echo "[Startup][ERROR] entrypoint failed at line ${LINENO}: ${BASH_COMMAND} (exit=${rc})"; exit ${rc}' ERR

umask 002
export MINION_HOME="/opt/minion"
export KARAF_HOME="${MINION_HOME}"

MINION_PROCESS_ENV_CFG="${MINION_HOME}/etc/minion-process.env"
MINION_SERVER_CERTS_CFG="${MINION_HOME}/etc/minion-server-certs.env"
MINION_OVERLAY_ETC="/opt/minion-etc-overlay"
CACERTS="${MINION_HOME}/cacerts"
FEATURES_BOOT_DIR="${MINION_HOME}/etc/featuresBoot.d"
FEATURES_BOOT_TEMPLATES_DIR="${FEATURES_BOOT_DIR}/templates"
export JAVA_OPTS="${JAVA_OPTS} -Xms${JAVA_MIN_MEM:-2g} -Xmx${JAVA_MAX_MEM:-2g}"

# Prometheus JMX Exporter Configuration
#
# The JMX exporter allows Prometheus to scrape JMX metrics from the OpenNMS Minion applications.
# The Prometheus JMX exporter needs to be enabled and is disabled by default.
#
# Requirements:
# - PROM_JMX_EXPORTER_ENABLED=true
# - All other settings are optional and have sensible defaults
#
# Default behavior:
# - Configuration is managed via environment variables, which can be set in the Dockerfile, via docker run -e, or in a docker-compose file.
# - Template uses key/values from /java/agent/prom-jmx-exporter
PROM_JMX_EXPORTER_ENABLED="${PROM_JMX_EXPORTER_ENABLED:-false}" # required
PROM_JMX_EXPORTER_JAR="${PROM_JMX_EXPORTER_JAR:-/opt/prom-jmx-exporter/jmx_prometheus_javaagent.jar}"
PROM_JMX_EXPORTER_PORT="${PROM_JMX_EXPORTER_PORT:-9299}"
PROM_JMX_EXPORTER_CONFIG="${PROM_JMX_EXPORTER_CONFIG:-/opt/prom-jmx-exporter/config.yaml}"

if [[ "${PROM_JMX_EXPORTER_ENABLED,,}" == "true" ]]; then
  export JAVA_OPTS="${JAVA_OPTS} -javaagent:${PROM_JMX_EXPORTER_JAR}=${PROM_JMX_EXPORTER_PORT}:${PROM_JMX_EXPORTER_CONFIG}"
fi

export JAVA_OPTS="$JAVA_OPTS -Djava.locale.providers=CLDR,COMPAT"
export JAVA_OPTS="$JAVA_OPTS $("${MINION_HOME}/bin/_module_opts.sh")"
export JAVA_OPTS="$JAVA_OPTS -Dopennms.home=${MINION_HOME}"
export JAVA_OPTS="$JAVA_OPTS -Djdk.util.zip.disableZip64ExtraFieldValidation=true"

# Error codes
E_ILLEGAL_ARGS=126

# Help function used in error messages and -h option
usage() {
    echo ""
    echo "Docker entry script for OpenNMS Minion service container"
    echo ""
    echo "-c: Start Minion and use environment credentials to register Minion on OpenNMS."
    echo "    WARNING: Credentials can be exposed via docker inspect and log files. Please consider to use -s option."
    echo "-s: Initialize a keystore file with credentials in /keystore/scv.jce."
    echo "    Mount /keystore to your local system or a volume to save the keystore file."
    echo "    You can mount the keystore file to ${MINION_HOME}/etc/scv.jce and just use -f to start the Minion."
    echo "-f: Initialize and start OpenNMS Minion in foreground."
    echo "-h: Show this help."
    echo ""
}

useEnvCredentials(){
  echo "WARNING: Credentials can be exposed via docker inspect and log files. Please consider to use a keystore file."
  echo "         You can initialize a keystore file with the -s option."
  ${MINION_HOME}/bin/scvcli set opennms.http ${OPENNMS_HTTP_USER} ${OPENNMS_HTTP_PASS}
  ${MINION_HOME}/bin/scvcli set opennms.broker ${OPENNMS_BROKER_USER} ${OPENNMS_BROKER_PASS}
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

  ${MINION_HOME}/bin/scvcli set opennms.http ${OPENNMS_HTTP_USER} ${OPENNMS_HTTP_PASS}
  ${MINION_HOME}/bin/scvcli set opennms.broker ${OPENNMS_BROKER_USER} ${OPENNMS_BROKER_PASS}

  rsync --out-format="%n %C" ${MINION_HOME}/etc/scv.jce /keystore/.
}

function updateConfig() {
    key=$1
    value=$2
    file=$3

    # Handling exceptions
    [ "$key" == "class.name" ]       && key="class-name"
    [ "$key" == "max.packet.size" ]  && key="maxPacketSize"
    [ "$key" == "template.timeout" ] && key="templateTimeout"

    # Omit $value here, in case there is sensitive information
    echo "[Configuring] '$key' in '$file'"

    # If config exists in file, replace it. Otherwise, append to file.
    if grep -E -q "^#?$key=" "$file"; then
        sed -r -i "s@^#?$key=.*@$key=$value@g" "$file" #note that no config values may contain an '@' char
    else
        echo "$key=$value" >> "$file"
    fi
}

function applyFeatureBootTemplates() {
    # Clean only files managed by this script; keep baseline boot files from the image/package.
    local managed_boot_files=(
      "kafka-ipc.boot"
      "kafka-rpc.boot"
      "kafka-sink.boot"
      "kafka-twin.boot"
      "grpc.boot"
      "disable-jms.boot"
      "jaeger.boot"
      "dominion-scv.boot"
    )
    local boot_file
    for boot_file in "${managed_boot_files[@]}"; do
      rm -f "${FEATURES_BOOT_DIR}/${boot_file}"
    done

    apply_template() {
        local name="$1"
        envsubst < "${FEATURES_BOOT_TEMPLATES_DIR}/${name}" > "${FEATURES_BOOT_DIR}/${name}"
        echo "[Features] Enabled: ${name}"
    }

    # IPC strategy — jms, kafka, or grpc
    case "${MINION_IPC:-}" in
      jms)
        echo "[Features] IPC strategy set to JMS."
        ;;
        kafka)
            apply_template "kafka-ipc.boot"
            apply_template "kafka-rpc.boot"
            apply_template "kafka-sink.boot"
            apply_template "kafka-twin.boot"
            apply_template "disable-jms.boot"
            ;;
        grpc)
            apply_template "grpc.boot"
            apply_template "disable-jms.boot"
            ;;
        *)
            echo "[Features] No IPC strategy set via MINION_IPC, using defaults."
            ;;
    esac

    # Standalone optional features
    [[ "${JAEGER_ENABLED:-false}"       == "true" ]] && apply_template "jaeger.boot" || true
    [[ "${DOMINION_SCV_ENABLED:-false}" == "true" ]] && apply_template "dominion-scv.boot" || true
}

function parseEnvironment() {
    # Configure additional features
    IFS=$'\n'

    for VAR in $(env)
    do
        env_var=$(echo "$VAR" | cut -d= -f1)
        env_val=$(echo "$VAR" | cut -d= -f2)

        if [ "${env_var}" == "JAVA_MIN_MEM" ]; then
          export JAVA_OPTS="$JAVA_OPTS -Xms${env_val}"
        fi
        if [ "${env_var}" == "JAVA_MAX_MEM" ]; then
          export JAVA_OPTS="$JAVA_OPTS -Xmx${env_val}"
        fi

        if [[ $env_var =~ ^KAFKA_IPC_ ]]; then
            ipc_name=$(echo "$env_var" | cut -d_ -f3- | tr '[:upper:]' '[:lower:]' | tr _ .)
            updateConfig "$ipc_name" "${!env_var}" "${MINION_HOME}/etc/org.opennms.core.ipc.kafka.cfg"
        fi
    done
}

initConfig() {
    if [ ! -d ${MINION_HOME} ]; then
        echo "OpenNMS Minion home directory doesn't exist in ${MINION_HOME}."
        exit ${E_ILLEGAL_ARGS}
    fi

    if [ ! -f ${MINION_HOME}/etc/configured ]; then
        # Create SSH Key-Pair to use with the Karaf Shell
        mkdir -p "${MINION_HOME}/.ssh" && \
            chmod 700 "${MINION_HOME}/.ssh" && \
            ssh-keygen -t rsa -f "${MINION_HOME}/.ssh/id_rsa" -q -N "" && \
            echo "minion=$(cat "${MINION_HOME}/.ssh/id_rsa.pub" | awk '{print $2}'),viewer" > "${MINION_HOME}/etc/keys.properties" && \
            echo "_g_\\:admingroup = group,admin,manager,viewer,systembundles,ssh" >> ${MINION_HOME}/etc/keys.properties && \
            chmod 600 "${MINION_HOME}/.ssh/id_rsa"

        parseEnvironment
        applyFeatureBootTemplates

        echo "Configured $(date)" > ${MINION_HOME}/etc/configured
    else
        echo "OpenNMS Minion is already configured, skipped."
    fi
}

applyOverlayConfig() {
  # Overlay etc specific config
  if [ -d "${MINION_OVERLAY_ETC}" ] && [ -n "$(ls -A ${MINION_OVERLAY_ETC})" ]; then
    echo "Apply custom etc configuration from ${MINION_OVERLAY_ETC}."
    rsync -Lr --out-format="%n %C" ${MINION_OVERLAY_ETC}/* ${MINION_HOME}/etc/. || exit ${E_INIT_CONFIG}
  else
    echo "No custom config found in ${MINION_OVERLAY_ETC}. Use default configuration."
  fi
}

applyOpennmsPropertiesD() {
  find "${MINION_HOME}/etc/opennms.properties.d" -name '*.properties' | while IFS= read -r filename; do
    echo "appending to custom.system.properties: $filename"
    echo "" >> ${MINION_HOME}/etc/custom.system.properties
    cat "$filename" >> ${MINION_HOME}/etc/custom.system.properties
  done
}

printFeatureBootInventory() {
  echo "[Features] Active boot files in ${FEATURES_BOOT_DIR}:"
  if compgen -G "${FEATURES_BOOT_DIR}/*.boot" > /dev/null; then
    find "${FEATURES_BOOT_DIR}" -maxdepth 1 -name "*.boot" -type f | sort | sed "s#^${FEATURES_BOOT_DIR}/#  - #"
  else
    echo "  - (none)"
  fi
}

printStartupDiagnostics() {
  echo "[Startup] Effective minion mode and toggles:"
  echo "  MINION_IPC=${MINION_IPC:-<unset>}"
  echo "  MINION_VALIDATE_FEATURES_BOOT=${MINION_VALIDATE_FEATURES_BOOT:-false}"
  echo "  MINION_REPAIR_FEATURES_BOOT=${MINION_REPAIR_FEATURES_BOOT:-true}"
  echo "  JAEGER_ENABLED=${JAEGER_ENABLED:-false}"
  echo "  DOMINION_SCV_ENABLED=${DOMINION_SCV_ENABLED:-false}"
  echo "  GRPC_CLIENT_HOST=${IPC_GRPC_HOST:-<unset>}"
  echo "  GRPC_CLIENT_PORT=${IPC_GRPC_PORT:-<unset>}"
  echo "  KAFKA_BOOTSTRAP_SERVERS=${KAFKA_IPC_BOOTSTRAP_SERVERS:-<unset>}"

  print_cfg() {
    local cfg="$1"
    local path="${MINION_HOME}/etc/${cfg}"
    echo "[Startup] ${cfg}:"
    if [[ -f "${path}" ]]; then
      sed -n '1,120p' "${path}" | sed 's/^/  /'
    else
      echo "  (missing)"
    fi
  }

  print_cfg "org.opennms.minion.controller.cfg"
  print_cfg "org.opennms.core.ipc.grpc.client.cfg"
  print_cfg "org.opennms.core.ipc.kafka.cfg"
}

printKarafResolutionDiagnostics() {
  local ver
  local root="${MINION_HOME}/system/org/opennms/karaf/opennms"

  ver="$(grep -E 'mvn:org\.opennms\.karaf/opennms/.*/xml/features' "${MINION_HOME}/etc/org.apache.karaf.features.cfg" \
    | sed -E 's#.*mvn:org\.opennms\.karaf/opennms/([^/]+)/xml/features.*#\1#' \
    | head -n1)"

  echo "[Karaf] Repository diagnostics:"
  echo "  configured-opennms-features-version=${ver:-<unknown>}"

  if [[ -n "${ver}" && -d "${root}/${ver}" ]]; then
    echo "  local-repo-path=${root}/${ver}"
    find "${root}/${ver}" -maxdepth 1 -type f -name "*features*.xml" | sed 's/^/  found-feature-xml=/' || true
  else
    echo "  local-repo-path=${root}/${ver} (missing)"
  fi

  echo "[Karaf] org.ops4j.pax.url.mvn.cfg:"
  if [[ "${MINION_DEBUG_KARAF_RESOLUTION_DIAGNOSTICS:-false}" == "true" ]]; then  
    if [[ -f "${MINION_HOME}/etc/org.ops4j.pax.url.mvn.cfg" ]]; then  
      sed -n '1,200p' "${MINION_HOME}/etc/org.ops4j.pax.url.mvn.cfg" | sed 's/^/  /'  
    else  
      echo "  (missing)"  
    fi  
  else  
    echo "  (redacted; set MINION_DEBUG_KARAF_RESOLUTION_DIAGNOSTICS=true to print contents)"
  fi
}

validateFeatureBootComposition() {
  local strict_validation="${MINION_VALIDATE_FEATURES_BOOT:-false}"
  local repair_missing="${MINION_REPAIR_FEATURES_BOOT:-true}"
  local missing=0

  require_boot_file() {
    local name="$1"
    if [[ ! -f "${FEATURES_BOOT_DIR}/${name}" ]]; then
      echo "[Features][ERROR] Missing required boot file: ${name}"
      missing=1
    fi
  }

  check_required_boot_files() {
    missing=0

    case "${MINION_IPC:-}" in
      kafka)
        require_boot_file "kafka-ipc.boot"
        require_boot_file "kafka-rpc.boot"
        require_boot_file "kafka-sink.boot"
        require_boot_file "kafka-twin.boot"
        require_boot_file "disable-jms.boot"
        ;;
      grpc)
        require_boot_file "grpc.boot"
        require_boot_file "disable-jms.boot"
        ;;
      jms|"")
        # Default/package-provided feature boots are expected to cover JMS.
        ;;
      *)
        echo "[Features][WARN] Unknown MINION_IPC='${MINION_IPC}', skipping strategy-specific checks."
        ;;
    esac

    [[ "${JAEGER_ENABLED:-false}" == "true" ]] && require_boot_file "jaeger.boot" || echo "[Features][INFO] Jaeger boot file not required because JAEGER_ENABLED=${JAEGER_ENABLED:-false}."
    [[ "${DOMINION_SCV_ENABLED:-false}" == "true" ]] && require_boot_file "dominion-scv.boot" || echo "[Features][INFO] Dominion SCV boot file not required because DOMINION_SCV_ENABLED=${DOMINION_SCV_ENABLED:-false}."
  }

  printFeatureBootInventory

  check_required_boot_files

  if [[ "$missing" -ne 0 && "${repair_missing}" == "true" ]]; then
    echo "[Features][WARN] Missing boot files detected; attempting auto-repair from templates."
    applyFeatureBootTemplates
    printFeatureBootInventory
    check_required_boot_files
  fi

  if [[ "$missing" -ne 0 ]]; then
    if [[ "${strict_validation}" != "true" ]]; then
      echo "[Features][WARN] Boot file validation found missing files; continuing because MINION_VALIDATE_FEATURES_BOOT=${strict_validation}."
      return 0
    fi
    echo "[Features][ERROR] Boot file validation failed; refusing to start with incomplete feature composition."
    return 1
  fi

  echo "[Features] Boot file validation passed."
}

start() {
    export KARAF_EXEC="exec"
    cd ${MINION_HOME}/bin
    exec ./karaf server
}

# Order of precedence is (later overwrites former):
# 1. Config set via environment variable
# 2. Config set via direct file overlay
configure() {
  initConfig
  applyOpennmsPropertiesD
  applyOverlayConfig
  if [[ "$JACOCO_AGENT_ENABLED" -gt 0 ]]; then
    export JAVA_OPTS="$JAVA_OPTS -javaagent:${MINION_HOME}/agent/jacoco-agent.jar=output=none,jmx=true,excludes=org.drools.*"
  fi
  if [[ -f "$MINION_PROCESS_ENV_CFG" ]]; then
    while read assignment; do
      [[ $assignment =~ ^#.* ]] && continue
      export "$assignment"
    done < "$MINION_PROCESS_ENV_CFG"
    export JAVA_OPTS="$CUSTOM_JAVA_OPTS $JAVA_OPTS"
  fi
  if [[ ${MINION_DEBUG_STARTUP_DIAGNOSTICS:-false} == "true" ]]; then
    printStartupDiagnostics
  fi
  if [[ "${MINION_DEBUG_KARAF_RESOLUTION_DIAGNOSTICS:-false}" == "true" ]]; then
    printKarafResolutionDiagnostics
  fi
  validateFeatureBootComposition
  if [[ -f "$MINION_SERVER_CERTS_CFG" ]]; then
    # cacerts is a symlink to a file, so *do not* put /. on the target
    rsync --out-format="%n %C" "$JAVA_HOME/lib/security/cacerts" "$CACERTS"
    export JAVA_OPTS="$JAVA_OPTS -Djavax.net.ssl.trustStore=$CACERTS -Djavax.net.ssl.trustStorePassword=changeit"
    while read certid; do
      [[ $certid =~ ^#.* ]] && continue
      # check if CA cert already exists and remove so re-adding doesn't error
      if keytool -list -alias "$certid" -keystore "$CACERTS" -storepass changeit; then
        keytool -delete -alias "$certid" -keystore "$CACERTS" -storepass changeit
      fi
      keytool -importcert -file "/opt/minion/server-certs/$certid" -alias "$certid" -keystore "$CACERTS" -storepass changeit -noprompt
    done < "$MINION_SERVER_CERTS_CFG"
  fi
}

# Evaluate arguments for build script.
if [[ "${#}" == 0 ]]; then
    usage
    exit ${E_ILLEGAL_ARGS}
fi

# Evaluate arguments for build script.
while getopts csfh flag; do
    case ${flag} in
        c)
            useEnvCredentials
            configure
            start
            ;;
        s)
            setCredentials
            ;;
        f)
            configure
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
    echo "Error: To many arguments: ${*}."
    usage
    exit ${E_ILLEGAL_ARGS}
fi
