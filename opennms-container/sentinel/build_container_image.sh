#!/usr/bin/env bash

# Exit immediately if anything returns non-zero
set -e

# Exit script if a statement returns a non-true return value.
set -o errexit

# Use the error status of the first failure, rather than that of the last item in a pipeline.
set -o pipefail

MYDIR="$(cd "$(dirname "$0")"; pwd)"
cd "${MYDIR}"

# shellcheck disable=SC1091
source ../set-build-environment.sh

TARBALL="$(find ../../opennms-assemblies/sentinel -name \*-sentinel.tar.gz -type f | head -n 1)"
if [ -z "${TARBALL}" ]; then
  echo "unable to find sentinel tarball in opennms-assemblies"
  exit 1
fi
rm -rf "${MYDIR}/tarball-root"
mkdir -p "${MYDIR}/tarball-root"
tar -x -z --strip-components 1 \
	-C "${MYDIR}/tarball-root" \
	-f "${TARBALL}"

BUILDER_NAME=sentinel-build
if [ $(docker buildx ls | grep -c "${BUILDER_NAME}") -gt 0 ]; then
  docker buildx rm $BUILDER_NAME
fi

docker context rm tls-environment >/dev/null 2>&1 || :
docker context create tls-environment
docker buildx create --name $BUILDER_NAME --driver docker-container --use tls-environment

docker buildx build -t sentinel \
  --builder $BUILDER_NAME \
  --platform="${DOCKER_ARCH}" \
  --build-arg BUILD_DATE="${BUILD_DATE}" \
  --build-arg VERSION="${VERSION}" \
  --build-arg SOURCE="${SOURCE}" \
  --build-arg REVISION="${REVISION}" \
  --build-arg BUILD_JOB_ID="${BUILD_JOB_ID}" \
  --build-arg BUILD_NUMBER="${BUILD_NUMBER}" \
  --build-arg BUILD_URL="${BUILD_URL}" \
  --build-arg BUILD_BRANCH="${BUILD_BRANCH}" \
  --load \
  --progress plain \
  .
docker image save sentinel -o "images/sentinel-${VERSION}.oci"

docker context rm tls-environment
