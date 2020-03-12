# Container registry and tags
VERSION                 := localbuild
SHELL                   := /bin/bash -o nounset -o pipefail -o errexit
BUILD_DATE              := $(shell date -u +"%Y-%m-%dT%H:%M:%SZ")
BASE_IMAGE              := opennms/deploy-base:1.0.0
DOCKER_CLI_EXPERIMENTAL := enabled
DOCKERX_INSTANCE	      := env-minion-oci
DOCKER_REGISTRY         := docker.io
DOCKER_ORG              := no42org
DOCKER_PROJECT          := minion
DOCKER_TAG              := $(DOCKER_REGISTRY)/$(DOCKER_ORG)/$(DOCKER_PROJECT):$(VERSION)
DOCKER_ARCH             := linux/amd64
DOCKER_FLAGS            := --output=type=docker,dest=images/minion.oci
SOURCE                  := $(shell git remote get-url origin)
REVISION                := $(shell git describe --always)
BUILD_NUMBER            := "unset"
BUILD_URL               := "unset"
BUILD_BRANCH            := $(shell git describe --always)

help:
	@echo ""
	@echo "Makefile to build Minion Docker container images, tag them and push them to a registry."
	@echo ""
	@echo "Requirements to build images:"
	@echo "  * Docker 18+"
	@echo "  * Buildx CLI tools from https://github.com/docker/buildx/releases in search path as docker-buildx"
	@echo "  * Minion as a tarball in tarball/minion.tar.gz"
	@echo ""
	@echo "Targets:"
	@echo "  help:      Show this help"
	@echo "  build:     Create an OCI image file in images/minion.oci"
	@echo "  test:      Test requirements to build the Minion OCI"
	@echo "  install:   Load the OCI file in your Docker instance"
	@echo "  uninstall: Remove the container image from your Docker instance"
	@echo "  clean:     Remove the Dockerx build instance keep the files in the directory images/"
	@echo "  clean-all: Remove the Dockerx build instance and delete *everything* in the directory images/"
	@echo ""
	@echo "Arguments to modify the build:"
	@echo "  VERSION:            Version number for this release of the deploy-base artefact, default: $(VERSION)"
	@echo "  BASE_IMAGE:         The base image we install our Java app as a tarball, default: $(BASE_IMAGE)"
	@echo "  DOCKERX_INSTANCE:   Name of the docker buildx instance, default: $(DOCKERX_INSTANCE)"
	@echo "  DOCKER_REGISTRY:    Registry to push the image to, default is set to $(DOCKER_REGISTRY)"
	@echo "  DOCKER_ORG:         Organisation where the image should pushed in the registry, default is set to $(DOCKER_ORG)"
	@echo "  DOCKER_PROJECT:     Name of the project in the registry, the default is set to $(DOCKER_PROJECT)"
	@echo "  DOCKER_TAG:         Docker tag is generated from registry, org, project, version and build number, set to $(DOCKER_TAG)"
	@echo "  DOCKER_ARCH:        Architecture for OCI image, default: $(DOCKER_ARCH)"
	@echo "  DOCKER_FLAGS:       Additional docker buildx flags, by default write a single architecture to a file, default: $(DOCKER_FLAGS)"
	@echo "  BUILD_NUMBER:       In case we run in CI/CD this is the build number which produced the artifact, default: $(BUILD_NUMBER)"
	@echo "  BUILD_URL:          In case we run in CI/CD this is the URL which for the build, default: $(BUILD_URL)"
	@echo "  BUILD_BRANCH:       In case we run in CI/CD this is the branch of the build, default: $(BUILD_BRANCH)"
	@echo ""
	@echo "Example:"
	@echo "  make build DOCKER_REGISTRY=myregistry.com DOCKER_ORG=myorg DOCKER_FLAGS=--push"
	@echo ""

test:
	command -v docker;
	command -v docker-buildx;
	test -f tarball/minion.tar.gz;

build: test
  # If we don't have a builder instance create it, otherwise use the existing one
	if ! docker-buildx inspect $(DOCKERX_INSTANCE); then docker-buildx create --name $(DOCKERX_INSTANCE); fi;
	docker-buildx use $(DOCKERX_INSTANCE);
	docker-buildx build --platform=$(DOCKER_ARCH) \
	  --build-arg VERSION=$(VERSION) \
		--build-arg BUILD_DATE=$(BUILD_DATE) \
		--build-arg SOURCE=$(SOURCE) \
		--build-arg REVISION=$(REVISION) \
		--build-arg BUILD_NUMBER=$(CIRCLE_BUILD_NUM) \
		--build-arg BUILD_URL=$(CIRCLE_BUILD_URL) \
		--build-arg BUILD_BRANCH=$(CIRCLE_BRANCH) \
		--tag=$(DOCKER_TAG) \
		$(DOCKER_FLAGS) \
		. ;

install: images/minion.oci
	docker image load -i images/minion.oci;

uninstall:
	docker rmi $(DOCKER_TAG)

clean:
	docker-buildx rm $(DOCKERX_INSTANCE);

clean-all:
	docker-buildx rm $(DOCKERX_INSTANCE);
	rm -rf images/*.*

.DEFAULT_GOAL := build

.PHONY: help build test install clean
