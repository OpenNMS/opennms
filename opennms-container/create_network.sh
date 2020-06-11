#!/usr/bin/env bash

set -e

BUILD_NETWORK="$1"; shift || :
[ -n "${BUILD_NETWORK}"  ] || BUILD_NETWORK="opennms-build-network"

if ! docker network inspect --format='{{ .Id }}' "${BUILD_NETWORK}" 2>/dev/null; then
  docker network create --attachable "${BUILD_NETWORK}"
fi
