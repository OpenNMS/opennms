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
ONMS_PACKAGES="opennms-core opennms-webapp-jetty opennms-webapp-remoting opennms-webapp-hawtio"

for PKG in ${ONMS_PACKAGES}; do 
  cp ../../target/rpm/RPMS/noarch/"${PKG}"*.rpm rpms
done

docker build -t horizon \
  --build-arg BUILD_DATE="$(date -u +\"%Y-%m-%dT%H:%M:%S%z\")" \
  --build-arg BASE_IMAGE="opennms/openjdk" \
  --build-arg BASE_IMAGE_VERSION="11.0.3.7-b1" \
  --build-arg VERSION="${VERSION}" \
  --build-arg BUILD_NUMBER="${CIRCLE_BUILD_NUM}" \
  --build-arg BUILD_URL="${CIRCLE_BUILD_URL}" \
  --build-arg BUILD_BRANCH="${CIRCLE_BRANCH}" \
  --build-arg BUILD_SHA1="${CIRCLE_SHA1}" \
  .

docker image save horizon -o images/container.oci
