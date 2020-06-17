#!/usr/bin/env bash

TOPDIR="$(cd "$(dirname "$0")" || exit 1; pwd -P)"
# shellcheck disable=SC2001
TOPDIR="$(echo "$TOPDIR" | sed -e 's,opennms-container/.*$,opennms-container,')"

# Use version number from pom except from develop and features branches
case "${CIRCLE_BRANCH}" in
  master)
    VERSION="$("${TOPDIR}/pom2version.py" "${TOPDIR}/../pom.xml")"
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

[ -n "${BUILD_NETWORK}"           ] || BUILD_NETWORK="opennms-build-network"
[ -n "${BUILD_DATE}"              ] || BUILD_DATE="$(date -u "+%Y-%m-%dT%H:%M:%S%z")"
[ -n "${SOURCE}"                  ] || SOURCE="${CIRCLE_REPOSITORY_URL:-local-build}"
[ -n "${REVISION}"                ] || REVISION="$(git describe --always)"
[ -n "${BUILD_JOB_ID}"            ] || BUILD_JOB_ID="${CIRCLE_WORKFLOW_JOB_ID:-0}"
[ -n "${BUILD_NUMBER}"            ] || BUILD_NUMBER="${CIRCLE_BUILD_NUM:-0}"
[ -n "${BUILD_URL}"               ] || BUILD_URL="${CIRCLE_BUILD_URL}"
[ -n "${BUILD_BRANCH}"            ] || BUILD_BRANCH="${CIRCLE_BRANCH}"
[ -n "${BUILD_BRANCH}"            ] || BUILD_BRANCH="$(git branch --show-current)"

[ -n "${YUM_CONTAINER_NAME}"      ] || YUM_CONTAINER_NAME="yum-repo"
[ -n "${RPMDIR}"                  ] || RPMDIR="${TOPDIR}/../target/rpm/RPMS/noarch"

[ -n "${CONTAINER_PROJECT}"       ] || CONTAINER_PROJECT="$(basename "${TOPDIR}")"
[ -n "${CONTAINER_REGISTRY}"      ] || CONTAINER_REGISTRY="docker.io"
[ -n "${CONTAINER_REGISTRY_REPO}" ] || CONTAINER_REGISTRY_REPO="opennms"

# shellcheck disable=SC2034
# Array of tags for the OCI image used in the specific projects
OCI_TAGS=("${VERSION}")
