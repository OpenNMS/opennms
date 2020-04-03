#!/usr/bin/env bash

# Use version number from pom except from develop and features branches
case "${CIRCLE_BRANCH}" in
  master)
    VERSION="$(../pom2version.py ../../pom.xml)"
    ;;
  develop)
    VERSION="bleeding"
    ;;
  "release-"*)
    VERSION="release-candidate"
    ;;
  *)
    # Replace / in branch names which is not allowed in OCI tags
    VERSION="${CIRCLE_BRANCH//\//-}"
    ;;
esac

# shellcheck disable=SC2034
# Array of tags for the OCI image used in the specific projects
OCI_TAGS=("${VERSION}")
