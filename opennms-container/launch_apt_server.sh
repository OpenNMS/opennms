#!/usr/bin/env bash

# Exit immediately if anything returns non-zero
set -e

# Exit script if a statement returns a non-true return value.
set -o errexit

# Use the error status of the first failure, rather than that of the last item in a pipeline.
set -o pipefail

DEBDIR="$1"; shift || :
PORT="$1"; shift || :
TMP_PATH="$1"; shift || :
OCI="ubuntu:focal"
MOUNT_POINT="/tmp/deb"

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

echo "=== stopping old yum server, if necessary ==="
./stop_apt_server.sh $TMP_PATH >/dev/null 2>&1 || :

echo "=== creating ${BUILD_NETWORK} network, if necessary ==="
./create_network.sh "${BUILD_NETWORK}"

echo "=== copying DEBs from ${DEBDIR} to temporary volume ==="
cp -v apt/* $TMP_PATH/
mkdir -p "${TMP_PATH}/dists/stable/main/binary-all"
cp -v "${DEBDIR}"/*.deb "${TMP_PATH}/dists/stable/main/binary-all"

echo "=== launching apt server ==="
#docker run --rm -it -v /Users/freddy/git/opennms/opennms-container/horizon/deb.bak:/tmp/deb --publish "8080:8080"  --publish "8081:8081" ubuntu:focal /tmp/deb/setup.sh
echo docker run --rm --detach --name "${APT_CONTAINER_NAME}" --volume "$TMP_PATH:$MOUNT_POINT" --network "${BUILD_NETWORK}" --publish "${PORT}:${PORT}" "${OCI}" ${MOUNT_POINT}/entrypoint.sh $MOUNT_POINT $PORT
echo "FIND $TMP_PATH"
find $TMP_PATH
echo "FIND $MOUNT_POINT"
docker run --rm --volume "$TMP_PATH:$MOUNT_POINT" --network "${BUILD_NETWORK}" --publish "${PORT}:${PORT}" "${OCI}" find $MOUNT_POINT
echo "END"
docker run --rm --detach --name "${APT_CONTAINER_NAME}" --volume ${TMP_PATH}:${MOUNT_POINT} --network "${BUILD_NETWORK}" --publish "${PORT}:${PORT}" --entrypoint ${MOUNT_POINT}/entrypoint.sh "${OCI}" $MOUNT_POINT $PORT

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
