#!/usr/bin/env bash

TOPDIR="$(cd "$(dirname "$0")" || exit 1; pwd -P)"
# shellcheck disable=SC2001
TOPDIR="$(echo "$TOPDIR" | sed -e 's,opennms-container/.*$,opennms-container,')"

# Use version number from pom except from develop and features branches
POM_VERSION="$("${TOPDIR}/pom2version.py" "${TOPDIR}/../pom.xml")"
case "${CIRCLE_BRANCH}" in
  master-*)
    VERSION="${POM_VERSION}"
    ;;
  develop)
    VERSION="bleeding"
    ;;
  *)
    # Replace / in branch names which is not allowed in OCI tags
    VERSION="${CIRCLE_BRANCH//\//-}"
    ;;
esac

if [ -z "$VERSION" ]; then
  VERSION="$("${TOPDIR}/pom2version.py" "${TOPDIR}/../pom.xml")"
fi

[ -n "${BUILD_BRANCH}"            ] || BUILD_BRANCH="${CIRCLE_BRANCH}"
[ -n "${BUILD_BRANCH}"            ] || BUILD_BRANCH="$(git branch --show-current)"
[ -n "${BUILD_NETWORK}"           ] || BUILD_NETWORK="opennms-build-network"
[ -n "${BUILD_DATE}"              ] || BUILD_DATE="$(date -u "+%Y-%m-%dT%H:%M:%S%z")"
[ -n "${SOURCE}"                  ] || SOURCE="${CIRCLE_REPOSITORY_URL:-local-build}"
[ -n "${REVISION}"                ] || REVISION="${BUILD_BRANCH}-${POM_VERSION}-$(git rev-parse --short --verify HEAD)"
[ -n "${BUILD_JOB_ID}"            ] || BUILD_JOB_ID="${CIRCLE_WORKFLOW_JOB_ID:-0}"
[ -n "${BUILD_NUMBER}"            ] || BUILD_NUMBER="${CIRCLE_BUILD_NUM:-0}"
[ -n "${BUILD_URL}"               ] || BUILD_URL="${CIRCLE_BUILD_URL}"

[ -n "${YUM_CONTAINER_NAME}"      ] || YUM_CONTAINER_NAME="yum-repo"
[ -n "${RPMDIR}"                  ] || RPMDIR="${TOPDIR}/../target/rpm/RPMS/noarch"
[ -n "${DEBDIR}"                  ] || DEBDIR="${TOPDIR}/../target/debs"

[ -n "${CONTAINER_PROJECT}"       ] || CONTAINER_PROJECT="$(basename "${TOPDIR}")"
[ -n "${CONTAINER_REGISTRY}"      ] || CONTAINER_REGISTRY="docker.io"
[ -n "${CONTAINER_REGISTRY_REPO}" ] || CONTAINER_REGISTRY_REPO="opennms"

[ -n "${DOCKER_ARCH}" ] || DOCKER_ARCH="linux/amd64"

if [ "${DOCKER_ARCH}" = "linux/amd64" ]; then
  CONFD_ARCH="linux-amd64"
elif [ "${DOCKER_ARCH}" = "linux/arm64" ]; then
  CONFD_ARCH="linux-arm64"
else
  echo "ARCH ${DOCKER_ARCH} not supported by confd!"
  exit -1
fi

# shellcheck disable=SC2034
# Array of tags for the OCI image used in the specific projects
OCI_TAGS=("${VERSION}")
