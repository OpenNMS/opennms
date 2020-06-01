#!/usr/bin/env bash

set -e

# Exit script if a statement returns a non-true return value.
set -o errexit

# Use the error status of the first failure, rather than that of the last item in a pipeline.
set -o pipefail

RPMDIR="$1"; shift || :
PORT="$1"; shift || :
OCI="opennms/yum-repo:1.0.0-b4609"

[ -n "${YUM_CONTAINER_NAME}" ] || YUM_CONTAINER_NAME="yum-repo"
[ -n "${BUILD_NETWORK}"      ] || BUILD_NETWORK="opennms-build-network"

YUM_VOLUME="${YUM_CONTAINER_NAME}-volume"

err_report() {
  echo "error on line $1" >&2
  echo "docker logs:" >&2
  echo "" >&2
  docker logs "${YUM_CONTAINER_NAME}" >&2
  exit 1
}

trap 'err_report $LINENO' ERR SIGHUP SIGINT SIGTERM

if [ -z "$RPMDIR" ]; then
  echo "usage: $0 <rpmdir> [port]"
  echo ""
  exit 1
fi
RPMDIR="$(cd "$RPMDIR"; pwd -P)"

if [ -z "$PORT" ]; then
  PORT=19990
fi

MYDIR="$(dirname "$0")"
MYDIR="$(cd "$MYDIR"; pwd -P)"

cd "$MYDIR"

echo "=== stopping old yum server, if necessary ==="
./stop_yum_server.sh >/dev/null 2>&1 || :

echo "=== removing old helper, if necessary ==="
docker rm --force "${YUM_CONTAINER_NAME}-helper" || :

echo "=== creating ${BUILD_NETWORK} network, if necessary ==="
./create_network.sh "${BUILD_NETWORK}"

echo "=== creating ${YUM_VOLUME} volume ==="
docker volume rm --force "${YUM_VOLUME}" || :
docker volume create --name "${YUM_VOLUME}"

echo "=== copying RPMs from ${RPMDIR} to temporary volume ==="
docker create -v "${YUM_VOLUME}:/repo" --name "${YUM_CONTAINER_NAME}-helper" busybox
for FILE in "${RPMDIR}"/*.rpm; do
  echo "* ${FILE}"
  docker cp "${FILE}" "${YUM_CONTAINER_NAME}-helper:/repo/"
done
docker rm --force "${YUM_CONTAINER_NAME}-helper"

echo "=== launching yum server ==="
docker run --rm --detach --name "${YUM_CONTAINER_NAME}" --volume "${YUM_VOLUME}:/repo" --network "${BUILD_NETWORK}" --publish "${PORT}:${PORT}" "${OCI}"

echo "=== waiting for server to be available ==="
COUNT=0
while [ "$COUNT" -lt 30 ]; do
  COUNT="$((COUNT+1))"
  if [ "$( (docker logs "${YUM_CONTAINER_NAME}" 2>&1 || :) | grep -c 'server started' )" -gt 0 ]; then
    echo "READY"
    break
  fi
  sleep 1
done

docker logs "${YUM_CONTAINER_NAME}"

if [ "$COUNT" -eq 30 ]; then
  echo "gave up waiting for server"
  echo "docker logs:"
  echo ""
  docker logs "${YUM_CONTAINER_NAME}"
  exit 1
fi
