#!/usr/bin/env bash

# Exit script if a statement returns a non-true return value.
set -o errexit

# Use the error status of the first failure, rather than that of the last item in a pipeline.
set -o pipefail

# shellcheck source=registry-config.sh
source ../registry-config.sh

# shellcheck source=opennms-container/version-n-tags.sh
source ../version-tags.sh

# OpenNMS Horizon packages
ONMS_PACKAGES="meridian-core meridian-webapp-jetty"

for PKG in ${ONMS_PACKAGES}; do
  cp ../../target/rpm/RPMS/noarch/"${PKG}"*.rpm rpms
done

docker build -t horizon \
  --build-arg BUILD_DATE="$(date -u +\"%Y-%m-%dT%H:%M:%S%z\")" \
  --build-arg VERSION="${VERSION}" \
  --build-arg SOURCE="${CIRCLE_REPOSITORY_URL}" \
  --build-arg REVISION="$(git describe --always)" \
  --build-arg BUILD_JOB_ID="${CIRCLE_WORKFLOW_JOB_ID}" \
  --build-arg BUILD_NUMBER="${CIRCLE_BUILD_NUM}" \
  --build-arg BUILD_URL="${CIRCLE_BUILD_URL}" \
  --build-arg BUILD_BRANCH="${CIRCLE_BRANCH}" \
  .

if [ -n "${CIRCLE_BUILD_NUM}" ]; then
  IMAGE_VERSION+=("${BASE_IMAGE_VERSION}-b${CIRCLE_BUILD_NUM}")
fi

docker image save horizon -o images/container.oci
