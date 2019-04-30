#!/usr/bin/env bash

# shellcheck disable=SC2034

# Base Image Dependency
BASE_IMAGE="opennms/openjdk"
BASE_IMAGE_VERSION="1.8.0.191.b12-b3"
BUILD_DATE="$(date -u +"%Y-%m-%dT%H:%M:%S%z")"

# Sentinel Image versioning
# Floating tag name
VERSION="bleeding"

# Allow a manual build number which allows to overwrite an existing image
BUILD_NUMBER="b1"

# Floating tags
IMAGE_VERSION=("${VERSION}-${BUILD_NUMBER}"
               "${VERSION}")

# Most specific tag when it is not build locally and in CircleCI
if [ -n "${CIRCLE_BUILD_NUM}" ]; then
  IMAGE_VERSION+=("${VERSION}-${BUILD_NUMBER}.${CIRCLE_BUILD_NUM}")
fi

REPO_HOST="yum.opennms.org"
REPO_RELEASE="stable"
REPO_RPM="https://${REPO_HOST}/repofiles/opennms-repo-${REPO_RELEASE}-rhel7.noarch.rpm"
REPO_KEY_URL="https://${REPO_HOST}/OPENNMS-GPG-KEY"

# System Package dependencies
PACKAGES="wget
          gettext"

#
# If you want to install packages from the official repository, add your packages here.
# By default the build system will build the RPMS in the ./rpms directory and install from here.
#
# Suggested packages to install OpenNMS Minion packages from repository
SENTINEL_PACKAGES="opennms-sentinel"

# Run as user
USER="sentinel"
GROUP="sentinel"
