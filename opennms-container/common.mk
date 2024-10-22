##
# Common Makefile bits to build OpenNMS container images with Docker
##
.PHONY: help test docker-buildx-create oci build install uninstall uninstall-all clean clean-all

.DEFAULT_GOAL := image

ifeq (,$(shell which docker))
$(error 'docker' command not found, but this Makefile requires it)
endif

VERSION                 := $(shell ../../.circleci/scripts/pom2version.sh ../../pom.xml)
SHELL                   := /bin/bash -o nounset -o pipefail -o errexit
BUILD_DATE              := $(shell date -u +"%Y-%m-%dT%H:%M:%SZ")
BASE_IMAGE              := opennms/deploy-base:ubi9-3.5.0.b276-jre-17
DOCKER_CLI_EXPERIMENTAL := enabled
DOCKER_REGISTRY         := docker.io
DOCKER_ORG              := opennms
DOCKER_BASE             := $(DOCKER_ORG)/$(DOCKER_PROJECT)
DOCKER_TAG              := $(DOCKER_REGISTRY)/$(DOCKER_BASE):$(VERSION)
DOCKER_BUILDX_TAG       := $(DOCKER_BASE):buildx-$(shell echo $(BUILD_DATE) | sed 's/[-:]//g')
DOCKER_ARCH             := $(shell docker version --format '{{.Server.Os}}/{{.Server.Arch}}')
DOCKER_OCI              := images/$(DOCKER_PROJECT)-$(VERSION).oci
DOCKER_FLAGS            :=
DOCKER_OUTPUT           :=
DOCKER_OUTPUT_OCI       := type=docker,dest=$(DOCKER_OCI)
DOCKER_OUTPUT_IMAGE     := type=image
DOCKERX_INSTANCE        := opennms-build-env-oci
SOURCE                  := $(shell git remote get-url origin)
REVISION                := $(shell git describe --always)
BUILD_NUMBER            := 0
BUILD_URL               := "unset"
BUILD_BRANCH            := $(shell git describe --always)
README                  := tarball-root/data/tmp/README
ASSEMBLE_COMMAND        := ./assemble.pl -Dopennms.home=/opt/opennms -DskipTests

# These are defaults that some Makefiles will override so
# ?= for conditional assignment is used for these.
TAR_STRIP_COMPONENTS    ?= 0
ADDITIONAL_TARGETS      ?=

%.yml: %.yml.in
	@echo "Generating $@..."
	@sed -e 's,@VERSION@,$(VERSION),' \
		-e 's,@REVISION@,$(REVISION),' \
		-e 's,@BRANCH@,$(BUILD_BRANCH),' \
		-e 's,@BUILD_NUMBER@,$(BUILD_NUMBER),' $< > $@

help:
	@echo ""
	@echo "Makefile to build $(CONTAINER_TYPE_FRIENDLY) Docker container images and push to a registry."
	@echo ""
	@echo "Requirements to build images:"
	@echo "  * Docker 19.03+"
	@echo "  * $(CONTAINER_TYPE_FRIENDLY) assembled with: $(ASSEMBLE_COMMAND)"
	@echo ""
	@echo "Targets:"
	@echo "  help:      Show this help"
	@echo "  oci:       Create an OCI image file in $(DOCKER_OCI)"
	@echo "  image:     Create Docker image in the local repository"
	@echo "  test:      Test requirements to build the OCI"
	@echo "  install:   Load the OCI file in your Docker instance"
	@echo "  uninstall: Remove the container image from your Docker instance"
	@echo "  uninstall-all: Remove all container images built by this Makefile from your Docker instance"
	@echo "  clean:     Remove the Dockerx build instance, keeping the files in the directory images/"
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
	@echo "  DOCKER_OCI:         Path to OCI image, default: $(DOCKER_OCI)"
	@echo "  DOCKER_OUTPUT:      Output method: $(DOCKER_OUTPUT)"
	@echo "  DOCKER_OUTPUT_OCI:  DOCKER_OUTPUT value used to write a single architecture to a file, default: $(DOCKER_OUTPUT_OCI)"
	@echo "  DOCKER_OUTPUT_IMAGE: DOCKER_OUTPUT value used to store the image in Docker: $(DOCKER_OUTPUT_IMAGE)"
	@echo "  DOCKER_FLAGS:       Additional docker buildx flags, default: $(DOCKER_FLAGS)"
	@echo "  BUILD_NUMBER:       In case we run in CI/CD this is the build number which produced the artifact, default: $(BUILD_NUMBER)"
	@echo "  BUILD_URL:          In case we run in CI/CD this is the URL which for the build, default: $(BUILD_URL)"
	@echo "  BUILD_BRANCH:       In case we run in CI/CD this is the branch of the build, default: $(BUILD_BRANCH)"
	@echo ""
	@echo "Example:"
	@echo "  make build DOCKER_REGISTRY=myregistry.com DOCKER_ORG=myorg DOCKER_FLAGS=--push"
	@echo ""

