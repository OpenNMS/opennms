#!/bin/sh

set -e

[ -n "$APT_CONTAINER_NAME" ] || APT_CONTAINER_NAME="apt-repo"

RET=0
echo "=== stopping ${APT_CONTAINER_NAME} (if necessary) ==="
if ! docker rm -f "${APT_CONTAINER_NAME}" 2>/dev/null; then
  RET="$?"
fi

sleep 1

exit "$RET"
