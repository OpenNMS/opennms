#!/usr/bin/env bash
# =====================================================================
# Entrypoint for the Minion container image
# =====================================================================

# Cause false/positives
# shellcheck disable=SC2086

set -e

umask 002
export MINION_HOME="/opt/minion"
export KARAF_HOME="${MINION_HOME}"

MINION_CONFIG="${MINION_HOME}/etc/org.opennms.minion.controller.cfg"
MINION_PROCESS_ENV_CFG="${MINION_HOME}/etc/minion-process.env"
MINION_SERVER_CERTS_CFG="${MINION_HOME}/etc/minion-server-certs.env"
MINION_OVERLAY_ETC="/opt/minion-etc-overlay"
CUSTOM_SYSTEM_PROPERTIES="${MINION_HOME}/etc/custom.system.properties"
INSTANCE_ID_CFG="${MINION_HOME}/etc/instance-id.properties"
KARAF_SHELL_CFG="${MINION_HOME}/etc/org.apache.karaf.shell.cfg"
KARAF_MGMT_CFG="${MINION_HOME}/etc/org.apache.karaf.management.cfg"
JETTY_WEB_CFG="${MINION_HOME}/etc/org.ops4j.pax.web.cfg"
IPC_KAFKA_CFG="${MINION_HOME}/etc/org.opennms.core.ipc.kafka.cfg"
IPC_KAFKA_RPC_CFG="${MINION_HOME}/etc/org.opennms.core.ipc.rpc.kafka.cfg"
IPC_KAFKA_SINK_CFG="${MINION_HOME}/etc/org.opennms.core.ipc.sink.kafka.cfg"
IPC_KAFKA_TWIN_CFG="${MINION_HOME}/etc/org.opennms.core.ipc.twin.kafka.cfg"
IPC_KAFKA_SINK_OFFHEAP_CFG="${MINION_HOME}/etc/org.opennms.core.ipc.sink.offheap.cfg"
IPC_GRPC_CFG="${MINION_HOME}/etc/org.opennms.core.ipc.grpc.client.cfg"
DOMINION_GRPC_CFG="${MINION_HOME}/etc/org.opennms.features.minion.dominion.grpc.cfg"
SYSLOG_CFG="${MINION_HOME}/etc/org.opennms.netmgt.syslog.cfg"
TRAPD_CFG="${MINION_HOME}/etc/org.opennms.netmgt.trapd.cfg"
TELEMETRY_FEATURE_XML="${MINION_HOME}/deploy/confd-telemetry-feature.xml"
PROM_JMX_EXPORTER_CONFIG_PATH="/opt/prom-jmx-exporter/config.yaml"
FEATURES_BOOT_DIR="${MINION_HOME}/etc/featuresBoot.d"
KAFKA_IPC_BOOT="${FEATURES_BOOT_DIR}/kafka-ipc.boot"
KAFKA_RPC_BOOT="${FEATURES_BOOT_DIR}/kafka-rpc.boot"
KAFKA_SINK_BOOT="${FEATURES_BOOT_DIR}/kafka-sink.boot"
KAFKA_TWIN_BOOT="${FEATURES_BOOT_DIR}/kafka-twin.boot"
DISABLE_JMS_BOOT="${FEATURES_BOOT_DIR}/disable-jms.boot"
GRPC_BOOT="${FEATURES_BOOT_DIR}/grpc.boot"
DOMINION_SCV_BOOT="${FEATURES_BOOT_DIR}/dominion-scv.boot"
JAEGER_BOOT="${FEATURES_BOOT_DIR}/jaeger.boot"
CACERTS="${MINION_HOME}/cacerts"
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
# - Configuration is managed via environment variables
# - Settings are applied to ${PROM_JMX_EXPORTER_CONFIG_PATH}
PROM_JMX_EXPORTER_ENABLED="${PROM_JMX_EXPORTER_ENABLED:-false}" # required
PROM_JMX_EXPORTER_JAR="${PROM_JMX_EXPORTER_JAR:-/opt/prom-jmx-exporter/jmx_prometheus_javaagent.jar}"
PROM_JMX_EXPORTER_PORT="${PROM_JMX_EXPORTER_PORT:-9299}"
PROM_JMX_EXPORTER_CONFIG="${PROM_JMX_EXPORTER_CONFIG:-${PROM_JMX_EXPORTER_CONFIG_PATH}}"

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
    [ "$key" == "trapd.useaddressfromvarbind" ] && key="trapd.useAddressFromVarbind"
    [ "$key" == "trapd.newsuspectontrap" ] && key="trapd.newSuspectOnTrap"

    mkdir -p "$(dirname "$file")"
    [ -f "$file" ] || touch "$file"

    # Omit $value here, in case there is sensitive information
    echo "[Configuring] '$key' in '$file'"

    # If config exists in file, replace it. Otherwise, append to file.
    if grep -E -q "^#?$key=" "$file"; then
      sed -r -i "s@^#?$key=.*@$key=$value@g" "$file" #note that no config values may contain an '@' char
    else
      echo "$key=$value" >> "$file"
    fi
}

  function writeBootFile() {
    file=$1
    shift
    if [ "$#" -gt 0 ]; then
      printf "%s\n" "$@" > "$file"
    else
      rm -f "$file"
    fi
  }

  function mapKeyLowerDot() {
    echo "$1" | tr '[:upper:]' '[:lower:]' | tr _ .
  }

  function mapKeyPreserveCase() {
    echo "$1" | sed 's/__/./g'
  }

  function applyEnvPrefixToConfig() {
    prefix=$1
    targetFile=$2
    mapper=$3
    found=0

    IFS=$'\n'
    for VAR in $(env); do
      env_var=$(echo "$VAR" | cut -d= -f1)
      if [[ $env_var =~ ^${prefix} ]]; then
        env_val=$(echo "$VAR" | cut -d= -f2)
        key_raw=$(echo "$env_var" | sed "s/^${prefix}//")
        key=$($mapper "$key_raw")
        updateConfig "$key" "$env_val" "$targetFile"
        found=1
      fi
    done

    return $found
  }

  function writeSystemProperties() {
    found=0
    tmpfile=$(mktemp)

    IFS=$'\n'
    for VAR in $(env); do
      env_var=$(echo "$VAR" | cut -d= -f1)
      if [[ $env_var =~ ^SYSTEM_PROP_ ]]; then
        env_val=$(echo "$VAR" | cut -d= -f2)
        key_raw=$(echo "$env_var" | sed "s/^SYSTEM_PROP_//")
        key=$(mapKeyPreserveCase "$key_raw")
        echo "$key=$env_val" >> "$tmpfile"
        found=1
      fi
    done

    if [ "$found" -gt 0 ]; then
      mkdir -p "$(dirname "$CUSTOM_SYSTEM_PROPERTIES")"
      mv "$tmpfile" "$CUSTOM_SYSTEM_PROPERTIES"
    else
      rm -f "$tmpfile" "$CUSTOM_SYSTEM_PROPERTIES"
    fi
  }

  function writeProcessEnv() {
    if [ -n "${MINION_PROCESS_ENV_JAVA_OPTS}" ]; then
      mkdir -p "$(dirname "$MINION_PROCESS_ENV_CFG")"
      {
        echo "#"
        echo "# DON'T EDIT THIS FILE :: GENERATED FROM ENV"
        echo "#"
        echo "CUSTOM_JAVA_OPTS=${MINION_PROCESS_ENV_JAVA_OPTS}"
      } > "$MINION_PROCESS_ENV_CFG"
      return
    fi

    java_opts=""
    IFS=$'\n'
    for VAR in $(env); do
      env_var=$(echo "$VAR" | cut -d= -f1)
      if [[ $env_var =~ ^MINION_PROCESS_ENV_JAVA_OPT_ ]]; then
        env_val=$(echo "$VAR" | cut -d= -f2)
        if [ -z "$java_opts" ]; then
          java_opts="$env_val"
        else
          java_opts="$java_opts $env_val"
        fi
      fi
    done

    if [ -n "$java_opts" ]; then
      mkdir -p "$(dirname "$MINION_PROCESS_ENV_CFG")"
      {
        echo "#"
        echo "# DON'T EDIT THIS FILE :: GENERATED FROM ENV"
        echo "#"
        echo "CUSTOM_JAVA_OPTS=$java_opts"
      } > "$MINION_PROCESS_ENV_CFG"
    else
      rm -f "$MINION_PROCESS_ENV_CFG"
    fi
  }

  function writeServerCerts() {
    if [ -n "${MINION_SERVER_CERTS}" ]; then
      mkdir -p "$(dirname "$MINION_SERVER_CERTS_CFG")"
      echo "${MINION_SERVER_CERTS}" | tr ',' '\n' > "$MINION_SERVER_CERTS_CFG"
    else
      rm -f "$MINION_SERVER_CERTS_CFG"
    fi
  }

  function writePromJmxConfig() {
    if [ -n "${PROM_JMX_EXPORTER_CONFIG_YAML}" ]; then
      mkdir -p "$(dirname "$PROM_JMX_EXPORTER_CONFIG_PATH")"
      printf "%s\n" "$PROM_JMX_EXPORTER_CONFIG_YAML" > "$PROM_JMX_EXPORTER_CONFIG_PATH"
      return
    fi

    found=0
    start_delay=${PROM_JMX_EXPORTER_CFG_START_DELAY_SECONDS}
    lower_name=${PROM_JMX_EXPORTER_CFG_LOWER_CASE_OUTPUT_NAME}
    lower_labels=${PROM_JMX_EXPORTER_CFG_LOWERCASE_OUTPUT_LABEL_NAMES}
    auto_exclude=${PROM_JMX_EXPORTER_CFG_AUTO_EXCLUDE_OBJECT_NAME_ATTRIBUTES}
    include_objects=${PROM_JMX_EXPORTER_CFG_INCLUDE_OBJECT_NAMES}
    exclude_objects=${PROM_JMX_EXPORTER_CFG_EXCLUDE_OBJECT_NAMES}

    [ -n "$start_delay" ] && found=1
    [ -n "$lower_name" ] && found=1
    [ -n "$lower_labels" ] && found=1
    [ -n "$auto_exclude" ] && found=1
    [ -n "$include_objects" ] && found=1
    [ -n "$exclude_objects" ] && found=1

    if [ "$found" -eq 0 ]; then
      return
    fi

    mkdir -p "$(dirname "$PROM_JMX_EXPORTER_CONFIG_PATH")"
    {
      echo "#"
      echo "# DON'T EDIT THIS FILE :: GENERATED FROM ENV"
      echo "#"
      echo "startDelaySeconds: ${start_delay:-0}"
      echo "lowercaseOutputName: ${lower_name:-true}"
      echo "lowercaseOutputLabelNames: ${lower_labels:-true}"
      echo "autoExcludeObjectNameAttributes: ${auto_exclude:-true}"

      if [ -n "$include_objects" ]; then
        echo "includeObjectNames:"
        echo "$include_objects" | tr ',' '\n' | while read -r item; do
          [ -n "$item" ] && echo "- \"$item\""
        done
      fi

      if [ -n "$exclude_objects" ]; then
        echo "excludeObjectNames:"
        echo "$exclude_objects" | tr ',' '\n' | while read -r item; do
          [ -n "$item" ] && echo "- \"$item\""
        done
      fi

        cat <<'EOF'
rules:
- pattern: org\.opennms\..+\.(.+)<name=(.+)><>Value
  name: minion_$1_$2
  type: GAUGE

- pattern: org\.opennms\..+\.(.+)<name=(.+)><>Count
  name: minion_$1_$2_count
  type: COUNTER

- pattern: org\.opennms\..+\.(.+)<name=(.+)><>(\d+)thPercentile
  name: minion_$1_$2
  type: GAUGE
  labels:
    quantile: "0.$3"

- pattern: 'org\.opennms\.netmgt\.trapd\.device<location="([^"]+)", ip="([^"]+)", type=([^>]+)><>(\w+)'
  name: trapd_device_$4
  type: COUNTER
  labels:
    location: "$1"
    ip: "$2"
    type: "$3"
EOF
    } > "$PROM_JMX_EXPORTER_CONFIG_PATH"
  }

  function writeTelemetryFeatureXml() {
    if [ -n "${TELEMETRY_FEATURES_XML}" ]; then
      mkdir -p "$(dirname "$TELEMETRY_FEATURE_XML")"
      printf "%s\n" "$TELEMETRY_FEATURES_XML" > "$TELEMETRY_FEATURE_XML"
    else
      rm -f "$TELEMETRY_FEATURE_XML"
    fi
  }

