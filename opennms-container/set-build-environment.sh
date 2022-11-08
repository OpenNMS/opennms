#!/usr/bin/env bash

TOPDIR="$(cd "$(dirname "$0")" || exit 1; pwd -P)"
# shellcheck disable=SC2001
TOPDIR="$(echo "$TOPDIR" | sed -e 's,opennms-container/.*$,opennms-container,')"

POM2VERSION="${TOPDIR}/../.circleci/scripts/pom2version.sh"

# Use version number from pom except from develop and features branches
POM_VERSION="$("${POM2VERSION}" "${TOPDIR}/../pom.xml")"
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
  VERSION="$("${POM2VERSION}" "${TOPDIR}/../pom.xml")"
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

[ -n "${CONTAINER_PROJECT}"       ] || CONTAINER_PROJECT="$(basename "${TOPDIR}")"
[ -n "${CONTAINER_REGISTRY}"      ] || CONTAINER_REGISTRY="docker.io"
[ -n "${CONTAINER_REGISTRY_REPO}" ] || CONTAINER_REGISTRY_REPO="opennms"

[ -n "${REPO_PORT}"               ] || REPO_PORT="19990"

if [ -z "${DOCKER_ARCH}" ]; then
  _machine="$(uname -m)"
  case "${_machine}" in
    x86_64|amd64)
      DOCKER_ARCH="linux/amd64"
      ;;
    arm64)
      DOCKER_ARCH="linux/arm64"
      ;;
    *)
      echo "WARNING: unable to detect local arch, assuming linux/amd64"
      DOCKER_ARCH="linux/amd64"
      ;;
  esac
fi

# shellcheck disable=SC2034
# Array of tags for the OCI image used in the specific projects
OCI_TAGS=("${VERSION}")