# If we ever execute the commands in this recipe, it's because $(TARBALL)
# doesn't exist and make wants to build it, so tell the user we couldn't
# find the tarball and they need to run the assembly.
$(TARBALL):
	$(warning Couldn't find assembly tarball at $(TARBALL))
	$(error Go to the top-level and run this: $(ASSEMBLE_COMMAND))

test: $(TARBALL)
	$(info Ready to go, let's light this candle!)
	@true

# We do a sanity check first to make sure the assembly was built with
# the proper opennms.home path, otherwise the build can succeed but
# people get really weird startup errors from runjava because it can't
# find find-java.sh.
$(README): $(TARBALL) Dockerfile $(shell find container-fs -type f)
	@echo "Sanity checking OPENNMS_HOME path in **/fix-permissions script..."
	@fix_permissions_file=`tar -t -z -f $< | grep '/fix-permissions$$'` || exit 1 ; \
	  fix_permissions_opennms_home=`tar -x -z -f $< -O "$$fix_permissions_file" | \
	    egrep "^\\s*OPENNMS_HOME='" | sed "s/^.*OPENNMS_HOME='//;s/'//"` || exit 1 ; \
          expectation="/opt/opennms" ; \
	  if [ "$$fix_permissions_opennms_home" != "$$expectation" ]; then \
	    echo "OPENNMS_HOME in bin/fix-permissions from $< was not $$expectation" >&2 ; \
	    echo "OPENNMS_HOME was $$fix_permissions_opennms_home -- make sure -Dopennms.home=$$expectation is passed when assemble.pl is run" >&2 ; \
	    echo "Go to the top-level and run this: $(ASSEMBLE_COMMAND)" >&2 ; \
	    exit 1 ; \
	  fi
	@echo "Unpacking tarball for Docker context..."
	rm -rf tarball-root
	mkdir -p tarball-root
	tar -x -z --strip-components $(TAR_STRIP_COMPONENTS) -C ./tarball-root -f $<
	touch $@

unpack-tarball: $(README)

docker-buildx-create:
	$(info Initialize builder instance ...)
	docker context inspect "$(DOCKERX_INSTANCE)-context" > /dev/null 2>&1 || \
	  docker context create "$(DOCKERX_INSTANCE)-context"
	docker buildx inspect $(DOCKERX_INSTANCE) > /dev/null 2>&1 || \
	  docker --context "$(DOCKERX_INSTANCE)-context" buildx create \
	    --name "$(DOCKERX_INSTANCE)" --driver docker-container

# If DOCKERX_INSTANCE is set, we want to make sure docker-buildx-create
# is run before docker-buildx
ifdef DOCKERX_INSTANCE
docker-buildx: docker-buildx-create
else
docker-buildx: check-docker-buildx-default
endif

check-docker-buildx-default:
	CURRENT_BUILDX=`docker buildx inspect | head -1 | sed 's/^Name: *//'` ; \
	  if [ "$$CURRENT_BUILDX" != "default" ]; then \
	    echo "DOCKERX_INSTANCE is not set but there is a non-default docker buildx instance" >&2 ; \
	    echo "active: $$CURRENT_BUILDX" >&2 ; \
	    echo "You probably want to 'docker buildx use default' or delete the other instance" >&2 ; \
	    echo "with 'docker buildx rm $$CURRENT_BUILDX'." >&2 ; \
	    exit 1 ; \
	  fi

# The docker-buildx target is intended to be called from another recipe
# and DOCKER_OUTPUT needs to be set
docker-buildx: $(README) $(ADDITIONAL_TARGETS)
ifndef DOCKER_OUTPUT
	$(warning DOCKER_OUTPUT cannot be empty when running 'docker-buildx')
	$(warning The 'docker-buildx' goal is not intended to be run directly.)
	$(error Did you want to run 'make oci' or 'make image' instead?)
endif
	$(info Build container image for architecture: $(DOCKER_ARCH) ...)
	docker buildx build \
	  --builder=$(DOCKERX_INSTANCE) \
	  --platform=$(DOCKER_ARCH) \
	  --build-arg BASE_IMAGE=$(BASE_IMAGE) \
	  --build-arg VERSION=$(VERSION) \
	  --build-arg BUILD_DATE=$(BUILD_DATE) \
	  --build-arg SOURCE=$(SOURCE) \
	  --build-arg REVISION=$(REVISION) \
	  --build-arg BUILD_NUMBER=$(BUILD_NUMBER) \
	  --build-arg BUILD_URL=$(BUILD_URL) \
	  --build-arg BUILD_BRANCH=$(BUILD_BRANCH) \
	  --tag=$(DOCKER_TAG) \
	  --output=$(DOCKER_OUTPUT) \
	  $(DOCKER_FLAGS) \
	  .

$(DOCKER_OCI): $(README) $(ADDITIONAL_TARGETS)
	$(MAKE) DOCKER_OUTPUT="$(DOCKER_OUTPUT_OCI)" docker-buildx

oci: $(DOCKER_OCI)

# Don't use the builder when we are saving the image as a docker image,
# otherwise the image will be in the builder.
#
# We also add the "buildx" tag here, so we can cleanup images later.
# Without this, if you run 'make image' two times in a row, all of the
# tags on the first image will be removed when the second image is
# created and then moved to the second image, leaving an untagged
# image in the local repository. This will at least give us an easy
# way to cleanup those orphaned untagged images in 'uninstall-all'.
image:
	$(MAKE) DOCKER_OUTPUT="$(DOCKER_OUTPUT_IMAGE)" DOCKERX_INSTANCE="" docker-buildx
	@if ! docker image inspect "$(DOCKER_TAG)" > /dev/null; then \
	  echo "*** Could not find '$(DOCKER_TAG)' image" >&2 ; \
	  echo "See above--the image we just built is not showing up in the" >&2 ; \
	  echo "local repository. This is quite odd as it should be there." >&2 ; \
	  echo "This can happen if a docker buildx builder instance using" >&2 ; \
	  echo "the 'docker-container' driver was used to build the image." >&2 ; \
	  echo "Try running 'make clean' and building again." >&2 ; \
	  echo "Including 'docker buildx ls' output below" >&2 ; \
	  docker buildx ls >&2 ; \
	  exit 1 ; \
	fi
	docker image tag "$(DOCKER_TAG)" "$(DOCKER_BASE):$(VERSION)"
	docker image tag "$(DOCKER_TAG)" "$(DOCKER_BASE):latest"
	docker image tag "$(DOCKER_TAG)" "$(DOCKER_BUILDX_TAG)"

build:
	$(error 'build' has been removed and replaced with 'oci'. Also see 'image' goal.)

install: $(DOCKER_OCI)
	$(info Load image ...)
	docker image load -i "$(DOCKER_OCI)"
	docker image tag "$(DOCKER_TAG)" "$(DOCKER_BASE):$(VERSION)"
	docker image tag "$(DOCKER_TAG)" "$(DOCKER_BASE):latest"

uninstall:
	$(info Remove image(s) ...)
	-docker rmi "$(DOCKER_TAG)"
	-docker rmi "$(DOCKER_BASE):$(VERSION)"
	-docker rmi "$(DOCKER_BASE):latest"

uninstall-all: uninstall
	-docker image rm `docker image ls --format='{{ .Repository }}:{{ .Tag }}' '$(DOCKER_BASE):buildx-*T*Z'`

clean-context:
	$(info Destroy builder environment: $(DOCKERX_INSTANCE) ...)
	-docker buildx rm $(DOCKERX_INSTANCE)
	-docker context rm "$(DOCKERX_INSTANCE)-context"

clean: clean-context
	$(info Delete tarball and artifacts ...)
	rm -rf images/*.oci
	rm -rf tarball-root
	rm -rf $(ADDITIONAL_TARGETS)

clean-all: clean
