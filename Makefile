##
# Makefile to build OpenNMS from source
##
.DEFAULT_GOAL := quick-build

SHELL                 := /bin/bash -o nounset -o pipefail -o errexit
WORKING_DIRECTORY     := $(shell pwd)
SITE_FILE             := antora-playbook-local.yml
ARTIFACTS_DIR         := target/artifacts
MAVEN_BIN             := maven/bin/mvn
MAVEN_ARGS            := --batch-mode -DupdatePolicy=never -Djava.awt.headless=true -Daether.connector.resumeDownloads=false -Daether.connector.basic.threads=1 -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn -DvaadinJavaMaxMemory=2g -DmaxCpus=8 -Dstyle.color=always
export MAVEN_OPTS     := -Xms8g -Xmx8g -XX:ReservedCodeCacheSize=1g -XX:+TieredCompilation -XX:TieredStopAtLevel=1 -XX:-UseGCOverheadLimit -XX:-MaxFDLimit -Djdk.util.zip.disableZip64ExtraFieldValidation=true -Dmaven.wagon.http.retryHandler.count=3

GIT_BRANCH            := $(shell git branch | grep \* | cut -d' ' -f2)
OPENNMS_VERSION       := $(shell mvn org.apache.maven.plugins:maven-help-plugin:3.5.1:evaluate -Dexpression=project.version -q -DforceStdout)
VERSION               := $(shell echo ${OPENNMS_VERSION} | sed -e 's,-SNAPSHOT,,')
RELEASE_BUILD_KEY     := onms
RELEASE_BRANCH        := $(shell echo ${GIT_BRANCH} | sed -e 's,/,-,g')
ifndef CIRCLE_BUILD_NUM
override RELEASE_BUILD_NUM = 0
endif

RELEASE_BUILD_NUM     ?= ${CIRCLE_BUILD_NUM}
RELEASE_BUILDNAME     := ${RELEASE_BRANCH}
RELEASE_COMMIT        := $(shell git rev-parse --short HEAD)
RELEASE_MINOR_VERSION := $(shell git log --pretty="format:%cd" --date=short -1 | sed -e "s,^Date: *,," -e "s,-,,g" )
RELEASE_MICRO_VERSION := ${RELEASE_BUILD_KEY}.${RELEASE_BUILDNAME}.${RELEASE_BUILD_NUM}
OPEN_FILES_LIMIT      := 20000

RELEASE_VERSION     := UNSET.0.0
RELEASE_BRANCH      := develop
PUSH_RELEASE        := false
MAJOR_VERSION       := $(shell echo $(RELEASE_VERSION) | cut -d. -f1)
MINOR_VERSION       := $(shell echo $(RELEASE_VERSION) | cut -d. -f2)
PATCH_VERSION       := $(shell echo $(RELEASE_VERSION) | cut -d. -f3)
SNAPSHOT_VERSION    := $(MAJOR_VERSION).$(MINOR_VERSION).$(shell expr $(PATCH_VERSION) + 1)-SNAPSHOT
MAVEN_REPO          := bluebird-snapshots
MAVEN_USERNAME      := ""
MAVEN_PASSWORD      := ""
RELEASE_LOG         := target/release.log
OK                  := "[ 👍 ]"
SKIP                := "[ ⏭️ ]"
JAVA_MAJOR_VERSION  := 17

# Package requirements
OPENNMS_HOME          := /opt/opennms
OPENNMS_ETC           := /etc/opennms
OPENNMS_RRD_DATA      := /var/lib/opennms/rrd
OPENNMS_REPORTS_DATA  := /var/lib/opennms/reports
OPENNMS_LOGS_DATA     := /var/log/opennms
BUILD_ROOT            := $(ARTIFACTS_DIR)/buildroot
OPA_VERSION           := $(shell grep '<opennmsApiVersion>' pom.xml | sed -E 's/.*<opennmsApiVersion>([[:digit:]]+(\.[[:digit:]]+)+).*/\1/')
EXTRA_INFO            := $(GIT_BRANCH)
EXTRA_INFO2           := $(RELEASE_COMMIT)
PKG_RELEASE           := $(RELEASE_BUILD_NUM)
MAINTAINER_EMAIL      ?= maintainer@bluebirdops.org

INSTALL_VERSION       := ${VERSION}-0.${RELEASE_MINOR_VERSION}.${RELEASE_MICRO_VERSION}.${RELEASE_COMMIT}
DEPLOY_BASE_IMAGE     := quay.io/bluebird/deploy-base:2.0.2.b21
BUILD_DATE            := $(shell date '+%Y%m%d')
DOCKER_ARCH           := linux/amd64
OCI_REGISTRY          ?= quay.io
OCI_REGISTRY_USER     ?= changeme
OCI_REGISTRY_PASSWORD ?= changeme
OCI_REGISTRY_ORG      ?= changeme
TRIVY_ARGS            := --java-db-repository quay.io/bluebird/trivy-java-db:1 --timeout 30m --format json

