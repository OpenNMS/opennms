#!/bin/bash

export DOCKER_CONTENT_TRUST=0

export COSIGN_VERSION="1.13.1"
export ENABLE_COSIGN="0"

export KEY_FOLDER="${HOME}/.docker/trust"
export PRIVATE_KEY_FOLDER="${KEY_FOLDER}/private"
install -d -m 700 "${PRIVATE_KEY_FOLDER}"

if [ -z "${DOCKER_SERVER}" ] || [ -z "${DOCKER_USERNAME}" ] || [ -z "${DOCKER_PASSWORD}" ]; then
  echo "you must set \$DOCKER_SERVER, \$DOCKER_USERNAME, and \$DOCKER_PASSWORD"
  exit 1
fi
echo "${DOCKER_PASSWORD}" | docker login -u "${DOCKER_USERNAME}" --password-stdin "${DOCKER_SERVER}"

NOTARY_AUTH="$(printf '%s:%s' "${DOCKER_USERNAME}" "${DOCKER_PASSWORD}" | base64 -w0)"
export NOTARY_AUTH

configure_cosign() {
  if [ "${ENABLE_COSIGN}" -ne 1 ]; then
    return
  fi

  if [ ! -e /tmp/cosign.key ]; then
    printf '%s' "${COSIGN_PRIVATE_KEY}" | base64 -d > /tmp/cosign.key
  fi
  if [ ! -x "${HOME}/bin/cosign" ]; then
    curl -L -o "${HOME}/bin/cosign" "https://github.com/sigstore/cosign/releases/download/v${COSIGN_VERSION}/cosign-linux-amd64"
    chmod 755 "${HOME}/bin/cosign"
  fi
}

# usage: cosign_sign <docker-tag> [path-to-sbom]
cosign_sign() {
  local _tag="$1"
  local _sbom="$2"

  if [ "${ENABLE_COSIGN}" -ne 1 ]; then
    return
  fi

  if [ -z "${_tag}" ]; then
    echo 'you must specify a docker tag to sign!'
    exit 1
  fi
  cosign sign --key /tmp/cosign.key "${_tag}"

  # we won't attach an SBOM to the multi-arch tag, since their contents can be OS-dependent
  if [ -z "${_sbom}" ] || [ ! -e "${_sbom}" ]; then
    echo 'WARNING: sbom not set, or does not exist (this is OK when signing multi-arch tags)'
  else
    cosign attest --key /tmp/cosign.key --predicate "${_sbom}" "${_tag}"
  fi
}

get_sha256() {
  docker inspect --format='{{.RepoDigests}}' "$1" | sed -e 's,^\[,,' -e 's,\]$,,' | awk '{ print $1 }' | cut -d'@' -f2
}

# usage: create_and_push_manifest <registry> <source-tag> <target-tag>
# ex: create_and_push_manifest docker.io "develop" "31-dev"
create_and_push_manifest() {
  local _repo="$1"
  local _source_tag="$2"
  local _target_tag="$3"

  local IMAGE_REF="${_repo}:${_source_tag}"
  local TARGET_REF="${_repo}:${_target_tag}"
  docker manifest create "${TARGET_REF}" \
    "${IMAGE_REF}-linux-amd64" \
    "${IMAGE_REF}-linux-arm64" \
    --amend

  DOCKER_IMAGE_SHA_256="$(docker manifest push "${TARGET_REF}" --purge | cut -d ':' -f 2)"
  echo "Manifest SHA-256: ${DOCKER_IMAGE_SHA_256}"
  echo "Image-Ref: ${IMAGE_REF}"

  MANIFEST_FROM_REG="$(docker manifest inspect "${TARGET_REF}" -v)";
  DOCKER_IMAGE_BYTES_SIZE="$(printf '%s' "${MANIFEST_FROM_REG}" | jq -r '.[].Descriptor.size' | uniq | sort -n |tail  -1)";
  echo "Manifest-inspect BYTES: ${DOCKER_IMAGE_BYTES_SIZE}";

  #echo "Manifest contents:\n";
  #printf "${MANIFEST_FROM_REG}" | jq -r '.[].Descriptor | "Architecture: " + .platform.architecture + .platform.variant + ", digest: " + .digest';

  export DOCKER_IMAGE_SHA_256 DOCKER_IMAGE_BYTES_SIZE
}
