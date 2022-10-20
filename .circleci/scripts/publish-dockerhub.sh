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
echo "EXITING"
echo "EXITING"
exit 0 

printf '%s' "${DCT_DELEGATE_KEY}" | base64 -d > "${PRIVATE_KEY_FOLDER}/${DCT_DELEGATE_KEY_NAME}.key"
chmod 600 "${PRIVATE_KEY_FOLDER}/${DCT_DELEGATE_KEY_NAME}.key"

export DOCKER_CONTENT_TRUST_REPOSITORY_PASSPHRASE="${DCT_DELEGATE_KEY_PASSPHRASE}"
docker trust key load "${PRIVATE_KEY_FOLDER}/${DCT_DELEGATE_KEY_NAME}.key"

for TYPE in horizon minion sentinel; do
  export DOCKER_REPO="${DOCKER_SERVER}/opennms/${TYPE}"

  # figure out DCT environment variables for $TYPE
  _key_contents_variable="$(printf 'DCT_REPO_%s_KEY' "${TYPE}" | tr '[:lower:]' '[:upper:]')"
  _key_name_variable="$(printf 'DCT_REPO_%s_KEY_NAME' "${TYPE}" | tr '[:lower:]' '[:upper:]')"
  _key_passphrase_variable="$(printf 'DCT_REPO_%s_KEY_PASSPHRASE' "${TYPE}" | tr '[:lower:]' '[:upper:]')"

  # save $TYPE's key
  printf '%s' "${!_key_contents_variable}" | base64 -d > "${PRIVATE_KEY_FOLDER}/${!_key_name_variable}.key"
  chmod 600 "${PRIVATE_KEY_FOLDER}/${!_key_name_variable}.key"

  # in dockerhub, only push the "branchname-arch" version of the individual ones
  find /tmp/artifacts/oci -name "${TYPE}-*.oci" | while read -r _file; do
    echo "* processing ${TYPE} image: ${_file}"
    _internal_tag="$(basename "${_file}" | sed -e 's,\.oci$,,')"
    _arch_tag="$(printf '%s' "${_internal_tag}" | sed -e "s,^${TYPE}-,,")"

    _push_tag="${DOCKER_BRANCH_TAG}-${_arch_tag}"
    docker tag "${_internal_tag}" "${DOCKER_REPO}:${_push_tag}"
    do_with_retries docker push --quiet "${DOCKER_REPO}:${_push_tag}"
  done

  export NOTARY_TARGETS_PASSPHRASE="${!_key_passphrase_variable}"
  for _publish_tag in "${DOCKER_TAGS[@]}"; do
    create_and_push_manifest "${DOCKER_REPO}" "${DOCKER_BRANCH_TAG}" "${_publish_tag}"
    do_with_retries notary -d ~/.docker/trust/ -s https://notary.docker.io addhash "${DOCKER_REPO}" "${_publish_tag}" "${DOCKER_IMAGE_BYTES_SIZE}" --sha256 "${DOCKER_IMAGE_SHA_256}" --publish --verbose
  done

done
