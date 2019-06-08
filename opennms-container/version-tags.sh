#!/usr/bin/env bash

# shellcheck disable=SC2034

# Use version number from pom except from develop
if [[ "${CIRCLE_BRANCH}" == "develop" ]]; then
  VERSION="bleeding"
else
  VERSION="$(../pom2version.py ../../pom.xml)"
fi

# Array of tags for the OCI image used in the specific projects
OCI_TAGS=("${VERSION}")
