##
# Makefile to build Sentinel container image with Docker
##
.PHONY: help test build install uninstall clean clean-all

.DEFAULT_GOAL := build

VERSION                 := $(shell ../../.circleci/scripts/pom2version.sh ../../pom.xml)
SHELL                   := /bin/bash -o nounset -o pipefail -o errexit
BUILD_DATE              := $(shell date -u +"%Y-%m-%dT%H:%M:%SZ")
BASE_IMAGE              := opennms/deploy-base:jre-2.1.0.b175
DOCKER_CLI_EXPERIMENTAL := enabled
DOCKERX_INSTANCE        := env-sentinel-oci
DOCKER_REGISTRY         := docker.io
DOCKER_ORG              := opennms
DOCKER_PROJECT          := sentinel
DOCKER_TAG              := $(DOCKER_REGISTRY)/$(DOCKER_ORG)/$(DOCKER_PROJECT):$(VERSION)
DOCKER_ARCH             := linux/amd64
DOCKER_IMAGE_NAME       := $(DOCKER_PROJECT)-$(VERSION).oci
DOCKER_FLAGS            := --output=type=docker,dest=images/$(DOCKER_IMAGE_NAME)
SOURCE                  := $(shell git remote get-url origin)
REVISION                := $(shell git describe --always)
BUILD_NUMBER            := 0
BUILD_URL               := "unset"
BUILD_BRANCH            := $(shell git describe --always)

help:
	@echo ""
	@echo "Makefile to build Sentinel Docker container images and push to a registry."
	@echo ""
	@echo "Requirements to build images:"
	@echo "  * Docker 19.03+"
	@echo "  * Sentinel assembled with: ./assemble.pl -Dopennms.home=/opt/opennms -DskipTests"
	@echo ""
	@echo "Targets:"
	@echo "  help:      Show this help"
	@echo "  build:     Create an OCI image file in images/$(DOCKER_IMAGE_NAME)"
	@echo "  test:      Test requirements to build the Sentinel OCI"
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
	@echo "  DOCKER_IMAGE_NAME:  Name of the OCI image, default: $(DOCKER_IMAGE_NAME)"
	@echo "  DOCKER_FLAGS:       Additional docker buildx flags, by default write a single architecture to a file, default: $(DOCKER_FLAGS)"
	@echo "  BUILD_NUMBER:       In case we run in CI/CD this is the build number which produced the artifact, default: $(BUILD_NUMBER)"
	@echo "  BUILD_URL:          In case we run in CI/CD this is the URL which for the build, default: $(BUILD_URL)"
	@echo "  BUILD_BRANCH:       In case we run in CI/CD this is the branch of the build, default: $(BUILD_BRANCH)"
	@echo ""
	@echo "Example:"
	@echo "  make build DOCKER_REGISTRY=myregistry.com DOCKER_ORG=myorg DOCKER_FLAGS=--push"
	@echo ""

test:
	@echo "Test Docker Buildx installation and existence of the Sentinel tarball ..."
	@command -v docker
	test -f ../../opennms-assemblies/sentinel/target/*sentinel*.tar.gz

unpack-tarball: test
	@if [ ! -e ./tarball-root/data/tmp/README ] || [ ../../opennms-assemblies/sentinel/target/*sentinel*.tar.gz -nt ./tarball-root/data/tmp/README ]; then \
	  echo "Unpacking Sentinel tarball for Docker context..."; \
	  mkdir -p tarball-root && \
	  tar -x -z --strip-components 1 -C ./tarball-root -f ../../opennms-assemblies/sentinel/target/*sentinel*.tar.gz && \
	  touch ./tarball-root/data/tmp/README; \
	fi

build: test unpack-tarball
	@echo "Copy Sentinel tarball to Docker context ..."
	@mkdir -p tarball-root && tar -x -z --strip-components 1 -C ./tarball-root -f ../../opennms-assemblies/sentinel/target/*sentinel*.tar.gz
	@echo "Initialize builder instance ..."
	if ! docker buildx inspect $(DOCKERX_INSTANCE); then docker context create "$(DOCKERX_INSTANCE)-context" && docker buildx create --name "$(DOCKERX_INSTANCE)" --driver docker-container --use "$(DOCKERX_INSTANCE)-context"; fi;
	docker buildx use $(DOCKERX_INSTANCE)
	@echo "Build container image for architecture: $(DOCKER_ARCH) ..."
	docker buildx build --platform=$(DOCKER_ARCH) \
	  --build-arg BASE_IMAGE=$(BASE_IMAGE) \
	  --build-arg VERSION=$(VERSION) \
	  --build-arg BUILD_DATE=$(BUILD_DATE) \
	  --build-arg SOURCE=$(SOURCE) \
	  --build-arg REVISION=$(REVISION) \
	  --build-arg BUILD_NUMBER=$(BUILD_NUMBER) \
	  --build-arg BUILD_URL=$(BUILD_URL) \
	  --build-arg BUILD_BRANCH=$(BUILD_BRANCH) \
	  --tag=$(DOCKER_TAG) \
	  $(DOCKER_FLAGS) \
	  .

install: build images/$(DOCKER_IMAGE_NAME)
	@echo "Load image ..."
	@docker image load -i "images/$(DOCKER_IMAGE_NAME)"
	@docker image tag "$(DOCKER_TAG)" "$(DOCKER_ORG)/$(DOCKER_PROJECT):$(VERSION)"
	@docker image tag "$(DOCKER_TAG)" "$(DOCKER_PROJECT):$(VERSION)"
	@docker image tag "$(DOCKER_TAG)" "$(DOCKER_PROJECT):latest"

uninstall:
	@echo "Remove image ..."
	@docker rmi $(DOCKER_TAG)

clean:
	@echo "Destroy builder environment: $(DOCKERX_INSTANCE) ..."
	@docker buildx rm $(DOCKERX_INSTANCE) >/dev/null 2>&1 || :

clean-all: clean
	@echo "Delete tarball and artifacts ..."
	@rm -rf images/*.oci
	@rm -rf tarball-root
