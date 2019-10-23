#!/usr/bin/env bash

MYDIR="$(dirname "$0")"
TOPDIR="$(cd "$MYDIR"; pwd)"

# Make sure at least one Maven is in the path
PATH="$PATH:$TOPDIR/maven/bin"

set -e
set -o pipefail

JUNIT_PLUGIN_VERSION="$(grep '<maven.testing.plugin.version>' "${TOPDIR}/pom.xml" | sed -e 's,<[^>]*>,,g' -e 's, *,,g')"

# Ensure the plugin is available
# We swap to another directory so that we don't need to spend time parsing our current pom
(cd /tmp && mvn -llr org.apache.maven.plugins:maven-dependency-plugin:3.1.1:get \
      -DremoteRepositories=http://maven.opennms.org/content/groups/opennms.org-release/ \
      -Dartifact=org.opennms.maven.plugins:structure-maven-plugin:1.0 \
      && mvn org.apache.maven.plugins:maven-dependency-plugin::get \
        -DartifactId=surefire-junit4 -DgroupId=org.apache.maven.surefire -Dversion="$JUNIT_PLUGIN_VERSION")

# Now execute the plugin if the structure has not been generated yet
STRUCTURE_GRAPH_JSON="target/structure-graph.json"
if [ ! -e $STRUCTURE_GRAPH_JSON ]; then
  mvn org.opennms.maven.plugins:structure-maven-plugin:1.0:structure
else
  echo "Found existing Maven project structure map. Skipping generation."
  echo "(If you have modified the Maven project modules or structure in some way since then delete $STRUCTURE_GRAPH_JSON and run the script again.)"
fi

# Generate the list of tests to run, based on the files that have been changed
python3 .circleci/scripts/find-tests/find-tests.py generate-test-lists \
      --output-unit-test-classes=target/surefire_classnames \
      --output-integration-test-classes=target/failsafe_classnames \
      .

# Determine the Maven modules related to the tests we want to run
# (We could technically do this in one step, but we keep it separate to mimic what CI is doing )
cat target/*_classnames | python3 .circleci/scripts/find-tests/find-tests.py generate-test-modules \
      --output=target/test_projects \
      .

# Bail if there are no tests to run
if [ ! -s target/test_projects ]; then
  echo "No tests to run."
  exit 0
fi


# Run the tests
mvn \
           -P'!checkstyle' \
           -DupdatePolicy=never \
           -Dbuild.skip.tarball=true \
           -DfailIfNoTests=false \
           -DskipITs=false \
           -Dci.rerunFailingTestsCount=0 \
           -o \
           -B \
           -fae \
           -Dtest="$(< target/surefire_classnames paste -s -d, -)" \
           -Dit.test="$(< target/failsafe_classnames paste -s -d, -)" \
           -pl "$(< target/test_projects paste -s -d, -)" \
           "$@" \
           verify
