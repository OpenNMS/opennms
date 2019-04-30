#!/usr/bin/env bash

# Exit script if a statement returns a non-true return value.
set -o errexit

# Use the error status of the first failure, rather than that of the last item in a pipeline.
set -o pipefail

# shellcheck source=opennms-container/horizon/config.sh
source ./config.sh

# shellcheck source=opennms-container/registry-config.sh
source ../registry-config.sh

for TAG in ${IMAGE_VERSION[*]}; do
  docker push "${CONTAINER_REGISTRY}/${CONTAINER_REGISTRY_REPO}/horizon:${TAG}"
done