function parseEnvironment() {
    # Configure additional features
  IFS=$'\n'
  kafka_ipc_bootstrap=""
  kafka_rpc_bootstrap=""
  kafka_sink_bootstrap=""
  kafka_twin_bootstrap=""
  grpc_host=""
  jaeger_enabled=""

  for VAR in $(env); do
    env_var=$(echo "$VAR" | cut -d= -f1)
    env_val=$(echo "$VAR" | cut -d= -f2)

    if [ "${env_var}" == "JAVA_MIN_MEM" ]; then
      export JAVA_OPTS="$JAVA_OPTS -Xms${env_val}"
    fi
    if [ "${env_var}" == "JAVA_MAX_MEM" ]; then
      export JAVA_OPTS="$JAVA_OPTS -Xmx${env_val}"
    fi

    if [[ $env_var =~ ^KAFKA_IPC_ ]]; then
      ipc_name=$(mapKeyLowerDot "$(echo "$env_var" | cut -d_ -f3-)")
      updateConfig "$ipc_name" "${!env_var}" "$IPC_KAFKA_CFG"
      if [[ "$ipc_name" == "bootstrap.servers" ]]; then
        kafka_ipc_bootstrap="${!env_var}"
      fi
    fi

    if [[ $env_var =~ ^KAFKA_RPC_IPC_ ]]; then
      ipc_name=$(mapKeyLowerDot "$(echo "$env_var" | cut -d_ -f4-)")
      updateConfig "$ipc_name" "${!env_var}" "$IPC_KAFKA_RPC_CFG"
      if [[ "$ipc_name" == "bootstrap.servers" ]]; then
        kafka_rpc_bootstrap="${!env_var}"
      fi
    fi

    if [[ $env_var =~ ^KAFKA_SINK_IPC_ ]]; then
      ipc_name=$(mapKeyLowerDot "$(echo "$env_var" | cut -d_ -f4-)")
      updateConfig "$ipc_name" "${!env_var}" "$IPC_KAFKA_SINK_CFG"
      if [[ "$ipc_name" == "bootstrap.servers" ]]; then
        kafka_sink_bootstrap="${!env_var}"
      fi
    fi

    if [[ $env_var =~ ^KAFKA_TWIN_IPC_ ]]; then
      ipc_name=$(mapKeyLowerDot "$(echo "$env_var" | cut -d_ -f4-)")
      updateConfig "$ipc_name" "${!env_var}" "$IPC_KAFKA_TWIN_CFG"
      if [[ "$ipc_name" == "bootstrap.servers" ]]; then
        kafka_twin_bootstrap="${!env_var}"
      fi
    fi

    if [[ $env_var =~ ^OFFHEAP_SINK_IPC_ ]]; then
      ipc_name=$(mapKeyLowerDot "$(echo "$env_var" | cut -d_ -f4-)")
      updateConfig "$ipc_name" "${!env_var}" "$IPC_KAFKA_SINK_OFFHEAP_CFG"
    fi

    if [[ $env_var =~ ^GRPC_IPC_ ]]; then
      ipc_name=$(mapKeyLowerDot "$(echo "$env_var" | cut -d_ -f3-)")
      updateConfig "$ipc_name" "${!env_var}" "$IPC_GRPC_CFG"
      if [[ "$ipc_name" == "host" ]]; then
        grpc_host="${!env_var}"
      fi
    fi

    if [[ $env_var =~ ^DOMINION_GRPC_ ]]; then
      grpc_name=$(mapKeyLowerDot "$(echo "$env_var" | cut -d_ -f3-)")
      if [[ "$grpc_name" == "client.secret" ]]; then
        grpc_name="clientSecret"
      fi
      updateConfig "$grpc_name" "${!env_var}" "$DOMINION_GRPC_CFG"
    fi

    if [[ $env_var =~ ^SYSLOG_CFG_ ]]; then
      cfg_name=$(mapKeyLowerDot "$(echo "$env_var" | cut -d_ -f3-)")
      updateConfig "$cfg_name" "${!env_var}" "$SYSLOG_CFG"
    fi

    if [[ $env_var =~ ^TRAPD_CFG_ ]]; then
      cfg_name=$(mapKeyLowerDot "$(echo "$env_var" | cut -d_ -f3-)")
      updateConfig "$cfg_name" "${!env_var}" "$TRAPD_CFG"
    fi

    if [[ $env_var =~ ^KARAF_SSH_ ]]; then
      cfg_name=$(echo "$env_var" | cut -d_ -f3- | tr '[:lower:]' '[:upper:]')
      if [[ "$cfg_name" == "PORT" ]]; then
        updateConfig "sshPort" "${!env_var}" "$KARAF_SHELL_CFG"
      elif [[ "$cfg_name" == "HOST" ]]; then
        updateConfig "sshHost" "${!env_var}" "$KARAF_SHELL_CFG"
      fi
    fi

    if [[ $env_var =~ ^KARAF_RMI_REGISTRY_ ]]; then
      cfg_name=$(echo "$env_var" | cut -d_ -f4- | tr '[:lower:]' '[:upper:]')
      if [[ "$cfg_name" == "PORT" ]]; then
        updateConfig "rmiRegistryPort" "${!env_var}" "$KARAF_MGMT_CFG"
      elif [[ "$cfg_name" == "HOST" ]]; then
        updateConfig "rmiRegistryHost" "${!env_var}" "$KARAF_MGMT_CFG"
      fi
    fi

    if [[ $env_var =~ ^KARAF_RMI_SERVER_ ]]; then
      cfg_name=$(echo "$env_var" | cut -d_ -f4- | tr '[:lower:]' '[:upper:]')
      if [[ "$cfg_name" == "PORT" ]]; then
        updateConfig "rmiServerPort" "${!env_var}" "$KARAF_MGMT_CFG"
      elif [[ "$cfg_name" == "HOST" ]]; then
        updateConfig "rmiServerHost" "${!env_var}" "$KARAF_MGMT_CFG"
      fi
    fi

    if [[ $env_var =~ ^JETTY_WEB_ ]]; then
      cfg_name=$(echo "$env_var" | cut -d_ -f3- | tr '[:lower:]' '[:upper:]')
      if [[ "$cfg_name" == "PORT" ]]; then
        updateConfig "org.osgi.service.http.port" "${!env_var}" "$JETTY_WEB_CFG"
      elif [[ "$cfg_name" == "HOST" ]]; then
        updateConfig "org.ops4j.pax.web.listening.addresses" "${!env_var}" "$JETTY_WEB_CFG"
      fi
    fi

    if [[ $env_var == "OPENNMS_INSTANCE_ID" ]]; then
      updateConfig "org.opennms.instance.id" "${!env_var}" "$INSTANCE_ID_CFG"
    fi

    if [[ $env_var == "SCV_PROVIDER" ]]; then
      if [[ "${!env_var}" == "dominion" ]]; then
        writeBootFile "$DOMINION_SCV_BOOT" "!scv-jceks-impl" "dominion-secure-credentials-vault"
      else
        rm -f "$DOMINION_SCV_BOOT"
      fi
    fi

    if [[ $env_var == "JAEGER_AGENT_HOST" || $env_var == "JAEGER_ENDPOINT" ]]; then
      jaeger_enabled="true"
    fi
  done

  writeSystemProperties
  writeProcessEnv
  writeServerCerts
  writePromJmxConfig
  # writeTelemetryFeatureXml

  if [ -n "$kafka_ipc_bootstrap" ]; then
    writeBootFile "$KAFKA_IPC_BOOT" "!minion-jms" "!opennms-core-ipc-jms" "opennms-core-ipc-kafka"
  else
    rm -f "$KAFKA_IPC_BOOT"
  fi

  if [ -n "$kafka_rpc_bootstrap" ]; then
    writeBootFile "$KAFKA_RPC_BOOT" "!opennms-core-ipc-rpc-jms" "opennms-core-ipc-rpc-kafka"
  else
    rm -f "$KAFKA_RPC_BOOT"
  fi

  if [ -n "$kafka_sink_bootstrap" ]; then
    writeBootFile "$KAFKA_SINK_BOOT" "!opennms-core-ipc-sink-camel" "opennms-core-ipc-sink-kafka"
  else
    rm -f "$KAFKA_SINK_BOOT"
  fi

  if [ -n "$kafka_twin_bootstrap" ]; then
    writeBootFile "$KAFKA_TWIN_BOOT" "!opennms-core-ipc-twin-jms" "opennms-core-ipc-twin-kafka"
  else
    rm -f "$KAFKA_TWIN_BOOT"
  fi

  if [ -n "$kafka_rpc_bootstrap" ] && [ -n "$kafka_sink_bootstrap" ]; then
    writeBootFile "$DISABLE_JMS_BOOT" "!minion-jms" "!opennms-core-ipc-jms"
  else
    rm -f "$DISABLE_JMS_BOOT"
  fi

  if [ -n "$grpc_host" ]; then
    writeBootFile "$GRPC_BOOT" "!opennms-core-ipc-jms" "!minion-jms" "opennms-core-ipc-grpc-client"
  else
    rm -f "$GRPC_BOOT"
  fi

  if [ -n "$jaeger_enabled" ]; then
    writeBootFile "$JAEGER_BOOT" "opennms-core-tracing-jaeger"
  else
    rm -f "$JAEGER_BOOT"
  fi
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

        # Expose Karaf Shell
        sed -i "/^sshHost/s/=.*/= 0.0.0.0/" ${MINION_HOME}/etc/org.apache.karaf.shell.cfg

        # Expose the RMI registry and server
        sed -i "/^rmiRegistryHost/s/=.*/= 0.0.0.0/" ${MINION_HOME}/etc/org.apache.karaf.management.cfg
        sed -i "/^rmiServerHost/s/=.*/= 0.0.0.0/" ${MINION_HOME}/etc/org.apache.karaf.management.cfg

        # Set Minion location and connection to OpenNMS instance
        echo "location = ${MINION_LOCATION}" > ${MINION_CONFIG}
        echo "id = ${MINION_ID}" >> ${MINION_CONFIG}
        echo "broker-url = ${OPENNMS_BROKER_URL}" >> ${MINION_CONFIG}

        parseEnvironment

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
