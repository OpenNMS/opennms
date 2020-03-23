#!/bin/bash -e
# =====================================================================
# Build script running OpenNMS Minion in Docker environment
#
# Source: https://github.com/indigo423/docker-minion
# Web: https://www.opennms.org
#
# =====================================================================

# Cause false/positives
# shellcheck disable=SC2086

umask 002
MINION_HOME="/opt/minion"
MINION_CONFIG="/opt/minion/etc/org.opennms.minion.controller.cfg"
MINION_OVERLAY_ETC="/opt/minion-etc-overlay"

export KARAF_OPTS="-Djava.locale.providers=CLDR,COMPAT"

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
  if [ -d /keystore ]; then
    mkdir /keystore
  fi

  read -r -p "Enter OpenNMS HTTP username: " OPENNMS_HTTP_USER
  read -r -s -p "Enter OpenNMS HTTP password: " OPENNMS_HTTP_PASS
  echo ""

  read -r -p "Enter OpenNMS Broker username: " OPENNMS_BROKER_USER
  read -r -s -p "Enter OpenNMS Broker password: " OPENNMS_BROKER_PASS
  echo ""

  ${MINION_HOME}/bin/scvcli set opennms.http ${OPENNMS_HTTP_USER} ${OPENNMS_HTTP_PASS}
  ${MINION_HOME}/bin/scvcli set opennms.broker ${OPENNMS_BROKER_USER} ${OPENNMS_BROKER_PASS}

  cp ${MINION_HOME}/etc/scv.jce /keystore
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

function parseEnvironment() {
    # Configure additional features
    IFS=$'\n'
    for VAR in $(env)
    do
        env_var=$(echo "$VAR" | cut -d= -f1)

        if [[ $env_var =~ ^KAFKA_RPC_ ]]; then
            rpc_name=$(echo "$env_var" | cut -d_ -f3- | tr '[:upper:]' '[:lower:]' | tr _ .)
            updateConfig "$rpc_name" "${!env_var}" "${MINION_HOME}/etc/org.opennms.core.ipc.rpc.kafka.cfg"
            if [[ "$rpc_name" == "bootstrap.servers" ]]; then
                echo "!opennms-core-ipc-rpc-jms"   > ${MINION_HOME}/etc/featuresBoot.d/kafka-rpc.boot
                echo "opennms-core-ipc-rpc-kafka" >> ${MINION_HOME}/etc/featuresBoot.d/kafka-rpc.boot
            fi
        fi

        if [[ $env_var =~ ^KAFKA_SINK_ ]]; then
            sink_key=$(echo "$env_var" | cut -d_ -f3- | tr '[:upper:]' '[:lower:]' | tr _ .)
            updateConfig "$sink_key" "${!env_var}" "${MINION_HOME}/etc/org.opennms.core.ipc.sink.kafka.cfg"
            if [[ "$sink_key" == "bootstrap.servers" ]]; then
                echo "!opennms-core-ipc-sink-camel" > ${MINION_HOME}/etc/featuresBoot.d/kafka-sink.boot
                echo "opennms-core-ipc-sink-kafka" >> ${MINION_HOME}/etc/featuresBoot.d/kafka-sink.boot
            fi
        fi
    done
}

initConfig() {
    if [ ! -d ${MINION_HOME} ]; then
        echo "OpenNMS Minion home directory doesn't exist in ${MINION_HOME}."
        exit ${E_ILLEGAL_ARGS}
    fi

    if [ ! -f ${MINION_HOME}/etc/configured ]; then

        # Expose Karaf Shell
        sed -i "/^sshHost/s/=.*/= 0.0.0.0/" ${MINION_HOME}/etc/org.apache.karaf.shell.cfg

        # Expose the RMI registry and server
        sed -i "/^rmiRegistryHost/s/=.*/= 0.0.0.0/" ${MINION_HOME}/etc/org.apache.karaf.management.cfg
        sed -i "/^rmiServerHost/s/=.*/= 0.0.0.0/" ${MINION_HOME}/etc/org.apache.karaf.management.cfg

        # Set Minion location and connection to OpenNMS instance
        echo "location = ${MINION_LOCATION}" > ${MINION_CONFIG}
        echo "id = ${MINION_ID}" >> ${MINION_CONFIG}
        echo "broker-url = ${OPENNMS_BROKER_URL}" >> ${MINION_CONFIG}
        echo "http-url = ${OPENNMS_HTTP_URL}" >> ${MINION_CONFIG}

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
    cp -Lr ${MINION_OVERLAY_ETC}/* ${MINION_HOME}/etc || exit ${E_INIT_CONFIG}
  else
    echo "No custom config found in ${MINION_OVERLAY_ETC}. Use default configuration."
  fi
}

start() {
    export KARAF_EXEC="exec"
    cd ${MINION_HOME}/bin
    exec ./karaf server
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
            initConfig
            applyOverlayConfig
            start
            ;;
        s)
            setCredentials
            ;;
        f)
            initConfig
            applyOverlayConfig
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
