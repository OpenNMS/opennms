##
# Makefile to build Horizon container image with Docker
##

CONTAINER_TYPE_FRIENDLY := Horizon
DOCKER_PROJECT          := horizon
TARBALL                 := ../../opennms-full-assembly/target/*-core.tar.gz
ADDITIONAL_TARGETS      := labels.txt

include ../common.mk

labels.txt::
	echo "org.opencontainers.image.created=\"$(BUILD_DATE)\"" > $@
	echo "org.opencontainers.image.title=\"OpenNMS Horizon $(VERSION)\"" >> $@
	echo "org.opencontainers.image.source=\"$(SOURCE)\"" >> $@
	echo "org.opencontainers.image.revision=\"$(REVISION)\"" >> $@
	echo "org.opencontainers.image.version=\"$(VERSION)\"" >> $@
	echo "org.opencontainers.image.vendor=\"The OpenNMS Group, Inc.\"" >> $@
	echo "org.opencontainers.image.authors=\"OpenNMS Community\"" >> $@
	echo "org.opencontainers.image.licenses=\"AGPL-3.0\"" >> $@
	echo "org.opennms.image.base=\"$(BASE_IMAGE)\"" >> $@
	test -z "$(BUILD_JOB_ID)" || echo "org.opennme.cicd.jobid=\"$(BUILD_JOB_ID)\"" >> $@
	echo "org.opennms.cicd.buildnumber=\"$(BUILD_NUMBER)\"" >> $@
	test -z "$(BUILD_URL)" || test "$(BUILD_URL)" = "unset" || echo "org.opennms.cicd.buildurl=\"$(BUILD_URL)\"" >> $@
	echo "org.opennms.cicd.branch=\"$(BUILD_BRANCH)\"" >> $@
