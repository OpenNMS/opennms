#!/bin/bash

set -e
set -o pipefail

MYDIR="$(dirname "$0")"
MYDIR="$(cd "$MYDIR" || exit 1; pwd)"

PROJECT="opennms"
REPO=""
VERSION="$("${MYDIR}/pom2version.sh" "${MYDIR}/../../pom.xml")"

case "${CIRCLE_BRANCH}" in
  develop)
    REPO="develop"
    ;;
  foundation-*)
    REPO="${CIRCLE_BRANCH}"
    ;;
  release-*)
    REPO="testing"
    ;;
  master-*)
    REPO="stable"
    ;;
  *)
    echo "This branch is not eligible for deployment: ${CIRCLE_BRANCH}"
    exit 0
    ;;
esac

OPTS=(--verbose --no-wait-for-sync --republish --error-retry-max 3)

cloudsmith push raw \
  "${OPTS[@]}" \
  --version "${VERSION}" \
  --name "${REPO}/minion-config-schema.yml" \
  --description "minion-config-schema.yml for version ${VERSION} in the ${REPO} repository" \
  "${PROJECT}/config-schema" \
  "/tmp/artifacts/yml/minion-config-schema.yml"

for FILE in /tmp/artifacts/rpm/*.rpm; do
  cloudsmith push rpm "${OPTS[@]}" "${PROJECT}/$REPO/any-distro/any-version" "$FILE"
done
for FILE in /tmp/artifacts/deb/*.deb; do
  cloudsmith push deb "${OPTS[@]}" "${PROJECT}/$REPO/any-distro/any-version" "$FILE"
done

# shellcheck disable=SC1091
. "${MYDIR}/lib.sh"

export DOCKER_SERVER="docker.cloudsmith.io"
export DOCKER_USERNAME="${CLOUDSMITH_USERNAME}"
export DOCKER_PASSWORD="${CLOUDSMITH_API_KEY}"

# shellcheck disable=SC1091
. "${MYDIR}/lib-docker.sh"

# never do DCT on Cloudsmith, they don't support it
export DOCKER_CONTENT_TRUST=0

configure_cosign

for TYPE in horizon minion sentinel; do
  export DOCKER_REPO="${DOCKER_SERVER}/opennms/${REPO}/${TYPE}"

  find /tmp/artifacts/oci -name "${TYPE}-linux-*.oci" | sort -u | while read -r _file; do
    echo "* processing ${TYPE} image: ${_file}"
    _file_tag="$(basename "${_file}" | sed -e 's,\.oci$,,')"
    _internal_tag="opennms/${_file_tag}"
    _arch_tag="$(printf '%s' "${_file_tag}" | sed -e "s,^${TYPE}-,,")"

    echo "${TYPE}: tag=${_internal_tag}, arch_tag=${_arch_tag}, file=${_file}"
    for _publish_tag in "${DOCKER_TAGS[@]}"; do
      _tagname="${DOCKER_REPO}:${_publish_tag}-${_arch_tag}"
      echo "* pushing ${TYPE} (${_arch_tag}) to Cloudsmith as ${_tagname}"
      docker tag "${_internal_tag}" "${_tagname}"
      do_with_retries docker push --quiet "${_tagname}"
      # if this is the "amd64" version, then push it again without the arch modifier
      if [ "${_arch_tag}" = "linux-amd64" ]; then
        _tagname="${DOCKER_REPO}:${_publish_tag}"
        docker tag "${_internal_tag}" "${_tagname}"
        do_with_retries docker push --quiet "${_tagname}"
      fi
    done

    _sha256="$(get_sha256 "${_internal_tag}")"
    cosign_sign "${DOCKER_REPO}@${_sha256}" "/tmp/artifacts/xml/${_file_tag}-sbom.xml"
  done

done
