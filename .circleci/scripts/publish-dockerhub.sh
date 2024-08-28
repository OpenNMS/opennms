#!/bin/bash

set -e
set -o pipefail

MYDIR="$(cd "$(dirname "$0")"; pwd)"

# shellcheck disable=SC1091
. "${MYDIR}/lib.sh"

echo "docker tags: ${DOCKER_TAGS[*]}"
echo ""

export DOCKER_SERVER="docker.io"
export DOCKER_USERNAME="${DOCKERHUB_LOGIN}"
export DOCKER_PASSWORD="${DOCKERHUB_PASS}"

# shellcheck disable=SC1091
. "${MYDIR}/lib-docker.sh"

configure_cosign

if [ "${DOCKER_CONTENT_TRUST}" -eq 1 ]; then
  printf '%s' "${DCT_DELEGATE_KEY}" | base64 -d > "${PRIVATE_KEY_FOLDER}/${DCT_DELEGATE_KEY_NAME}.key"
  chmod 600 "${PRIVATE_KEY_FOLDER}/${DCT_DELEGATE_KEY_NAME}.key"

  export DOCKER_CONTENT_TRUST_REPOSITORY_PASSPHRASE="${DCT_DELEGATE_KEY_PASSPHRASE}"
  docker trust key load "${PRIVATE_KEY_FOLDER}/${DCT_DELEGATE_KEY_NAME}.key"
fi

for TYPE in horizon minion sentinel; do
  export DOCKER_REPO="${DOCKER_SERVER}/opennms/${TYPE}"

  if [ "${DOCKER_CONTENT_TRUST}" -eq 1 ]; then
    # figure out DCT environment variables for $TYPE
    _key_contents_variable="$(printf 'DCT_REPO_%s_KEY' "${TYPE}" | tr '[:lower:]' '[:upper:]' | tr '-' '_')"
    _key_name_variable="$(printf 'DCT_REPO_%s_KEY_NAME' "${TYPE}" | tr '[:lower:]' '[:upper:]' | tr '-' '_')"
    _key_passphrase_variable="$(printf 'DCT_REPO_%s_KEY_PASSPHRASE' "${TYPE}" | tr '[:lower:]' '[:upper:]' | tr '-' '_')"

    # save and load $TYPE's key
    printf '%s' "${!_key_contents_variable}" | base64 -d > "${PRIVATE_KEY_FOLDER}/${!_key_name_variable}.key"
    chmod 600 "${PRIVATE_KEY_FOLDER}/${!_key_name_variable}.key"
    export DOCKER_CONTENT_TRUST_REPOSITORY_PASSPHRASE="${!_key_passphrase_variable}"
    docker trust key load "${PRIVATE_KEY_FOLDER}/${!_key_name_variable}.key"

    # put the passphrase back to the delegate for signing
    export DOCKER_CONTENT_TRUST_REPOSITORY_PASSPHRASE="${DCT_DELEGATE_KEY_PASSPHRASE}"
  fi

  # in dockerhub, only push the "branchname-arch" version of the individual ones
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
      export NOTARY_TARGETS_PASSPHRASE="${!_key_passphrase_variable}"
      do_with_retries notary -d ~/.docker/trust/ -s https://notary.docker.io addhash "${DOCKER_REPO}" "${_publish_tag}" "${DOCKER_IMAGE_BYTES_SIZE}" --sha256 "${DOCKER_IMAGE_SHA_256}" --publish --verbose
    fi

    cosign_sign "${DOCKER_REPO}@sha256:${DOCKER_IMAGE_SHA_256}"
  done

done
