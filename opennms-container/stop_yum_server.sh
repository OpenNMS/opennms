#!/bin/sh

set -e

[ -n "$YUM_CONTAINER_NAME" ] || YUM_CONTAINER_NAME="yum-repo"
YUM_VOLUME="${YUM_CONTAINER_NAME}-volume"

RET=0
echo "=== stopping ${YUM_CONTAINER_NAME} (if necessary) ==="
if ! docker rm -f "${YUM_CONTAINER_NAME}" 2>/dev/null; then
  RET="$?"
fi

sleep 1

echo "=== removing temporary yum volume ==="
docker volume rm --force "${YUM_VOLUME}" 2>/dev/null || :

exit "$RET"
