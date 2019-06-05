#!/usr/bin/env bash

# Exit script if a statement returns a non-true return value.
set -o errexit

# Use the error status of the first failure, rather than that of the last item in a pipeline.
set -o pipefail

# shellcheck source=registry-config.sh
source ../registry-config.sh

# shellcheck source=opennms-container/version-n-tags.sh
source ../version-tags.sh

# OpenNMS Minion packages
SENTINEL_PACKAGES="opennms-sentinel"

for PKG in ${SENTINEL_PACKAGES}; do 
  cp ../../target/rpm/RPMS/noarch/"${PKG}"*.rpm rpms
done

docker build -t sentinel \
  --build-arg BUILD_DATE="$(date -u +\"%Y-%m-%dT%H:%M:%S%z\")" \
  --build-arg BASE_IMAGE="opennms/openjdk" \
  --build-arg BASE_IMAGE_VERSION="11.0.3.7-b1" \
  --build-arg VERSION="${VERSION}" \
  --build-arg BUILD_NUMBER="${CIRCLE_BUILD_NUM}" \
  --build-arg BUILD_URL="${CIRCLE_BUILD_URL}" \
  --build-arg BUILD_BRANCH="${CIRCLE_BRANCH}" \
  --build-arg BUILD_SHA1="${CIRCLE_SHA1}" \
  .

docker image save sentinel -o images/container.oci
