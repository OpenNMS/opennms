#!/usr/bin/env bash
# =====================================================================
# Entrypoint for generic OpenNMS Daemon container (Karaf-only).
# Configures datasource, applies overlays, then starts Karaf.
# =====================================================================

set -e

umask 002
export DAEMON_HOME="/opt/daemon"
export KARAF_HOME="${DAEMON_HOME}"

DAEMON_OVERLAY_ETC="/opt/daemon-etc-overlay"
DAEMON_OVERLAY="/opt/daemon-overlay"

export JAVA_OPTS="$JAVA_OPTS -Djava.locale.providers=CLDR,COMPAT"
export JAVA_OPTS="$JAVA_OPTS -Dopennms.home=${DAEMON_HOME}"
export JAVA_OPTS="$JAVA_OPTS -Djdk.util.zip.disableZip64ExtraFieldValidation=true"

initConfig() {
    if [ ! -f "${DAEMON_HOME}/etc/configured" ]; then
        # Create SSH Key-Pair for Karaf Shell
        mkdir -p "${DAEMON_HOME}/.ssh"
        chmod 700 "${DAEMON_HOME}/.ssh"
        ssh-keygen -t rsa -f "${DAEMON_HOME}/.ssh/id_rsa" -q -N ""
        echo "daemon=$(awk '{print $2}' "${DAEMON_HOME}/.ssh/id_rsa.pub"),viewer" > "${DAEMON_HOME}/etc/keys.properties"
        echo "_g_\\:admingroup = group,admin,manager,viewer,systembundles,ssh" >> "${DAEMON_HOME}/etc/keys.properties"
        chmod 600 "${DAEMON_HOME}/.ssh/id_rsa"

        # Expose Karaf Shell on all interfaces
        sed -i "/^sshHost/s/=.*/= 0.0.0.0/" "${DAEMON_HOME}/etc/org.apache.karaf.shell.cfg"

        # Configure distributed datasource from environment variables
        DB_CONFIG="${DAEMON_HOME}/etc/org.opennms.netmgt.distributed.datasource.cfg"
        echo "datasource.url = jdbc:postgresql://${POSTGRES_HOST}:${POSTGRES_PORT}/${POSTGRES_DB}" > "${DB_CONFIG}"
        echo "datasource.username = ${POSTGRES_USER}" >> "${DB_CONFIG}"
        echo "datasource.password = ${POSTGRES_PASSWORD}" >> "${DB_CONFIG}"
        echo "datasource.databaseName = ${POSTGRES_DB}" >> "${DB_CONFIG}"

        # Mark as configured
        echo "Configured $(date)" > "${DAEMON_HOME}/etc/configured"
    else
        echo "OpenNMS Daemon is already configured, skipped."
    fi
}

applyOverlayConfig() {
    # Overlay etc-specific config
    if [ -d "${DAEMON_OVERLAY_ETC}" ] && [ -n "$(ls -A "${DAEMON_OVERLAY_ETC}" 2>/dev/null)" ]; then
        echo "Apply custom etc configuration from ${DAEMON_OVERLAY_ETC}."
        cp -a "${DAEMON_OVERLAY_ETC}"/* "${DAEMON_HOME}/etc/."
    fi
    # Overlay for entire daemon dir
    if [ -d "${DAEMON_OVERLAY}" ] && [ -n "$(ls -A "${DAEMON_OVERLAY}" 2>/dev/null)" ]; then
        echo "Apply custom configuration from ${DAEMON_OVERLAY}."
        cp -a "${DAEMON_OVERLAY}"/* "${DAEMON_HOME}/."
    fi
}

start() {
    export KARAF_EXEC="exec"
    cd "${DAEMON_HOME}/bin"
    exec ./karaf server
}

initConfig
applyOverlayConfig
start
