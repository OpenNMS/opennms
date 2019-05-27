#!/usr/bin/env bash

# OpenNMS Sentinel version
VERSION="bleeding"

# Tags for the OCI image
OCI_TAGS=("${VERSION}-${BUILD_NUMBER}"
          "${VERSION}")

# Add build specific OCI tag if the build runs in CI/CD
if [ -n "${CIRCLE_BUILD_NUM}" ]; then
  OCI_TAGS+=("${VERSION}-${BUILD_NUMBER}.${CIRCLE_BUILD_NUM}")
fi
