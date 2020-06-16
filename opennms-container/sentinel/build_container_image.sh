#!/usr/bin/env bash

# Exit script if a statement returns a non-true return value.
set -o errexit

# Use the error status of the first failure, rather than that of the last item in a pipeline.
set -o pipefail

[ -n "${YUM_CONTAINER_NAME}" ] || YUM_CONTAINER_NAME="yum-repo"
[ -n "${BUILD_NETWORK}"  ] || BUILD_NETWORK="opennms-build-network"

MYDIR="$(dirname "$0")"
cd "$MYDIR"

# shellcheck source=registry-config.sh
source ../registry-config.sh

# shellcheck source=opennms-container/version-n-tags.sh
source ../set-build-environment.sh

RPMDIR="$(cd ../../target/rpm/RPMS/noarch; pwd -P)"
../launch_yum_server.sh "$RPMDIR"

cat <<END >rpms/opennms-docker.repo
[opennms-repo-docker-common]
name=Local RPMs to Install from Docker
baseurl=http://${YUM_CONTAINER_NAME}:19990/
enabled=1
gpgcheck=0
END

docker build -t sentinel \
  --network "${BUILD_NETWORK}" \
  --build-arg BUILD_DATE="$(date -u +\"%Y-%m-%dT%H:%M:%S%z\")" \
  --build-arg VERSION="${VERSION}" \
  --build-arg SOURCE="${CIRCLE_REPOSITORY_URL}" \
  --build-arg REVISION="$(git describe --always)" \
  --build-arg BUILD_JOB_ID="${CIRCLE_WORKFLOW_JOB_ID}" \
  --build-arg BUILD_NUMBER="${CIRCLE_BUILD_NUM}" \
  --build-arg BUILD_URL="${CIRCLE_BUILD_URL}" \
  --build-arg BUILD_BRANCH="${CIRCLE_BRANCH}" \
  .

docker image save sentinel -o images/container.oci

rm -f rpms/*.repo
../stop_yum_server.sh
