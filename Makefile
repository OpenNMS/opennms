## 
# Makefile to build PRIS
##
.PHONY: help docs docs-docker deps-docs deps-docs-docker clean-docs clean-docs-cache clean-all

.DEFAULT_GOAL := docs

SHELL                := /bin/bash -o nounset -o pipefail -o errexit
WORKING_DIRECTORY    := $(shell pwd)
DOCKER_ANTORA_IMAGE  := antora/antora:2.3.4
SITE_FILE            := antora-playbook-local.yml

help:
	@echo ""
	@echo "Makefile to build artifacts for OpenNMS"
	@echo ""
	@echo "Requirements to build the docs:"
	@echo "  * Native: Antora installed globally with antora binary in the search path"
	@echo "  * Docker: Docker installed with access to the official antora/antora image on DockerHub"
	@echo ""
	@echo "Targets:"
	@echo "  help:             Show this help"
	@echo "  deps-docs:        Test requirements to run Antora from the local system"
	@echo "  deps-docs-docker: Test requirements to run Antora with Docker"
	@echo "  docs:             Build Antora docs with a local install Antora, default target"
	@echo "  docs-docker:      Build Antora docs with from Docker"
	@echo "  clean-docs:       Clean all build artifacts in build and public directory"
	@echo "  clean-docs-cache: Clear git repository cache and UI components from .cache directory"
	@echo "  clean-all:        Clean build artifacts and Antora cache"
	@echo ""
	@echo "Arguments: "
	@echo "  DOCKER_ANTORA_IMAGE: Antora Docker image to build the documenation, default: $(DOCKER_ANTORA_IMAGE)"
	@echo "  SITE_FILE:           Antora site.yml file to build the site"
	@echo ""
	@echo "Example: "
	@echo "  make DOCKER_ANTORA_IMAGE=antora/antora:latest with-docker"
	@echo ""

deps-docs:
	@command -v antora

deps-docs-docker:
	@command -v docker

docs: deps-docs
	@echo "Build Antora docs..."
	antora --stacktrace $(SITE_FILE)

docs-docker: deps-docs-docker
	@echo "Build Antora docs with docker ..."
	docker run --rm -v $(WORKING_DIRECTORY):/antora $(DOCKER_ANTORA_IMAGE) --stacktrace generate $(SITE_FILE)

clean-docs:
	@echo "Delete build and public artifacts ..."
	@rm -rf build public

clean-docs-cache:
	@echo "Clean Antora cache for git repositories and UI components ..."
	@rm -rf .cache

clean-all: clean-docs clean-docs-cache
