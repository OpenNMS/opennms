## 
# Makefile to build PRIS
##
.PHONY: help docs docs-docker deps-docs deps-docs-docker clean-docs clean-docs-cache clean-all

.DEFAULT_GOAL := docs

SHELL                := /bin/bash -o nounset -o pipefail -o errexit
WORKING_DIRECTORY    := $(shell pwd)
DOCKER_ANTORA_IMAGE  := antora/antora:2.3.4
SITE_FILE            := antora-playbook-local.yml

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