.PHONY: help
help:
	@echo ""
	@echo "Makefile to build artifacts for OpenNMS"
	@echo ""
	@echo "Requirements to build:"
	@echo "  * OpenJDK 17 Development Kit"
	@echo "  * Maven 3.8.8, WARNING: Don't run with latest Maven, it throws errors. 3.8.8 is shipped with the git repo."
	@echo "  * NodeJS 18 with npm"
	@echo "  * Global install of yarn: npm install --global yarn"
	@echo "  * Global install of node-gyp to build the UI: yarn global add node-gyp"
	@echo "  * Antora"
	@echo "We are using the command tool to test for the requirements in your search path."
	@echo ""
	@echo "Build targets:"
	@echo "  help:                  Show this help"
	@echo "  validate:              Fail quickly by checking project structure with mvn:clean"
	@echo "  maven-structure-graph: Generate a JSON file with the Maven structure used to generate test class list"
	@echo "  test-lists:            Generate a list with all JUnit and Integration Test class names for splitting jobs"
	@echo "  compile:               Compile OpenNMS from source code with runs expensive tasks doing"
	@echo "  assemble:              Assemble the build artifacts with expensive tasks for a production build"
	@echo "  quick-build:           Runs a quick compile and quick assemble for development"
	@echo "  quick-compile:         Quick compile to get fast feedback for development"
	@echo "  quick-assemble:        Quick assemble to run on a build local system"
	@echo "  core-pkg-deb:          Build Core Debian packages"
	@echo "  core-pkg-rpm:          Build Core RPM packages"
	@echo "  minion-deb-pkg:        Build Minion Debian packages"
	@echo "  sentinel-deb-pkg:      Build Sentinel Debian packages"
	@echo ""
	@echo "Container Images:"
	@echo "  core-oci:              Build container image for Horizon Core, tag: opennms/horizon:latest"
	@echo "  minion-oci:            Build container image for Minion, tag opennms/minion:latest"
	@echo "  sentinel-oci:          Build container image for Sentinel, tag opennms/sentinel:latest"
	@echo "  show-core-oci:         Analyze the OCI image using dive, tag opennms/horizon:latest"
	@echo "  show-minion-oci:       Analyze the OCI image using dive, tag opennms/minion:latest"
	@echo "  show-sentinel-oci:     Analyze the OCI image using dive, tag opennms/sentinel:latest"
	@echo ""
	@echo "Dependencies and quality scans:"
	@echo "  core-oci-sbom:         Create software bill of material for the Core container image"
	@echo "  minion-oci-sbom:       Create software bill of material for the Minion container image"
	@echo "  sentinel-oci-sbom:     Create software bill of material for the Sentinel container image"
	@echo "  core-oci-sec-scan:     Create security scan report for the Core container image"
	@echo "  minion-oci-sec-scan:   Create security scan report for the Core container image"
	@echo "  sentinel-oci-sec-scan: Create security scan report for the Core container image"
	@echo "  code-coverage:         Test code coverage with SonarScanner CLI"
	@echo "  libyear:               Run libyear Maven pluginto show the freshness of lib dependencies"
	@echo ""
	@echo "Test suits:"
	@echo "  quick-smoke:           Simple smoke test to verify the application can be started by using the MenuHeaderIT and SinglePortFlowsIT test"
	@echo "  core-e2e:              Run full end to end test suite against the Core components. Specific tests can be set with: CORE_E2E_TESTS=MyTestIT-1,MyTestIT-2, ..."
	@echo "  minion-e2e:            Run end to end test suite against the Minion components. Specific tests can be set with: MINION_E2E_TESTS=MyTestIT-1,MyTestIT-2, ..."
	@echo "  sentinel-e2e:          Run end to end test suite against the Sentinel components. Specific tests can be set with: SENTINEL_E2E_TESTS=MyTestIT-1,MyTestIT-2, ..."
	@echo "  unit-tests:            Run full unit test suite, you can run specific tests in a projects with:"
	@echo "                           U_TESTS=org.opennms.netmgt.provision.detector.dhcp.DhcpDetectorTest TEST_PROJECTS=org.opennms:opennms-detector-dhcp"
	@echo "  integration-tests:     Run full integration test suit, you can run specific integration tests in a project with:"
	@echo "                           I_TESTS=org.opennms.netmgt.snmpinterfacepoller.SnmpPollerIT TEST_PROJECTS=org.opennms:opennms-services"
	@echo "  javadocs:              Generate Java docs"
	@echo "  docs:                  Build Antora docs with a local install Antora, default target"
	@echo "  install-core:          Install OpenNMS assembly to OPENMS_HOME to $(OPENNMS_HOME)"
	@echo "  uninstall-core:        Remove the installed version in OPENNMS_HOME from $(OPENNMS_HOME)"
	@echo "  clean:                 Clean assembly and docs and mostly used to recompile or rebuild from source"
	@echo "  clean-all:             Clean git repository with untracked files, docs, M2 opennms artifacts and build assemblies"
	@echo "  clean-git:             DELETE *all* untracked files from local git repository"
	@echo "  clean-m2:              Remove just OpenNMS build artifacts from Maven local repository"
	@echo "  clean-assembly:        Run mvn clean on assemblies, equivalent to clean.pl"
	@echo "  clean-docs:            Clean all docs build artifacts"
	@echo "  collect-artifacts:     Fetch and collect build artifacts in $(ARTIFACTS_DIR)"
	@echo "  collect-testresults:   Fetch test results from tests in $(ARTIFACTS_DIR)/tests"
	@echo "  spinup-postgres:       Spinup a PostgreSQL container to run integration tests used by integration tests"
	@echo "  destroy-postgres:      Shutdown and destroy the PostgreSQL container"
	@echo ""
	@echo "Arguments: "
	@echo "  SITE_FILE:           Antora site.yml file to build the site"
	@echo ""
	@echo ""

.PHONY: deps-build
deps-build:
	@echo "Check build dependencies: Java JDK, NodeJS, NPM, paste, python3 and yarn with node-gyp"
	@echo -n "👮‍♀️ Check Maven binary:          "
	@command -v $(MAVEN_BIN) > /dev/null
	@echo $(OK)
	@echo -n "👮‍♀️ Check Java runtime:          "
	@command -v java > /dev/null
	@echo $(OK)
	@echo -n "👮‍♀️ Check Java compiler:         "
	@command -v javac > /dev/null
	@echo $(OK)
	@echo -n "👮‍♀️ Check Node Package manager:  "
	@command -v npm > /dev/null
	@echo $(OK)
	@echo -n "👮‍♀️ Check paste binary:          "
	@command -v paste > /dev/null
	@echo $(OK)
	@echo -n "👮‍♀️ Check Python3:               "
	@command -v python3 > /dev/null
	@echo $(OK)
	@echo -n "👮‍♀️ Check yarn:                  "
	@command -v yarn > /dev/null
	@echo $(OK)
	@echo -n "👮‍♀️ Check node-gyp:              "
	@yarn global list | grep "^info \"node-gyp.*has binaries:"  > /dev/null
	@echo $(OK)
	@mkdir -p $(ARTIFACTS_DIR)
	@echo -n "👮‍♀️ Check Java version $(JAVA_MAJOR_VERSION):       "
	@java -version 2>&1 | grep '$(JAVA_MAJOR_VERSION)\..*' >/dev/null
	@echo $(OK)
	@echo -n "👮‍♀️ Check file limits ($(OPEN_FILES_LIMIT)):   "
	@if [ "$(shell ulimit -n)" -lt "$(OPEN_FILES_LIMIT)" ]; then echo -n "$(FAIL) "; echo "Increase your open file limit with: ulimit -n $(OPEN_FILES_LIMIT)"; exit 1; fi >/dev/null
	@echo $(OK)

.PHONY: deps-packages
deps-packages:
	@echo "Check dependencies to build packages"
	command -v fpm
	command -v rpmbuild

.PHONY: deps-docs
deps-docs:
	@echo "Check documentation build dependency: antora"
	command -v antora

.PHONY: deps-oci
deps-oci:
	@echo "Check OCI build dependency: docker"
	command -v docker
	command -v tar

.PHONY: deps-oci-sbom
deps-oci-sbom:
	@echo "Check OCI SBOM dependency: syft"
	command -v syft

.PHONY: deps-oci-sec-scan
deps-oci-sec-scan:
	@echo "Check OCI security scan dependency: trivy"
	command -v trivy

.PHONY: deps-sonar
deps-sonar:
	@echo "Check code coverage test dependency: sonar-scanner"
	command -v sonar-scanner

.PHONY: deps-oci-layers
deps-oci-layers:
	@echo "Show OCI container layer usage: dive"
	command -v dive

.PHONY: show-info
show-info:
	@echo "MAVEN_OPTS=\"$(MAVEN_OPTS)\""
	@echo "MAVEN_ARGS=\"$(MAVEN_ARGS)\""
	@$(MAVEN_BIN) --version

