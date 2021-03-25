## 
# Makefile to build PRIS
##
.PHONY: help docs docs-docker docs-deps docs-deps-docker docs-serve docs-serve-stop docs-clean docs-clean-cache clean-all

.DEFAULT_GOAL := docs

SHELL                := /bin/bash -o nounset -o pipefail -o errexit
WORKING_DIRECTORY    := $(shell pwd)
DOCKER_ANTORA_IMAGE  := opennms/antora:2.3.4-b6293
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
	@echo "  docs-deps:        Test requirements to run Antora from the local system"
	@echo "  docs-deps-docker: Test requirements to run Antora with Docker"
	@echo "  docs:             Build Antora docs with a local install Antora, default target"
	@echo "  docs-docker:      Build Antora docs with from Docker"
	@echo "  docs-clean:       Clean all build artifacts in build and public directory"
	@echo "  docs-clean-cache: Clear git repository cache and UI components from .cache directory"
	@echo "  clean-all:        Clean build artifacts and Antora cache"
	@echo "  docs-serve:       Run a local web server with Docker and Nginx to serve the docs locally"
	@echo "  docs-serve-stop:  Stop the local web server for serving the docs"
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

docs-clean:
	@echo "Delete build and public artifacts ..."
	@rm -rf build public

docs-clean-cache:
	@echo "Clean Antora cache for git repositories and UI components ..."
	@rm -rf .cache

clean-all: docs-clean docs-clean-cache

docs-serve:
	@echo "Start Nginx with public folder as html root ..."
	docker run --rm -v $(WORKING_DIRECTORY)/public:/usr/share/nginx/html --name opennms-docs -p 8080:80 -d nginx

docs-serve-stop:
	@echo "Stopping Nginx docs server ..."
	docker stop opennms-docs

clean-all: docs-clean docs-clean-cache
    
