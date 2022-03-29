#!/usr/bin/env bash

# Exit immediately if anything returns non-zero
set -e

# Exit script if a statement returns a non-true return value.
set -o errexit

# Use the error status of the first failure, rather than that of the last item in a pipeline.
set -o pipefail

DEBDIR="$1"; shift || :
PORT="$1"; shift || :
APT_VOLUME="$1"; shift || :
OCI="ubuntu:focal"
MOUNT_PATH="/repo"

[ -n "${APT_CONTAINER_NAME}" ] || APT_CONTAINER_NAME="apt-repo"
[ -n "${BUILD_NETWORK}"      ] || BUILD_NETWORK="opennms-build-network"

err_report() {
  echo "error on line $1" >&2
  echo "docker logs:" >&2
  echo "" >&2
  docker logs "${APT_CONTAINER_NAME}" >&2
  exit 1
}

trap 'err_report $LINENO' ERR SIGHUP SIGINT SIGTERM

if [ -z "$DEBDIR" ]; then
  echo "usage: $0 <rpmdir> [port]"
  echo ""
  exit 1
fi
DEBDIR="$(cd "$DEBDIR"; pwd -P)"

if [ -z "$PORT" ]; then
  PORT=19990
fi

MYDIR="$(dirname "$0")"
MYDIR="$(cd "$MYDIR"; pwd -P)"

cd "$MYDIR"

echo "=== stopping old apt server, if necessary ==="
./stop_apt_server.sh $APT_CONTAINER_NAME $APT_VOLUME >/dev/null 2>&1 || :

echo "=== creating ${BUILD_NETWORK} network, if necessary ==="
./create_network.sh "${BUILD_NETWORK}"

echo "=== creating ${APT_VOLUME} volume ==="
docker volume rm --force "${APT_VOLUME}" || :
docker volume create --name "${APT_VOLUME}"

echo "=== copying DEBs from ${DEBDIR} to temporary volume ==="
docker create -v "${APT_VOLUME}:${MOUNT_PATH}" --name "${APT_CONTAINER_NAME}-helper" busybox
mkdir -p dists/stable/main/binary-all
docker cp dists "${APT_CONTAINER_NAME}-helper:${MOUNT_PATH}"
rmdir -p dists/stable/main/binary-all
for FILE in "${DEBDIR}"/*.deb; do
  echo "* ${FILE}"
  docker cp "${FILE}" "${APT_CONTAINER_NAME}-helper:${MOUNT_PATH}/dists/stable/main/binary-all"
done
for FILE in apt/*; do
  echo "* ${FILE}"
  docker cp "${FILE}" "${APT_CONTAINER_NAME}-helper:${MOUNT_PATH}"
done
docker rm -f "${APT_CONTAINER_NAME}-helper"

echo "=== launching apt server ==="
echo "FIND $MOUNT_PATH"
docker run --rm --volume "$APT_VOLUME:$MOUNT_PATH" --network "${BUILD_NETWORK}" --publish "${PORT}:${PORT}" "${OCI}" find $MOUNT_PATH
echo "END"
docker run --rm --detach --name "${APT_CONTAINER_NAME}" --volume ${APT_VOLUME}:${MOUNT_PATH} --network "${BUILD_NETWORK}" --publish "${PORT}:${PORT}" --entrypoint ${MOUNT_PATH}/entrypoint.sh "${OCI}" $MOUNT_PATH $PORT

echo "=== waiting for server to be available ==="
COUNT=0
while [ "$COUNT" -lt 120 ]; do
  COUNT="$((COUNT+1))"
  if [ "$( (docker logs "${APT_CONTAINER_NAME}" 2>&1 || :) | grep -c 'Start apt server' )" -gt 0 ]; then
    echo "READY"
    break
  fi
  sleep 1
done

docker logs "${APT_CONTAINER_NAME}"

if [ "$COUNT" -eq 30 ]; then
  echo "gave up waiting for server"
  echo "docker logs:"
  echo ""
  docker logs "${APT_CONTAINER_NAME}"
  exit 1
fi
