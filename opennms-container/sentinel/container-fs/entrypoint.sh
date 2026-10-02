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
# Image-owned config lives outside the /opt/sentinel/etc volume so that it is
# still present when an existing etc volume is reused across image upgrades.
CONTAINER_CONFIG_ETC="${SENTINEL_HOME}/container-fs/etc"
FEATURES_BOOT_DIR="${SENTINEL_HOME}/etc/featuresBoot.d"
FEATURES_BOOT_TEMPLATES_DIR="${CONTAINER_CONFIG_ETC}/featuresBoot.d/templates"
# No longer supported; configuration is taken from environment variables instead
LEGACY_CONFD_KEY_STORE="${SENTINEL_HOME}/sentinel-config.yaml"

# Flow adapter configuration
#
# Set SENTINEL_FLOWS_ENABLED=true to install the sentinel-flows feature stack.
# Define individual adapters by overlaying one org.opennms.features.telemetry.adapters-<name>.cfg
# file per adapter into ${SENTINEL_HOME}/etc/ (see sentinel-etc-overlay), the same convention
# already used for Minion listener config. See sentinel-features.adoc for the file format.
SENTINEL_FLOWS_ENABLED="${SENTINEL_FLOWS_ENABLED:-false}"

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
E_INIT_CONFIG=127

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
    local key=$1
    local value=$2
    local file=$3
    # Optional: name of the environment variable the value came from. A line that
    # already resolves that variable through an ${env:<name>...} placeholder is
    # left alone, so the value keeps tracking the environment across restarts.
    local env_name=${4:-}

    if [[ "$value" == *'${'* ]]; then
        echo "[Configuring][WARN] value for '$key' contains '\${', which Karaf expands as a placeholder when it reads '$file'"
    fi

    # If config exists in file, replace it (also uncommenting it). Otherwise, append to file.
    # Key and value are passed through the environment and compared literally,
    # so values may contain any character (e.g. '@', '&' or '\').
    touch "$file"
    local result rc
    result=$(KEY="$key" VALUE="$value" ENV_NAME="$env_name" awk '
        BEGIN {
            key = ENVIRON["KEY"]
            placeholder = (ENVIRON["ENV_NAME"] != "") ? "${env:" ENVIRON["ENV_NAME"] : ""

            # Encode the value so Karaf reads it back unchanged. Karaf unescapes
            # backslashes twice (once when parsing the properties file, once more
            # during ${...} substitution), so a literal "\" must be written as "\\\\".
            # Control characters use properties escapes, and a leading space is
            # escaped so it is not stripped.
            raw = ENVIRON["VALUE"]; value = ""
            for (i = 1; i <= length(raw); i++) {
                c = substr(raw, i, 1)
                if (c == "\\")      value = value "\\\\\\\\"
                else if (c == "\n") value = value "\\n"
                else if (c == "\r") value = value "\\r"
                else if (c == "\t") value = value "\\t"
                else if (c == " " && i == 1) value = value "\\ "
                else                value = value c
            }
        }
        {
            line = $0
            sub(/^#?[ \t]*/, "", line)
            rest = substr(line, length(key) + 1)
            if (substr(line, 1, length(key)) == key && rest ~ /^[ \t]*=/) {
                found = 1
                current = rest
                sub(/^[ \t]*=[ \t]*/, "", current)
                if (placeholder != "" && index(current, placeholder) == 1) {
                    kept = 1
                    print
                } else {
                    print key "=" value
                }
                next
            }
            print
        }
        END {
            if (!found) print key "=" value
            exit kept ? 3 : 0
        }
    ' "$file") && rc=0 || rc=$?

    # Omit $value here, in case there is sensitive information
    case "$rc" in
      0) echo "[Configuring] '$key' in '$file'" ;;
      3) echo "[Configuring] '$key' in '$file' is resolved from \${env:${env_name}}, left unchanged"; return 0 ;;
      *) return "$rc" ;;
    esac
    # Rewrite in place to keep the file's ownership and permissions
    printf '%s\n' "$result" > "$file"
    CONFIG_WRITTEN=true
}

