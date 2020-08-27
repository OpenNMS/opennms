#!/usr/bin/env bash

# Exit script if a statement returns a non-true return value.
set -o errexit

# Use the error status of the first failure, rather than that of the last item in a pipeline.
set -o pipefail

cd "$(dirname "$0")"

# shellcheck disable=SC1091
source ../set-build-environment.sh

../launch_yum_server.sh "$RPMDIR"

cat <<END >rpms/opennms-docker.repo
[opennms-repo-docker-common]
name=Local RPMs to Install from Docker
baseurl=http://${YUM_CONTAINER_NAME}:19990/
enabled=1
gpgcheck=0
END

docker build -t minion \
  --network "${BUILD_NETWORK}" \
  --build-arg BUILD_DATE="${BUILD_DATE}" \
  --build-arg VERSION="${VERSION}" \
  --build-arg SOURCE="${SOURCE}" \
  --build-arg REVISION="${REVISION}" \
  --build-arg BUILD_JOB_ID="${BUILD_JOB_ID}" \
  --build-arg BUILD_NUMBER="${BUILD_NUMBER}" \
  --build-arg BUILD_URL="${BUILD_URL}" \
  --build-arg BUILD_BRANCH="${BUILD_BRANCH}" \
  .

docker image save minion -o images/container.oci

rm -f rpms/*.repo
../stop_yum_server.sh
