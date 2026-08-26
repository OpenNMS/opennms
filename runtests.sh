#!/usr/bin/env bash
#
# Runs the same test-selection logic CircleCI uses, locally: work out which
# tests your changes touch, then run just those tests and their modules.
#
# This is a convenience wrapper, not the CI definition. The authoritative
# version is .circleci/scripts/itest.sh; if the two drift, that one is right.
# Last reconciled against .circleci/scripts/itest.sh on 2026-07-31.
#
# Usage:
#   ./runtests.sh [extra maven args...]
#
# Environment:
#   CHANGES_ONLY=false   consider all tests, not just the ones your diff touches
#

MYDIR="$(dirname "$0")"
TOPDIR="$(cd "$MYDIR"; pwd)"

# Make sure at least one Maven is in the path
PATH="$PATH:$TOPDIR/maven/bin"

set -e
set -o pipefail

FIND_TESTS_DIR="target/find-tests"
STRUCTURE_GRAPH_JSON="target/structure-graph.json"

mkdir -p "$FIND_TESTS_DIR"

# Generate the Maven project structure map if we don't already have one.
if [ ! -e "$STRUCTURE_GRAPH_JSON" ]; then
  mvn org.opennms.maven.plugins:structure-maven-plugin:1.0:structure
else
  echo "Found existing Maven project structure map. Skipping generation."
  echo "(If you have modified the Maven project modules or structure in some way since then delete $STRUCTURE_GRAPH_JSON and run the script again.)"
fi

# Generate the list of tests to run, based on the files that have been changed
python3 .circleci/scripts/find-tests/find-tests.py generate-test-lists \
      --changes-only="${CHANGES_ONLY:-true}" \
      --output-unit-test-classes="${FIND_TESTS_DIR}/surefire_classnames" \
      --output-integration-test-classes="${FIND_TESTS_DIR}/failsafe_classnames" \
      .

# Determine the Maven modules related to the tests we want to run
# (We could technically do this in one step, but we keep it separate to mimic what CI is doing)
cat "${FIND_TESTS_DIR}"/*_classnames | python3 .circleci/scripts/find-tests/find-tests.py generate-test-modules \
      --output="${FIND_TESTS_DIR}/test_projects" \
      .

# Bail if there are no tests to run
if [ ! -s "${FIND_TESTS_DIR}/test_projects" ]; then
  echo "No tests to run."
  exit 0
fi

MAVEN_ARGS=()

# Surefire and Failsafe fail on an empty -Dtest/-Dit.test, so skip the phase
# entirely when nothing of that kind was selected.
if [ -s "${FIND_TESTS_DIR}/surefire_classnames" ]; then
  MAVEN_ARGS+=("-Dtest=$(< "${FIND_TESTS_DIR}/surefire_classnames" paste -s -d, -)")
else
  MAVEN_ARGS+=("-DskipSurefire=true")
fi

if [ -s "${FIND_TESTS_DIR}/failsafe_classnames" ]; then
  MAVEN_ARGS+=("-Dit.test=$(< "${FIND_TESTS_DIR}/failsafe_classnames" paste -s -d, -)")
else
  MAVEN_ARGS+=("-DskipFailsafe=true")
fi

# Run the tests
mvn "${MAVEN_ARGS[@]}" \
           -P'!checkstyle' \
           -P'!production' \
           -DupdatePolicy=never \
           -Dbuild.skip.tarball=true \
           -DfailIfNoTests=false \
           -Dsurefire.failIfNoSpecifiedTests=false \
           -Dfailsafe.failIfNoSpecifiedTests=false \
           -DrunPingTests=false \
           -DskipITs=false \
           -B \
           -fae \
           -pl "$(< "${FIND_TESTS_DIR}/test_projects" paste -s -d, -)" \
           "$@" \
           verify
