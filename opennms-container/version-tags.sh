#!/usr/bin/env bash

# Use version number from pom except from develop
if [[ "${CIRCLE_BRANCH}" == "develop" ]]; then
  VERSION="bleeding"
else
  VERSION="$(../pom2version.py ../../pom.xml)"
fi

# List of tags for the OCI image
OCI_TAGS=("${VERSION}")

# Add build specific OCI tag if the build runs in CI/CD
if [[ -n "${CIRCLE_BUILD_NUM}" ]]; then
  OCI_TAGS+=("${VERSION}-cb.${CIRCLE_BUILD_NUM}")
fi