function resetConfig() {
    # Undo a value written from an environment variable that is no longer set: restore
    # the image default for the key if the image ships one, otherwise remove the key.
    local key=$1
    local file=$2
    local seed
    seed="${CONTAINER_CONFIG_ETC}/$(basename "$file")"

    [ -f "$file" ] || return 0
    local result
    result=$(KEY="$key" SEED="$seed" awk '
        function matches(l,   rest) {
            sub(/^#?[ \t]*/, "", l)
            rest = substr(l, length(key) + 1)
            return substr(l, 1, length(key)) == key && rest ~ /^[ \t]*=/
        }
        BEGIN {
            key = ENVIRON["KEY"]
            # The image default for the key, if the image ships this file
            while ((getline l < ENVIRON["SEED"]) > 0) {
                if (matches(l)) { default_line = l; have_default = 1; break }
            }
        }
        matches($0) { if (have_default && !restored) { print default_line; restored = 1 } next }
        { print }
    ' "$file")

    echo "[Configuring] '$key' in '$file' reset, its environment variable is no longer set"
    if [ -n "$result" ]; then
      printf '%s\n' "$result" > "$file"
    else
      : > "$file"
    fi
}

# Records which keys were written from which environment variable, so a key can be
# reset once its variable is removed. Stored next to the files it describes.
ENV_MANAGED_KEYS="${SENTINEL_HOME}/etc/.container-env-keys"
APPLIED_ENV_KEYS=()

function applyEnvConfig() {
    local env_var=$1
    local key=$2
    local file=$3

    CONFIG_WRITTEN=false
    updateConfig "$key" "${!env_var}" "$file" "$env_var"
    if [[ "$CONFIG_WRITTEN" == "true" ]]; then
        APPLIED_ENV_KEYS+=("${env_var}"$'\t'"${key}"$'\t'"${file}")
    fi
}

function parseEnvironment() {
    local env_var ipc_name es_key
    APPLIED_ENV_KEYS=()

    # compgen -e lists variable names only, so multi-line values are handled correctly
    while IFS= read -r env_var; do
        if [[ $env_var =~ ^KAFKA_IPC_ ]]; then
            ipc_name=$(echo "${env_var#KAFKA_IPC_}" | tr '[:upper:]' '[:lower:]' | tr _ .)
            applyEnvConfig "$env_var" "$ipc_name" "${SENTINEL_HOME}/etc/org.opennms.core.ipc.sink.kafka.cfg"
            applyEnvConfig "$env_var" "$ipc_name" "${SENTINEL_HOME}/etc/org.opennms.core.ipc.sink.kafka.consumer.cfg"
        fi

        # Only known names are mapped. A catch-all would also pick up the variables
        # Kubernetes injects for a Service named "elasticsearch" (ELASTICSEARCH_PORT=tcp://...).
        if [[ $env_var =~ ^ELASTICSEARCH_ ]]; then
            case "${env_var#ELASTICSEARCH_}" in
              URL)            es_key="elasticUrl" ;;
              INDEX_STRATEGY) es_key="elasticIndexStrategy" ;;
              REPLICAS)       es_key="settings.index.number_of_replicas" ;;
              CONN_TIMEOUT)   es_key="connTimeout" ;;
              READ_TIMEOUT)   es_key="readTimeout" ;;
              USER)           es_key="globalElasticUser" ;;
              PASSWORD)       es_key="globalElasticPassword" ;;
              *)
                echo "[Configuring] Ignoring unrecognized variable ${env_var}"
                continue
                ;;
            esac
            applyEnvConfig "$env_var" "$es_key" "${SENTINEL_HOME}/etc/org.opennms.features.flows.persistence.elastic.cfg"
        fi

        case "$env_var" in
          OPENNMS_INSTANCE_ID)
            applyEnvConfig "$env_var" "org.opennms.instance.id" "${SENTINEL_HOME}/etc/custom.system.properties"
            ;;
          POSTGRES_JDBC_URL)
            applyEnvConfig "$env_var" "datasource.url" "${SENTINEL_HOME}/etc/org.opennms.netmgt.distributed.datasource.cfg"
            ;;
        esac
    done < <(compgen -e)

    resetRemovedEnvConfig
}

function resetRemovedEnvConfig() {
    # Reset keys written on an earlier start from variables that are no longer set
    local entry env_var key file
    # Keyed by key and file only, so a key written again on this start is never reset
    local -A applied=()
    for entry in "${APPLIED_ENV_KEYS[@]}"; do
      applied["${entry#*$'\t'}"]=1
    done

    if [ -f "${ENV_MANAGED_KEYS}" ]; then
      while IFS=$'\t' read -r env_var key file; do
        [ -n "$key" ] || continue
        if [ -z "${applied["${key}"$'\t'"${file}"]:-}" ]; then
          resetConfig "$key" "$file"
        fi
      done < "${ENV_MANAGED_KEYS}"
    fi

    if [ "${#APPLIED_ENV_KEYS[@]}" -gt 0 ]; then
      printf '%s\n' "${APPLIED_ENV_KEYS[@]}" > "${ENV_MANAGED_KEYS}"
    else
      rm -f "${ENV_MANAGED_KEYS}"
    fi
}

