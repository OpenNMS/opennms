#!/usr/bin/env bash

# shellcheck disable=SC2034

# Container registry and tags
CONTAINER_PROJECT="$(basename "$(pwd)")"
CONTAINER_REGISTRY="docker.io"
CONTAINER_REGISTRY_REPO="opennms"
