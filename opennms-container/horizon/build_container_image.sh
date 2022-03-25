#!/usr/bin/env bash

# Exit immediately if anything returns non-zero
set -e

# Exit script if a statement returns a non-true return value.
set -o errexit

# Use the error status of the first failure, rather than that of the last item in a pipeline.
set -o pipefail

cd "$(dirname "$0")"

# shellcheck disable=SC1091
source "../set-build-environment.sh"

#../launch_yum_server.sh "$RPMDIR"

cat <<END >opennms.list
deb https://debian.opennms.org stable main
deb-src https://debian.opennms.org stable main
END

if [ -d "${DEBDIR}" ]; then
  echo "Copy debs ${DEBDIR} to debs/"
  cp -v ${DEBDIR}/*.deb debs/
fi

  #--network "${BUILD_NETWORK}" \
docker buildx build -t horizon \
  --platform="${DOCKER_ARCH}" \
  --build-arg BUILD_DATE="${BUILD_DATE}" \
  --build-arg VERSION="${VERSION}" \
  --build-arg SOURCE="${SOURCE}" \
  --build-arg REVISION="${REVISION}" \
  --build-arg BUILD_JOB_ID="${BUILD_JOB_ID}" \
  --build-arg BUILD_NUMBER="${BUILD_NUMBER}" \
  --build-arg BUILD_URL="${BUILD_URL}" \
  --build-arg BUILD_BRANCH="${BUILD_BRANCH}" \
  --build-arg CONFD_ARCH="${CONFD_ARCH}" \
  .

if [ -n "${CIRCLE_BUILD_NUM}" ]; then
  IMAGE_VERSION+=("${BASE_IMAGE_VERSION}-b${CIRCLE_BUILD_NUM}")
fi

docker image save horizon -o images/container.oci

#rm -f rpms/*.repo
#../stop_yum_server.sh