function handleLegacyConfd() {
    # confd and sentinel-config.yaml are gone; refuse to start rather than silently
    # ignoring a configuration the user still expects to be applied.
    if [ -f "${LEGACY_CONFD_KEY_STORE}" ]; then
        echo "[Startup][ERROR] Found ${LEGACY_CONFD_KEY_STORE}, which is no longer supported."
        echo "[Startup][ERROR] Configure Sentinel through environment variables or etc overlays instead and remove the file."
        exit ${E_INIT_CONFIG}
    fi

    # Files rendered by confd in earlier images persist in the etc/deploy volumes.
    # A stale ipc-strategy.boot would combine with the new feature boot files and can
    # exclude every IPC transport, so remove them.
    local legacy_file
    for legacy_file in \
      "${FEATURES_BOOT_DIR}/ipc-strategy.boot" \
      "${SENTINEL_HOME}/deploy/confd-flows-feature.xml"; do
      if [ -f "${legacy_file}" ]; then
        echo "[Startup] Removing legacy confd-generated file ${legacy_file}"
        rm -f "${legacy_file}"
      fi
    done
}

function validateEnvironment() {
    # Runs before anything is written, so an invalid value leaves the configuration untouched
    case "${SENTINEL_IPC,,}" in
      kafka|jms)
        IPC_STRATEGY="${SENTINEL_IPC,,}"
        ;;
      "")
        # Same default as the confd-based images: Kafka once a bootstrap server is configured
        if [ -n "${KAFKA_IPC_BOOTSTRAP_SERVERS:-}" ]; then
          IPC_STRATEGY="kafka"
        else
          IPC_STRATEGY="jms"
        fi
        ;;
      *)
        echo "[Startup][ERROR] Invalid SENTINEL_IPC '${SENTINEL_IPC}', expected 'jms' or 'kafka'."
        exit ${E_ILLEGAL_ARGS}
        ;;
    esac

    case "${SENTINEL_FLOWS_ENABLED,,}" in
      true|false) ;;
      *)
        echo "[Startup][ERROR] Invalid SENTINEL_FLOWS_ENABLED '${SENTINEL_FLOWS_ENABLED}', expected 'true' or 'false'."
        exit ${E_ILLEGAL_ARGS}
        ;;
    esac
}

function seedContainerConfig() {
    # Install image-provided config files that are missing from etc. Existing files are
    # never overwritten, so values persisted by earlier starts (e.g. the Sentinel id)
    # and user edits survive container recreation and image upgrades.
    local src
    for src in "${CONTAINER_CONFIG_ETC}"/*.cfg; do
      [ -f "${src}" ] || continue
      if [ ! -f "${SENTINEL_HOME}/etc/$(basename "${src}")" ]; then
        echo "[Startup] Installing default $(basename "${src}")"
        cp "${src}" "${SENTINEL_HOME}/etc/"
      fi
    done
}

function applyFeatureBootTemplates() {
    local managed_boot_files=(
      "sentinel-ipc.boot"
      "sentinel-flows.boot"
    )
    local boot_file
    for boot_file in "${managed_boot_files[@]}"; do
      rm -f "${FEATURES_BOOT_DIR}/${boot_file}"
    done

    apply_template() {
        local template="$1"
        local name="${2:-$1}"
        cp "${FEATURES_BOOT_TEMPLATES_DIR}/${template}" "${FEATURES_BOOT_DIR}/${name}"
        echo "[Features] Enabled: ${template}"
    }

    echo "[Features] IPC strategy set to ${IPC_STRATEGY}."
    apply_template "sentinel-ipc-${IPC_STRATEGY}.boot" "sentinel-ipc.boot"

    if [[ "${SENTINEL_FLOWS_ENABLED,,}" == "true" ]]; then
        echo "[Features] Flow processing enabled."
        apply_template "sentinel-flows.boot"
    fi
}

initConfig() {
    if [ ! -d ${SENTINEL_HOME} ]; then
        echo "OpenNMS Sentinel home directory doesn't exist in ${SENTINEL_HOME}."
        exit ${E_ILLEGAL_ARGS}
    fi

    validateEnvironment
    handleLegacyConfd
    seedContainerConfig

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

        # Mark as configured
        echo "Configured $(date)" > ${SENTINEL_HOME}/etc/configured
    else
        echo "OpenNMS Sentinel is already configured, skipped."
    fi

    # Location, broker-url, and the datasource are resolved from the environment
    # via ${env:...} placeholders in the image-provided
    # org.opennms.sentinel.controller.cfg / org.opennms.netmgt.distributed.datasource.cfg.
    # Volumes created by earlier images keep the literal values written back then.
    #
    # The id must stay stable across restarts, even if SENTINEL_ID is changed or
    # removed later, so it's persisted once, whenever the file has none yet:
    # SENTINEL_ID if set, otherwise a random one.
    local controller_cfg="${SENTINEL_HOME}/etc/org.opennms.sentinel.controller.cfg"
    if ! grep -E -q "^[[:space:]]*id[[:space:]]*=" "${controller_cfg}"; then
        updateConfig "id" "${SENTINEL_ID:-$(uuidgen)}" "${controller_cfg}"
    fi

    # Re-applied on every start (not gated by the configured marker) so that
    # environment-driven config takes effect on container restarts, matching
    # the old confd behavior where templates were re-rendered on every start.
    parseEnvironment
    applyFeatureBootTemplates
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
