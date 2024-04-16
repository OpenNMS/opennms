#!/bin/bash

set -e
set -o pipefail

MYDIR="$(cd "$(dirname "$0")"; pwd)"

# shellcheck disable=SC1091
. "${MYDIR}/lib.sh"

echo "docker tags: ${DOCKER_TAGS[*]}"
echo ""

export DOCKER_SERVER="opennmspubacr.azurecr.io"
export DOCKER_USERNAME="${AZURE_SP}"
export DOCKER_PASSWORD="${AZURE_SP_PASSWORD}"

# shellcheck disable=SC1091
. "${MYDIR}/lib-docker.sh"

configure_cosign

printf '%s' "${AZURE_DCT_CI_KEY}" | base64 -d > "${PRIVATE_KEY_FOLDER}/${AZURE_DCT_CI_KEY_ID}.key"
printf '%s' "${AZURE_DCT_REPO_MINION_KEY}" | base64 -d > "${PRIVATE_KEY_FOLDER}/${AZURE_DCT_REPO_MINION_KEY_ID}.key"
chmod 600 "${PRIVATE_KEY_FOLDER}"/*

if [ "${DOCKER_CONTENT_TRUST}" -eq 1 ]; then
  export DOCKER_CONTENT_TRUST_REPOSITORY_PASSPHRASE="${AZURE_DCT_CI_PASSPHRASE}"
  docker trust key load "${PRIVATE_KEY_FOLDER}/${AZURE_DCT_CI_KEY_ID}.key"
fi

# shellcheck disable=SC2043
for TYPE in minion; do
  export DOCKER_REPO="${DOCKER_SERVER}/opennms/${TYPE}"

  # in Azure, only push the "branchname-arch" version of the individual ones
  find /tmp/artifacts/oci -name "${TYPE}-linux-*.oci" | sort -u | while read -r _file; do
    echo "* processing ${TYPE} image: ${_file}"
    _file_tag="$(basename "${_file}" | sed -e 's,\.oci$,,')"
    _internal_tag="opennms/${_file_tag}"
    _arch_tag="$(printf '%s' "${_file_tag}" | sed -e "s,^${TYPE}-,,")"

    _push_tag="${DOCKER_BRANCH_TAG}-${_arch_tag}"
    _tagname="${DOCKER_REPO}:${_push_tag}"
    docker tag "${_internal_tag}" "${_tagname}"
    do_with_retries docker push --quiet "${_tagname}"

    _sha256="$(get_sha256 "${_internal_tag}")"
    cosign_sign "${DOCKER_REPO}@${_sha256}" "/tmp/artifacts/xml/${_file_tag}-sbom.xml"
  done

  for _publish_tag in "${DOCKER_TAGS[@]}"; do
    create_and_push_manifest "${DOCKER_REPO}" "${DOCKER_BRANCH_TAG}" "${_publish_tag}"

    if [ "${DOCKER_CONTENT_TRUST}" -eq 1 ]; then
      export NOTARY_TARGETS_PASSPHRASE="${AZURE_DCT_REPO_MINION_KEY_PASSPHRASE}"
      do_with_retries notary -d ~/.docker/trust/ -s "https://${DOCKER_SERVER}" addhash "${DOCKER_REPO}" "${_publish_tag}" "${DOCKER_IMAGE_BYTES_SIZE}" --sha256 "${DOCKER_IMAGE_SHA_256}" --publish --verbose
    fi

    cosign_sign "${DOCKER_REPO}@sha256:${DOCKER_IMAGE_SHA_256}"
  done

done
