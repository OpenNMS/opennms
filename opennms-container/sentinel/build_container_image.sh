#!/usr/bin/env bash

# Exit immediately if anything returns non-zero
set -e

# Exit script if a statement returns a non-true return value.
set -o errexit

# Use the error status of the first failure, rather than that of the last item in a pipeline.
set -o pipefail

cd "$(dirname "$0")"

# shellcheck disable=SC1091
source ../set-build-environment.sh

../launch_apt_server.sh "$DEBDIR" "$REPO_PORT" "$APT_VOLUME"

if [ ! -d debs ]; then
  mkdir debs
fi

docker cp "${APT_CONTAINER_NAME}:/repo/pgp-key.public" debs/

# local apt repo
APT_CONTAINER_IP=$(docker inspect -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' "$APT_CONTAINER_NAME")
cat <<END >debs/opennms.list
deb [signed-by=/tmp/debs/pgp-key.public] http://${APT_CONTAINER_NAME}:${REPO_PORT} stable main
END


BUILDER_NAME=sentinel-build
if [ $(docker buildx ls | grep -c "${BUILDER_NAME}") -gt 0 ]; then
  docker buildx rm $BUILDER_NAME
fi

docker context create tls-environment
docker buildx create --name $BUILDER_NAME --driver docker-container --driver-opt network="${BUILD_NETWORK}" --use tls-environment

docker buildx build -t sentinel \
  --builder $BUILDER_NAME \
  --platform="${DOCKER_ARCH}" \
  --add-host ${APT_CONTAINER_NAME}:${APT_CONTAINER_IP} \
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
docker image save sentinel -o images/container.oci

rm -f debs/*.list
../stop_apt_server.sh $APT_CONTAINER_NAME $APT_VOLUME

docker context rm tls-environment