.PHONY: validate
validate: deps-build show-info
	$(MAVEN_BIN) clean
	$(MAVEN_BIN) clean --file opennms-full-assembly/pom.xml -Dbuild.profile=default

.PHONY: maven-structure-graph
maven-structure-graph: deps-build show-info
	$(MAVEN_BIN) org.opennms.maven.plugins:structure-maven-plugin:1.0:structure $(MAVEN_ARGS) -Dbuild.profile=default -Droot.dir=$(WORKING_DIRECTORY) --fail-at-end -Prun-expensive-tasks -Pbuild-bamboo

.PHONY: test-lists
test-lists: maven-structure-graph
	mkdir -p $(ARTIFACTS_DIR)/tests
	python3 .cicd-assets/find-tests/find-tests.py generate-test-lists --changes-only="false" --output-unit-test-classes="$(ARTIFACTS_DIR)/tests/unit_tests_classnames" --output-integration-test-classes="$(ARTIFACTS_DIR)/tests/integration_tests_classnames" .
	cat $(ARTIFACTS_DIR)/tests/*_tests_classnames | python3 .cicd-assets/find-tests/find-tests.py generate-test-modules --output="$(ARTIFACTS_DIR)/tests/test_modules" .
	find smoke-test -type f -regex ".*\/src\/test\/java\/.*IT.*\.java" | sed -e 's#^.*src/test/java/\(.*\)\.java#\1#' | tr "/" "." > $(ARTIFACTS_DIR)/tests/smoke_tests_classnames

.PHONY: compile
compile: maven-structure-graph
	$(MAVEN_BIN) install $(MAVEN_ARGS) -DskipTests=true -Dbuild.profile=default -Droot.dir=$(WORKING_DIRECTORY) -Dbuild.skip.tarball=false -Prun-expensive-tasks -Psmoke -Dbuild.type=production -Dbuild.sbom=true 2>&1 | tee $(ARTIFACTS_DIR)/mvn.compile.log

.PHONY: compile-ui
compile-ui:
	cd ui && yarn install && yarn build && yarn test

.PHONY: assemble
assemble: deps-build show-info
	$(MAVEN_BIN) install $(MAVEN_ARGS) -DskipTests=true -Dbuild.profile=default -Droot.dir=$(WORKING_DIRECTORY) -Dopennms.home=$(OPENNMS_HOME) -Dinstall.version=$(INSTALL_VERSION) -Pbuild-bamboo -Prun-expensive-tasks -Dbuild.skip.tarball=false -Denable.license=true -Dbuild.type=production --file opennms-full-assembly/pom.xml 2>&1 | tee $(ARTIFACTS_DIR)/mvn.assemble.log

.PHONY: quick-build
quick-build: quick-compile quick-assemble

.PHONY: quick-compile
quick-compile: maven-structure-graph
	$(MAVEN_BIN) install $(MAVEN_ARGS) -T 1C -DskipTests=true -Dbuild.profile=default -Droot.dir=$(WORKING_DIRECTORY) -Dcyclonedx.skip=true 2>&1 | tee $(ARTIFACTS_DIR)/mvn.quick-compile.log

.PHONY: quick-assemble
quick-assemble: deps-build show-info
	$(MAVEN_BIN) install $(MAVEN_ARGS) -DskipTests=true -Dbuild.profile=default -Droot.dir=$(WORKING_DIRECTORY) -Dopennms.home=$(OPENNMS_HOME) -Dinstall.version=$(INSTALL_VERSION) --file opennms-full-assembly/pom.xml 2>&1 | tee $(ARTIFACTS_DIR)/mvn.quick-assemble.log

.PHONY: core-oci
core-oci:
ifeq (,$(wildcard ./opennms-full-assembly/target/opennms-full-assembly-*-core.tar.gz))
	@echo "Can't build the Core container image, the build artifact"
	@echo "./opennms-full-assembly/target/opennms-full-assembly-$(OPENNMS_VERSION)-core.tar.gz doesn't exist."
	@echo ""
	@echo "You can create the artifact with:"
	@echo ""
	@echo "  make quick-compile && make quick-assemble"
	@echo ""
	@exit 1
endif
	mkdir -p opennms-container/core/tarball-root && \
	tar xzf opennms-full-assembly/target/opennms-full-assembly-*-core.tar.gz -C opennms-container/core/tarball-root && \
	cd opennms-container/core && \
    docker build --build-arg DEPLOY_BASE_IMAGE=$(DEPLOY_BASE_IMAGE) \
		 --build-arg BUILD_DATE=$(BUILD_DATE) \
		 --build-arg VERSION=$(OPENNMS_VERSION) \
		 --build-arg REVISION=$(RELEASE_COMMIT) \
		 -t opennms/horizon:latest .

.PHONY: minion-oci
minion-oci:
ifeq (,$(wildcard ./opennms-assemblies/minion/target/org.opennms.assemblies.minion-*-minion.tar.gz))
	@echo "Can't build the Minion container image, the build artifact"
	@echo "./opennms-assemblies/minion/target/org.opennms.assemblies.minion-$(OPENNMS_VERSION)-minion.tar.gz doesn't exist."
	@echo ""
	@echo "You can create the artifact with:"
	@echo ""
	@echo "  make quick-compile && make quick-assemble"
	@echo ""
	@exit 1
endif
	mkdir -p opennms-container/minion/tarball-root && \
	tar xzf opennms-assemblies/minion/target/org.opennms.assemblies.minion-*-minion.tar.gz --strip-component 1 -C opennms-container/minion/tarball-root && \
	cd opennms-container/minion && \
	cat minion-config-schema.yml.in | sed -e 's,@VERSION@,$(OPENNMS_VERSION),' \
		-e 's,@REVISION@,$(RELEASE_COMMIT),' \
		-e 's,@BRANCH@,$(GIT_BRANCH),' \
		-e 's,@BUILD_NUMBER@,$(RELEASE_BUILD_NUM),' > minion-config-schema.yml && \
    docker build --build-arg DEPLOY_BASE_IMAGE=$(DEPLOY_BASE_IMAGE) \
         --build-arg BUILD_DATE=$(BUILD_DATE) \
         --build-arg VERSION=$(OPENNMS_VERSION) \
         --build-arg REVISION=$(RELEASE_COMMIT) \
         -t opennms/minion:latest .

.PHONY: sentinel-oci
sentinel-oci:
ifeq (,$(wildcard ./opennms-assemblies/sentinel/target/org.opennms.assemblies.sentinel-*-sentinel.tar.gz))
	@echo "Can't build the Sentinel container image, the build artifact"
	@echo "./opennms-assemblies/sentinel/target/org.opennms.assemblies.sentinel-$(OPENNMS_VERSION)-sentinel.tar.gz doesn't exist."
	@echo ""
	@echo "You can create the artifact with:"
	@echo ""
	@echo "  make quick-compile && make quick-assemble"
	@echo ""
	@exit 1
endif
	mkdir -p opennms-container/sentinel/tarball-root && \
	tar xzf opennms-assemblies/sentinel/target/org.opennms.assemblies.sentinel-*-sentinel.tar.gz --strip-component 1 -C opennms-container/sentinel/tarball-root
	cd opennms-container/sentinel && \
    docker build --build-arg DEPLOY_BASE_IMAGE=$(DEPLOY_BASE_IMAGE) \
         --build-arg BUILD_DATE=$(BUILD_DATE) \
         --build-arg VERSION=$(OPENNMS_VERSION) \
         --build-arg REVISION=$(RELEASE_COMMIT) \
         -t opennms/sentinel:latest .

.PHONY: show-core-oci
show-core-oci: deps-oci-layers core-oci
	CI=true dive opennms/horizon:latest

.PHONY: show-minion-oci
show-minion-oci: deps-oci-layers minion-oci
	CI=true dive opennms/minion:latest

.PHONY: show-sentinel-oci
show-sentinel-oci: deps-oci-layers sentinel-oci
	CI=true dive opennms/sentinel:latest

.PHONY: core-oci-sbom
core-oci-sbom: deps-oci-sbom core-oci
	syft scan opennms/horizon:latest -o cyclonedx=$(ARTIFACTS_DIR)/oci/core-oci-sbom.xml --quiet

.PHONY: minion-oci-sbom
minion-oci-sbom: deps-oci-sbom minion-oci
	syft scan opennms/minion:latest -o cyclonedx=$(ARTIFACTS_DIR)/oci/minion-oci-sbom.xml --quiet

.PHONY: sentinel-oci-sbom
sentinel-oci-sbom: deps-oci-sbom sentinel-oci
	syft scan opennms/sentinel:latest -o cyclonedx=$(ARTIFACTS_DIR)/oci/sentinel-oci-sbom.xml --quiet

.PHONY: core-oci-sec-scan
core-oci-sec-scan: deps-oci-sec-scan core-oci
	trivy image opennms/horizon:latest $(TRIVY_ARGS) -o $(ARTIFACTS_DIR)/oci/core-trivy-report.json

.PHONY: minion-oci-sec-scan
minion-oci-sec-scan: deps-oci-sec-scan minion-oci
	trivy image opennms/minion:latest $(TRIVY_ARGS) -o $(ARTIFACTS_DIR)/oci/minion-trivy-report.json

.PHONY: sentinel-oci-sec-scan
sentinel-oci-sec-scan: deps-oci-sec-scan sentinel-oci
	trivy image opennms/sentinel:latest $(TRIVY_ARGS) -o $(ARTIFACTS_DIR)/oci/sentinel-trivy-report.json

# Run just the a very limited set of integration tests to verify the application comes up and we have something we can
# at least work with.
.PHONY: quick-smoke
quick-smoke: deps-oci core-oci test-lists
	$(MAVEN_BIN) install $(MAVEN_ARGS) -N -DskipTests=false -DskipITs=false -DfailIfNoTests=false -Dtest.fork.count=1 -Dit.test="MenuHeaderIT,SinglePortFlowsIT" --fail-fast -Dfailsafe.skipAfterFailureCount=1 -P!smoke.all -Psmoke.core --file smoke-test/pom.xml 2>&1 | tee $(ARTIFACTS_DIR)/mvn.smoke-quick.log

.PHONY: core-e2e
core-e2e: deps-oci test-lists core-oci minion-oci sentinel-oci
	$(eval CORE_E2E_TESTS ?= $(shell cat $(ARTIFACTS_DIR)/tests/smoke_tests_classnames | paste -s -d, -))
	$(MAVEN_BIN) install $(MAVEN_ARGS) -N -DskipTests=false -DskipITs=false -DfailIfNoTests=false -Dtest.fork.count=1 -Dit.test="$(CORE_E2E_TESTS)" --fail-fast -Dfailsafe.skipAfterFailureCount=1 -P!smoke.all -Psmoke.core --file smoke-test/pom.xml 2>&1 | tee $(ARTIFACTS_DIR)/mvn.core-smoke.log

.PHONY: minion-e2e
minion-e2e: deps-oci test-lists minion-oci sentinel-oci core-oci
	$(eval MINION_E2E_TESTS ?= $(shell cat $(ARTIFACTS_DIR)/tests/smoke_tests_classnames | paste -s -d, -))
	$(MAVEN_BIN) install $(MAVEN_ARGS) -N -DskipTests=false -DskipITs=false -DfailIfNoTests=false -Dtest.fork.count=1 -Dit.test="$(MINION_E2E_TESTS)" --fail-fast -Dfailsafe.skipAfterFailureCount=1 -P!smoke.all -Psmoke.minion --file smoke-test/pom.xml 2>&1 | tee $(ARTIFACTS_DIR)/mvn.minion-smoke.log

.PHONY: sentinel-e2e
sentinel-e2e: deps-oci test-lists sentinel-oci minion-oci core-oci
	$(eval SENTINEL_E2E_TESTS ?= $(shell cat $(ARTIFACTS_DIR)/tests/smoke_tests_classnames | paste -s -d, -))
	$(MAVEN_BIN) install $(MAVEN_ARGS) -N -DskipTests=false -DskipITs=false -DfailIfNoTests=false -Dtest.fork.count=1 -Dit.test="$(SENTINEL_E2E_TESTS)" --fail-fast -Dfailsafe.skipAfterFailureCount=1 -P!smoke.all -Psmoke.sentinel --file smoke-test/pom.xml 2>&1 | tee $(ARTIFACTS_DIR)/mvn.sentinel-smoke.log

# We allow users here to pass a specific unit tests and projects to run.
# Otherwise we run the full test suite
.PHONY: unit-tests
unit-tests: test-lists spinup-postgres
	$(eval U_TESTS ?= $(shell grep -Fxv -f ./.cicd-assets/_skipTests.txt ./target/artifacts/tests/unit_tests_classnames | paste -s -d, -))
	$(eval TESTS_PROJECTS ?= $(shell cat ${ARTIFACTS_DIR}/tests/test_modules | paste -s -d, -))
	# Parallel compiling with -T 1C works, but it doesn't for tests
	$(MAVEN_BIN) install $(MAVEN_ARGS) -T 1C -DskipTests=true -DskipITs=true -Dbuild.profile=default -Droot.dir=$(WORKING_DIRECTORY) -Dfailsafe.skipAfterFailureCount=1 -P!checkstyle -P!production -Pbuild-bamboo -Dbuild.skip.tarball=true -Dmaven.test.skip.exec=true --fail-fast --also-make --projects "$(TESTS_PROJECTS)" 2>&1 | tee $(ARTIFACTS_DIR)/mvn.tests.compile.log
	if [ $(command -v ionice) ]; then ionice; fi; nice $(MAVEN_BIN) install $(MAVEN_ARGS) -DskipTests=false -DskipITs=true -DskipSurefire=false -DskipFailsafe=true -Dbuild.profile=default -Droot.dir=$(WORKING_DIRECTORY) -Dfailsafe.skipAfterFailureCount=1 -P!checkstyle -P!production -Pbuild-bamboo -Pcoverage -Dbuild.skip.tarball=true -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false -Dfailsafe.failIfNoSpecifiedTests=false -DrunPingTests=false --fail-fast -Dorg.opennms.core.test-api.dbCreateThreads=1 -Dorg.opennms.core.test-api.snmp.useMockSnmpStrategy=false -Dtest="$(U_TESTS)" --projects "$(TESTS_PROJECTS)" 2>&1 | tee $(ARTIFACTS_DIR)/mvn.u_tests.log

.PHONY: integration-tests
integration-tests: test-lists spinup-postgres
	$(eval I_TESTS ?= $(shell grep -Fxv -f ./.cicd-assets/_skipIntegrationTests.txt ./target/artifacts/tests/integration_tests_classnames | paste -s -d, -))
	$(eval TESTS_PROJECTS ?= $(shell cat $(ARTIFACTS_DIR)/tests/test_modules | paste -s -d, -))
	# Parallel compiling with -T 1C works, but it doesn't for tests
	$(MAVEN_BIN) install $(MAVEN_ARGS) -T 1C -DskipTests=true -DskipITs=true -Dbuild.profile=default -Droot.dir=$(WORKING_DIRECTORY) -Dfailsafe.skipAfterFailureCount=1 -P!checkstyle -P!production -Pbuild-bamboo -Dbuild.skip.tarball=true -Dmaven.test.skip.exec=true --fail-fast --also-make --projects "$(TESTS_PROJECTS)" 2>&1 | tee $(ARTIFACTS_DIR)/mvn.tests.compile.log
	if [ $(command -v ionice) ]; then ionice; fi; nice $(MAVEN_BIN) install $(MAVEN_ARGS) -DskipTests=false -DskipITs=false -DskipSurefire=true -DskipFailsafe=false -Dbuild.profile=default -Droot.dir=$(WORKING_DIRECTORY) -Dfailsafe.skipAfterFailureCount=1 -P!checkstyle -P!production -Pbuild-bamboo -Pcoverage -Dbuild.skip.tarball=true -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false -Dfailsafe.failIfNoSpecifiedTests=false -DrunPingTests=false --fail-fast -Dorg.opennms.core.test-api.dbCreateThreads=1 -Dorg.opennms.core.test-api.snmp.useMockSnmpStrategy=false -Dtest="$(U_TESTS)" -Dit.test="$(I_TESTS)" --projects "$(TESTS_PROJECTS)" 2>&1 | tee $(ARTIFACTS_DIR)/mvn.i_tests.log

.PHONY: code-coverage
code-coverage: deps-sonar
	mkdir -p $(ARTIFACTS_DIR)/code-coverage
	# Generate a list with all Jacoco code coverage reports from compile phase
	find . -type f '!' -path './.git/*' -name jacoco.xml | sort -u > $(ARTIFACTS_DIR)/code-coverage/jacoco.xml

	# Get just the source folders from Java compiled targets and reverse engineer the main and assembly directory structure
	for src in $(shell find . -type d '!' -path './.git/*' -name target | sed -e 's,/target,/src,') ; do \
  		echo $$src/main ; \
  		echo $$src/assembly ; \
  	done \
  	| sort -u > $(ARTIFACTS_DIR)/code-coverage/source-folders.txt

	# Generate a list for all Junit report folders
	find . -type d '!' -path './.git/*' -a \( -name surefire-reports\* -o -name failsafe-reports\* \) | sort -u > $(ARTIFACTS_DIR)/code-coverage/junit-report-folders.txt

	# Get just the test folders from Java compiled targets and reverse engineer the test directory structure
	for src in $(shell find . -type d '!' -path './.git/*' -name target | sed -e 's,/target,/src,') ; do \
  		echo $$src/test ; \
  	done \
  	| sort -u > $(ARTIFACTS_DIR)/code-coverage/test-folders.txt

	# Get just test class folders from surefire or failsafe directories
	for test_classes_dir in $(shell cat target/artifacts/code-coverage/junit-report-folders.txt | sed -e 's,/surefire-reports,,' | sed -e 's,/failsafe-reports,,') ; do \
		find "$$test_classes_dir" -maxdepth 1 -type d -name test-classes ; \
	done \
	| sort -u > $(ARTIFACTS_DIR)/code-coverage/test-class-folders.txt

	# Get just class folders from surefire or failsafe directories
	for classes_dir in $(shell cat target/artifacts/code-coverage/junit-report-folders.txt | sed -e 's,/surefire-reports,,' | sed -e 's,/failsafe-reports,,') ; do \
		find "$$classes_dir" -maxdepth 1 -type d -name classes ; \
	done \
	| sort -u > $(ARTIFACTS_DIR)/code-coverage/class-folders.txt

	bash -c "sonar-scanner -Dsonar.host.url=\"https://sonarcloud.io\" \
                           -Djava.security.egd=file:/dev/./urandom \
                           -Dsonar.coverage.jacoco.xmlReportPaths=\"$(shell cat $(ARTIFACTS_DIR)/code-coverage/jacoco.xml | paste -s -d, -)\" \
                           -Dsonar.junit.reportPaths=\"$(shell cat $(ARTIFACTS_DIR)/code-coverage/junit-report-folders.txt | paste -s -d, -)\" \
                           -Dsonar.sources=\"$(shell cat $(ARTIFACTS_DIR)/code-coverage/source-folders.txt | paste -s -d, -)\" \
                           -Dsonar.tests=\"$(shell cat $(ARTIFACTS_DIR)/code-coverage/test-folders.txt | paste -s -d, -)\" \
                           -Dsonar.java.binaries=\"$(shell $(ARTIFACTS_DIR)/code-coverage/class-folders.txt | paste -s -d, -)\" \
                           -Dsonar.java.libraries=\"${HOME}/.m2/repository/**/*.jar,**/*.jar\" \
                           -Dsonar.java.test.binaries=\"$(shell cat $(ARTIFACTS_DIR)/code-coverage/test-class-folders.txt | paste -s -d, -)\" \
                           -Dsonar.java.test.libraries=\"${HOME}/.m2/repository/**/*.jar,**/*.jar\""

.PHONY: core-pkg-buildroot
core-pkg-buildroot:
ifeq (,$(wildcard ./opennms-full-assembly/target/opennms-full-assembly-*-core.tar.gz))
	@echo "Can't build the Core container image, the build artifact"
	@echo "./opennms-full-assembly/target/opennms-full-assembly-$(OPENNMS_VERSION)-core.tar.gz doesn't exist."
	@echo ""
	@echo "You can create the artifact with:"
	@echo ""
	@echo "  make quick-compile && make quick-assemble"
	@echo ""
	@exit 1
endif
	mkdir -p "$(BUILD_ROOT)/core/opt/opennms"
	tar xzf "./opennms-full-assembly/target/opennms-full-assembly-$(OPENNMS_VERSION)-core.tar.gz" -C "$(BUILD_ROOT)/core/opt/opennms"
	rm -rf "$(BUILD_ROOT)/core/opt/opennms/logs" \
           "$(BUILD_ROOT)/core/opt/opennms/share/rrd" \
           "$(BUILD_ROOT)/core/opt/opennms/share/reports"
	mkdir -p "$(BUILD_ROOT)/core$(OPENNMS_RRD_DATA)" \
             "$(BUILD_ROOT)/core$(OPENNMS_REPORTS_DATA)" \
             "$(BUILD_ROOT)/core$(OPENNMS_LOGS_DATA)" \
             "$(BUILD_ROOT)/core/usr/lib/systemd/system"
	cp "$(BUILD_ROOT)/core/opt/opennms/etc/opennms.service" "$(BUILD_ROOT)/core/usr/lib/systemd/system"
	mv "$(BUILD_ROOT)/core/opt/opennms/etc" "$(BUILD_ROOT)/core$(OPENNMS_ETC)"

.PHONY: core-pkg-deb
core-pkg-deb: deps-packages core-pkg-buildroot
	@echo "==== Building Debian Core Packages ===="
	@echo
	@echo "Version:     " $(OPENNMS_VERSION)
	@echo "Release:     " $(PKG_RELEASE)
	@echo "OPA VERSION: " $(OPA_VERSION)
	@echo
	fpm -s dir -t deb \
		-n "bbo-core" \
		-v "$(OPENNMS_VERSION)-$(PKG_RELEASE)" \
		--config-files /opt/opennms/etc \
		--description "BluebirdOps Network Management Platform" \
		--url "https://github.com/bluebird-community/opennms" \
		--maintainer "Maintainer <$(MAINTAINER_EMAIL)>" \
		--depends adduser \
		--depends jicmp \
		--depends jicmp6 \
		--depends jrrd2 \
		--deb-recommends openjdk-17-jdk-headless \
		--deb-suggests postgresql (>= 13.0) \
		--deb-suggests iplike-pgsql13 \
		--deb-suggests iplike-pgsql14 \
		--deb-suggests iplike-pgsql15 \
		--deb-suggests iplike-pgsql16 \
		--deb-suggests iplike-pgsql17 \
		--after-install packages/pkg-postinst-core.sh \
		-C "$(BUILD_ROOT)/core"

.PHONY: core-pkg-rpm
core-pkg-rpm: deps-packages core-pkg-buildroot
	@echo "==== Building RPM Core Packages ===="
	@echo
	@echo "Version:     " $(OPENNMS_VERSION)
	@echo "Release:     " $(PKG_RELEASE)
	@echo "OPA VERSION: " $(OPA_VERSION)
	@echo
	fpm -s dir -t rpm \
	    -n "bbo-core" \
		-v "$(OPENNMS_VERSION)_$(PKG_RELEASE)" \
		--config-files /opt/opennms/etc \
		--description "BluebirdOps Network Management Platform" \
		--url "https://github.com/bluebird-community/opennms" \
		--maintainer "Maintainer <$(MAINTAINER_EMAIL)>" \
		--depends jicmp \
		--depends jicmp6 \
		--depends jrrd2 \
		--rpm-tag "Recommends: java-17-openjdk-devel" \
		--rpm-tag "Suggests: postgresql-server >= 13.0" \
		--rpm-tag "Suggests: iplike-pgsql13" \
		--rpm-tag "Suggests: iplike-pgsql14" \
		--rpm-tag "Suggests: iplike-pgsql15" \
		--rpm-tag "Suggests: iplike-pgsql16" \
		--rpm-tag "Suggests: iplike-pgsql17" \
		--after-install packages/pkg-postinst-core.sh \
		-C "$(BUILD_ROOT)/core"

.PHONY: minion-deb-pkg
minion-deb-pkg: compile assemble
	@echo "==== Building Debian Minion ===="
	@echo
	@echo "Version:     " $(OPENNMS_VERSION)
	@echo "Release:     " $(DEB_PKG_RELEASE)
	@echo "OPA VERSION: " $(OPA_VERSION)
	@echo "DEBEMAIL:    " $(DEBEMAIL)
	@echo
	@echo "- adding auto-generated changelog entry"
	@cp opennms-assemblies/minion/target/org.opennms.assemblies.minion-*-minion.tar.gz "target/opennms-minion_$(OPENNMS_VERSION).orig.tar.gz"
	@cp opennms-assemblies/minion/target/org.opennms.assemblies.minion-*-minion.tar.gz "target/opennms-minion_$(OPENNMS_VERSION).tar.gz"
	@tar xzf "target/opennms-minion_$(OPENNMS_VERSION).tar.gz" -C target
	@sed -i='' "s/OPA_VERSION/$(OPA_VERSION)/g" target/minion-$(OPENNMS_VERSION)/debian/control
	cd target/minion-$(OPENNMS_VERSION); \
	export DEBEMAIL="$(DEBEMAIL)"; dch -b -v "$(OPENNMS_VERSION)-$(DEB_PKG_RELEASE)" "$(EXTRA_INFO)$(EXTRA_INFO2)"; \
	dpkg-buildpackage -us -uc -Zgzip
	mkdir -p $(ARTIFACTS_DIR)/debian/minion
	mv target/*.deb target/*.dsc target/*.debian.tar.gz target/*.buildinfo target/*.changes $(ARTIFACTS_DIR)/debian/minion

.PHONY: sentinel-deb-pkg
sentinel-deb-pkg: compile assemble
	@echo "==== Building Debian Sentinel ===="
	@echo
	@echo "Version:     " $(OPENNMS_VERSION)
	@echo "Release:     " $(DEB_PKG_RELEASE)
	@echo "OPA VERSION: " $(OPA_VERSION)
	@echo "DEBEMAIL:    " $(DEBEMAIL)
	@echo
	@echo "- adding auto-generated changelog entry"
	@cp opennms-assemblies/sentinel/target/org.opennms.assemblies.sentinel-*-sentinel.tar.gz "target/opennms-sentinel_$(OPENNMS_VERSION).orig.tar.gz"
	@cp opennms-assemblies/sentinel/target/org.opennms.assemblies.sentinel-*-sentinel.tar.gz "target/opennms-sentinel_$(OPENNMS_VERSION).tar.gz"
	@tar xzf "target/opennms-sentinel_$(OPENNMS_VERSION).tar.gz" -C target
	@sed -i='' "s/OPA_VERSION/$(OPA_VERSION)/g" target/sentinel-$(OPENNMS_VERSION)/debian/control
	cd target/sentinel-$(OPENNMS_VERSION); \
	export DEBEMAIL="$(DEBEMAIL)"; dch -b -v "$(OPENNMS_VERSION)-$(DEB_PKG_RELEASE)" "$(EXTRA_INFO)$(EXTRA_INFO2)"; \
	dpkg-buildpackage -us -uc -Zgzip
	mkdir -p $(ARTIFACTS_DIR)/debian/sentinel
	mv target/*.deb target/*.dsc target/*.debian.tar.gz target/*.buildinfo target/*.changes $(ARTIFACTS_DIR)/debian/sentinel

.PHONY: javadocs
javadocs: deps-build show-info
	$(MAVEN_BIN) javadoc:aggregate --batch-mode -Prun-expensive-tasks

.PHONY: docs
docs: deps-docs
	@echo "Build Antora docs..."
	antora --stacktrace $(SITE_FILE)

.PHONY: install-core
install-core: quick-compile quick-assemble
	@echo "Install OpenNMS Horizon Core to $(OPENNMS_HOME)"
	mkdir -p $(OPENNMS_HOME)
	tar xzf ./target/opennms-$(OPENNMS_VERSION).tar.gz -C $(OPENNMS_HOME)

.PHONY: uninstall-core
uninstall-core:
	@echo "Uninstall OpenNMS Horizon Core from $(OPENNMS_HOME)"
	rm -rf "$(OPENNMS_HOME)/*"

.PHONY: clean-all
clean-all: clean-docs clean-assembly clean-m2 clean-git

.PHONY: clean-git
clean-git:
	git clean -fdx

.PHONY: clean-m2
clean-m2:
	rm -rf ~/.m2/repository/org/opennms

.PHONY: clean-assembly
clean-assembly:
	$(MAVEN_BIN) -Passemblies clean

.PHONY: clean-docs
clean-docs:
	@echo "Delete build and public artifacts ..."
	@rm -rf build public
	@echo "Clean Antora cache for git repositories and UI components ..."
	@rm -rf .cache

.PHONY: clean
clean: clean-assembly clean-docs

.PHONY: collect-artifacts
# We use find with a regex, which exits gracefully when targets don't exist in case steps failed.
collect-artifacts:
	mkdir -p $(ARTIFACTS_DIR)/{archives,config-schema,oci}
	find . -type f -regex "^\.\/target\/opennms-.*\.tar\.gz" -exec mv -v {} $(ARTIFACTS_DIR)/archives \; # Fetch -source and assembled archive
	find . -type f -regex "^\.\/opennms-assemblies\/minion\/target\/org.opennms.assemblies.minion-.*\.tar\.gz" -exec mv -v {} $(ARTIFACTS_DIR)/archives/minion-${OPENNMS_VERSION}.tar.gz \;
	find . -type f -regex "^\.\/opennms-assemblies\/sentinel\/target\/org.opennms.assemblies.sentinel-.*\.tar\.gz" -exec mv -v {} $(ARTIFACTS_DIR)/archives/sentinel-${OPENNMS_VERSION}.tar.gz \;
	find . -type f -regex "^\.\/opennms-assemblies\/xsds\/target\/.*-xsds\.tar\.gz" -exec mv -v {} $(ARTIFACTS_DIR)/archives/opennms-${OPENNMS_VERSION}-xsds.tar.gz \;
	find . -type f -regex "^\.\/opennms-full-assembly\/target\/opennms-full-assembly-.*-core\.tar\.gz" -exec mv -v {} $(ARTIFACTS_DIR)/archives/opennms-${OPENNMS_VERSION}-core.tar.gz \;
	find . -type f -regex "^\.\/opennms-full-assembly\/target\/opennms-full-assembly-.*-optional\.tar\.gz" -exec mv -v {} $(ARTIFACTS_DIR)/archives/opennms-${OPENNMS_VERSION}-optional.tar.gz \;
	find . -type f -regex "^\.\/opennms-full-assembly\/target\/THIRD-PARTY.txt" -exec mv -v {} $(ARTIFACTS_DIR) \;
	find . -type f -regex "^\.\/opennms-container\/.*\/images\/.*\.oci" -exec mv -v {} $(ARTIFACTS_DIR)/oci \;
	find . -type f -regex "^\.\/target\/bom.*" -exec mv -v {} $(ARTIFACTS_DIR) \;

.PHONY: collect-testresults
collect-testresults:
	mkdir -p $(ARTIFACTS_DIR)/{surefire-reports,failsafe-reports,recordings}
	find . -type f -regex ".*\/target\/.*\.mp4" -exec mv -v {} $(ARTIFACTS_DIR)/recordings \;
	find . -type f -regex ".*\/target\/surefire-reports\/.*\.xml" -exec mv -v {} $(ARTIFACTS_DIR)/surefire-reports/ \;
	find . -type f -regex ".*\/target\/failsafe-reports\/.*\.xml" -exec mv -v {} $(ARTIFACTS_DIR)/failsafe-reports/ \;
	find . -type d -regex "^\.\/target\/logs" -exec tar czf $(ARTIFACTS_DIR)/logs.tar.gz {} \;
	find . -type d -regex "^\./smoke-test\/target\/logs" -exec tar czf $(ARTIFACTS_DIR)/smoke-test-logs.tar.gz {} \;
	find . -type d -regex "^\./smoke-test\/target\/screenshots" -exec tar czf $(ARTIFACTS_DIR)/smoke-test-screenshots.tar.gz {} \;
	find . -type f -regex "^\.\/target\/structure-graph\.json" -exec mv -v {} $(ARTIFACTS_DIR) \;

.PHONY: spinup-postgres
spinup-postgres: deps-oci
	@echo "Spin-up PostgreSQL database for tests using Docker Compose on port 5432/tcp"
	docker compose -f .cicd-assets/postgres/compose.yaml up -d

.PHONY: destroy-postgres
destroy-postgres: deps-oci
	@echo "Shutdown and remove PostgreSQL database using Docker Compose"
	docker compose -f .cicd-assets/postgres/compose.yaml down -v

.PHONY: registry-login
registry-login: deps-oci
	@echo ${OCI_REGISTRY_PASSWORD} | docker login --username ${OCI_REGISTRY_USER} --password-stdin ${OCI_REGISTRY}

.PHONY: libyear
libyear: deps-build
	@echo "Analyze dependency freshness measured in libyear"
	@mkdir -p $(ARTIFACTS_DIR)/logs
	$(MAVEN_BIN) $(MAVEN_ARGS) io.github.mfoo:libyear-maven-plugin:analyze 2>&1 | tee $(ARTIFACTS_DIR)/logs/libyear.log

.PHONY: release
release: deps-build
	@mkdir -p target
	@echo ""
	@echo "Release version:                $(RELEASE_VERSION)"
	@echo "New snapshot version:           $(SNAPSHOT_VERSION)"
	@echo "Git version tag:                v$(RELEASE_VERSION)"
	@echo "Release log:                    $(RELEASE_LOG)"
	@echo "Current branch:                 $(GIT_BRANCH)"
	@echo "Release branch:                 $(RELEASE_BRANCH)"
	@echo ""
	@echo -n "👮‍♀️ Check release branch:        "
	@if [ "$(GIT_BRANCH)" != "$(RELEASE_BRANCH)" ]; then echo "Releases are made from the $(RELEASE_BRANCH) branch, your branch is $(GIT_BRANCH)."; exit 1; fi
	@echo "$(OK)"
	@echo -n "👮‍♀️ Check branch in sync         "
	@if [ "$(git rev-parse HEAD)" != "$(git rev-parse @{u})" ]; then echo "$(RELEASE_BRANCH) branch not in sync with remote origin."; exit 1; fi
	@echo "$(OK)"
	@echo -n "👮‍♀️ Check uncommited changes     "
	@if git status --porcelain | grep -q .; then echo "There are uncommited changes in your repository."; exit 1; fi
	@echo "$(OK)"
	@echo -n "👮‍♀️ Check release version:       "
	@if [ "$(RELEASE_VERSION)" = "UNSET.0.0" ]; then echo "Set a release version, e.g. make release RELEASE_VERSION=1.0.0"; exit 1; fi
	@echo "$(OK)"
	@echo -n "👮‍♀️ Check version tag available: "
	@if git rev-parse v$(RELEASE_VERSION) >$(RELEASE_LOG) 2>&1; then echo "Tag v$(RELEASE_VERSION) already exists"; exit 1; fi
	@echo "$(OK)"
	@echo -n "💅 Set Maven release version:   "
	@mvn versions:set -DnewVersion=$(RELEASE_VERSION) >>$(RELEASE_LOG) 2>&1
	@echo "$(OK)"
	@echo -n "💅 Set version Karaf Test case: "
	@sed -i.versionsBackup 's/$(OPENNMS_VERSION)/$(RELEASE_VERSION)/g' opennms-full-assembly/src/test/java/org/opennms/assemblies/karaf/OnmsKarafTestCase.java >>$(RELEASE_LOG) 2>&1
	@echo "$(OK)"
	@echo -n "💅 Set version web assets:      "
	@sed -i.versionsBackup 's/$(OPENNMS_VERSION)/$(RELEASE_VERSION)/g' core/web-assets/package.json >>$(RELEASE_LOG) 2>&1
	@echo "$(OK)"
	@echo -n "💅 Set version Antora docs:     "
	@sed -i.versionsBackup 's/$(OPENNMS_VERSION)/$(RELEASE_VERSION)/g' docs/antora.yml >>$(RELEASE_LOG) 2>&1
	@echo "$(OK)"
	@echo -n "💅 Set version Maven deploy:    "
	@cd deploy && mvn versions:set -DnewVersion=$(RELEASE_VERSION) >>../$(RELEASE_LOG) 2>&1
	@echo "$(OK)"
	@echo -n "💅 Set version OSGi:            "
	@sed -i.versionsBackup 's/\<opennms\.osgi\.version\>$(VERSION).SNAPSHOT\<\/opennms\.osgi\.version\>/\<opennms\.osgi\.version\>$(RELEASE_VERSION)\<\/opennms\.osgi\.version\>/g' pom.xml >>$(RELEASE_LOG) 2>&1
	@echo "$(OK)"
	@echo -n "💅 Set version smoke-test:      "
	@cd smoke-test && mvn versions:set -DnewVersion=$(RELEASE_VERSION) >>../$(RELEASE_LOG) 2>&1
	@echo "$(OK)"
	@echo -n "👮‍♀️ Validate:                    "
	@mvn validate >>$(RELEASE_LOG) 2>&1
	@echo "$(OK)"
	@echo -n "🎁 Git commit new release       "
	@git commit --signoff -am "release: BluebirdOps $(RELEASE_VERSION)" >>$(RELEASE_LOG) 2>&1
	@echo "$(OK)"
	@echo -n "🦄 Set Git version tag:         "
	@git tag -a "v$(RELEASE_VERSION)" -m "Release BluebirdOps version $(RELEASE_VERSION)" >>$(RELEASE_LOG) 2>&1
	@echo "$(OK)"
	@echo -n "⬆️ Set Maven snapshot version:  "
	@mvn versions:set -DnewVersion=$(SNAPSHOT_VERSION) >>$(RELEASE_LOG) 2>&1
	@echo "$(OK)"
	@echo -n "⬆️ Set version Karaf Test case: "
	@sed -i.versionsBackup 's/$(RELEASE_VERSION)/$(SNAPSHOT_VERSION)/g' opennms-full-assembly/src/test/java/org/opennms/assemblies/karaf/OnmsKarafTestCase.java >>$(RELEASE_LOG) 2>&1
	@echo "$(OK)"
	@echo -n "⬆️ Set version web assets:      "
	@sed -i.versionsBackup 's/$(RELEASE_VERSION)/$(SNAPSHOT_VERSION)/g' core/web-assets/package.json >>$(RELEASE_LOG) 2>&1
	@echo "$(OK)"
	@echo -n "⬆️ Set version Antora docs:     "
	@sed -i.versionsBackup 's/$(RELEASE_VERSION)/$(SNAPSHOT_VERSION)/g' docs/antora.yml >>$(RELEASE_LOG) 2>&1
	@echo "$(OK)"
	@echo -n "⬆️ Set version Maven deploy:    "
	@cd deploy && mvn versions:set -DnewVersion=$(SNAPSHOT_VERSION) >>../$(RELEASE_LOG) 2>&1
	@echo "$(OK)"
	@echo -n "⬆️ Set version OSGi:            "
	@sed -i.versionsBackup 's/\<opennms\.osgi\.version\>$(RELEASE_VERSION)\<\/opennms\.osgi\.version\>/\<opennms\.osgi\.version\>$(MAJOR_VERSION).$(MINOR_VERSION).$(shell expr $(PATCH_VERSION) + 1).SNAPSHOT\<\/opennms\.osgi\.version\>/g' pom.xml >>$(RELEASE_LOG) 2>&1
	@echo "$(OK)"
	@echo -n "⬆️ Set version smoke-test:      "
	@cd smoke-test && mvn versions:set -DnewVersion=$(SNAPSHOT_VERSION) >>../$(RELEASE_LOG) 2>&1
	@echo "$(OK)"
	@echo -n "🎁 Git commit snapshot release: "
	@git commit --signoff -am "release: BluebirdOps $(SNAPSHOT_VERSION)" >>$(RELEASE_LOG) 2>&1
	@echo "$(OK)"
	@if [ "$(PUSH_RELEASE)" = "true" ]; then \
	    echo -n "🦄 Push commits                  "; \
  		git push >>$(RELEASE_LOG) 2>&1; \
		echo "$(OK)"; \
		echo -n "🚀 Push tag                      "; \
  		git push origin v$(RELEASE_VERSION) >>$(RELEASE_LOG) 2>&1; \
  		echo "$(OK)"; \
  	else \
  		echo "Push commits and tag:           $(SKIP)"; \
  	fi;

