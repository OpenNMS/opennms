#!/usr/bin/env bash

# shellcheck disable=SC2034

# Container registry and tags
CONTAINER_PROJECT="$(basename "$(pwd)")"
CONTAINER_REGISTRY="docker.io"
CONTAINER_REGISTRY_REPO="no42org"
CONTAINER_VERSION_TAGS=("${IMAGE_VERSION}"
                        "${VERSION}")
