#!/bin/sh
# Download the OpenNMS Prometheus RemoteWrite plugin KAR from Maven Central into
# the volume that the OpenNMS container mounts as $OPENNMS_HOME/deploy.
#
# The file name matters: etc/featuresBoot.d/prometheus-remotewrite.boot refers to
# it with "wait-for-kar=opennms-prometheus-remotewrite-plugin", and Karaf derives
# the KAR name from the file name.
set -eu

VERSION="${PLUGIN_VERSION:-2.1.0}"
DEST_DIR="${DEST_DIR:-/deploy}"
DEST="${DEST_DIR}/opennms-prometheus-remotewrite-plugin.kar"

GROUP_PATH="org/opennms/plugins/timeseries"
ARTIFACT="org.opennms.plugins.timeseries.prometheus.remotewrite.assembly.kar"
URL="https://repo1.maven.org/maven2/${GROUP_PATH}/${ARTIFACT}/${VERSION}/${ARTIFACT}-${VERSION}.kar"

if [ -s "${DEST}" ]; then
  echo "Plugin KAR already present: ${DEST} (delete it or 'docker compose down -v' to re-download)"
  exit 0
fi

echo "Downloading Prometheus RemoteWrite plugin ${VERSION}"
echo "  from ${URL}"
mkdir -p "${DEST_DIR}"
# Download to a temporary name so a failed transfer never looks like a valid KAR.
curl -fSL --retry 3 --retry-delay 2 -o "${DEST}.part" "${URL}"
mv "${DEST}.part" "${DEST}"
chmod 0644 "${DEST}"
echo "Wrote ${DEST}"
