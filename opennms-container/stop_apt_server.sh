#!/usr/bin/env bash

set -e

APT_CONTAINER_NAME=$1
APT_VOLUME=$2

[ -n "$APT_CONTAINER_NAME" ] || APT_CONTAINER_NAME="apt-repo"
[ -n "$APT_VOLUME" ] || APT_VOLUME="${APT_CONTAINER_NAME}-volume"

RET=0
echo "=== stopping ${APT_CONTAINER_NAME} (if necessary) ==="
if ! docker rm -f "${APT_CONTAINER_NAME}" 2>/dev/null; then
  RET="$?"
fi

sleep 1

echo "=== removing temporary volume ${APT_VOLUME} ==="
docker volume rm --force "${APT_VOLUME}" 2>/dev/null || :

exit "$RET"
