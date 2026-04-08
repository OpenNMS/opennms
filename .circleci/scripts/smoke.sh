#!/bin/bash
set -e
set -o pipefail

SUITE="$1"; shift
if [ -z "$SUITE" ]; then
  SUITE="core"
fi

find_tests()
{
    # Generate surefire test list
    circleci tests glob '**/src/test/java/**/*Test*.java' |\
        sed -e 's#^.*src/test/java/\(.*\)\.java#\1#' | tr "/" "." > surefire_classnames
    circleci tests split --split-by=timings --timings-type=classname < surefire_classnames > /tmp/this_node_tests

    # Generate failsafe list
    circleci tests glob '**/src/test/java/**/*IT*.java' |\
        sed -e 's#^.*src/test/java/\(.*\)\.java#\1#' | tr "/" "." > failsafe_classnames
    circleci tests split --split-by=timings --timings-type=classname < failsafe_classnames > /tmp/this_node_it_tests
}

# prime Docker to already contain the images we need in parallel, since
# testcontainers downloads them serially
echo "#### Priming Docker container cache"
CONTAINER_COUNT=10
touch /tmp/finished-containers.txt
for CONTAINER in \
  "alpine:3.5" \
  "testcontainersofficial/ryuk:0.3.0" \
  "selenium/standalone-firefox-debug:latest" \
  "cassandra:3.11.2" \
  "confluentinc/cp-kafka:5.2.1" \
  "confluentinc/cp-kafka:latest" \
  "docker.elastic.co/elasticsearch/elasticsearch:7.17.9" \
  "opennms/dummy-http-endpoint:0.0.2" \
  "postgres:13-alpine" \
  "postgres:latest" \
; do
  ( (docker pull "$CONTAINER" || :) && echo "$CONTAINER" >> /tmp/finished-containers.txt ) &
done

while true; do
  if [ "$(wc -l < /tmp/finished-containers.txt )" -ge $CONTAINER_COUNT ]; then
    echo "#### All docker containers have now been pulled to the local cache"
    break
  fi
  sleep 1
done

# Configure the heap for the Maven JVM - the tests themselves are forked out in separate JVMs
# The heap size should be sufficient to buffer the output (stdout/stderr) from the test
export MAVEN_OPTS="-Xmx2g -Xms2g"

# shellcheck disable=SC3045
# Set higher open files limit
ulimit -n 65536

cd ~/project/smoke-test
if [ $SUITE = "minimal" ]; then
  echo "#### Executing minimal set smoke/system tests"
  IT_TESTS="MenuHeaderIT,SinglePortFlowsIT"
  SUITE=core
else
  find_tests
  echo "#### Executing complete suite of smoke/system tests"
  IT_TESTS="$(< /tmp/this_node_it_tests paste -s -d, -)"
fi

sudo apt update && sudo apt -y install openjdk-17-jdk-headless

# When retries are enabled, use --fail-at-end so all tests run before retrying
FAILURE_MODE="--fail-fast"
SKIP_AFTER_FAILURE=""
if [ "${CCI_RERUN_FAILTEST:-0}" -gt 0 ]; then
  FAILURE_MODE="--fail-at-end"
else
  SKIP_AFTER_FAILURE="-Dfailsafe.skipAfterFailureCount=1"
fi

# When we are ready to collect coverge on smoke tests, add "-Pcoverage" below
set +e
ionice nice ../compile.pl \
  -DskipTests=false \
  -DskipITs=false \
  -DfailIfNoTests=false \
  -Dtest.fork.count=1 \
  -Dit.test="$IT_TESTS" \
  $FAILURE_MODE \
  --batch-mode \
  $SKIP_AFTER_FAILURE \
  -N \
  '-P!smoke.all' \
  "-Psmoke.$SUITE" \
  install
TEST_EXIT=$?
set -e

# Retry failed tests if configured
RETRIES_LEFT="${CCI_RERUN_FAILTEST:-0}"
MAX_RETRIES="$RETRIES_LEFT"
RETRIED_TESTS=""
while [ "$TEST_EXIT" -ne 0 ] && [ "$RETRIES_LEFT" -gt 0 ]; do
    ATTEMPT=$((MAX_RETRIES - RETRIES_LEFT + 1))
    echo "#### Finding failed smoke tests for re-run (attempt $ATTEMPT of $MAX_RETRIES)"

    set +e +o pipefail
    FAILED_TESTS=$(find . \( -path "*/failsafe-reports/TEST-*.xml" -o -path "*/surefire-reports/TEST-*.xml" \) \
      -exec grep -l -E 'failures="[1-9]|errors="[1-9]' {} + 2>/dev/null \
      | sed 's|.*/TEST-||;s|\.xml||' \
      | sort -u)
    set -e -o pipefail

    if [ -z "$FAILED_TESTS" ]; then
        echo "#### No failed tests found in reports, skipping retry"
        break
    fi

    echo "#### Failed tests: $FAILED_TESTS"
    RETRIED_TESTS="$FAILED_TESTS"

    # Clean failed test XML reports so fresh results are written
    set +e +o pipefail
    find . \( -path "*/failsafe-reports/TEST-*.xml" -o -path "*/surefire-reports/TEST-*.xml" \) \
      -exec grep -l -E 'failures="[1-9]|errors="[1-9]' {} + 2>/dev/null \
      | xargs rm -f
    set -e -o pipefail

    FAILED_ITS=$(echo "$FAILED_TESTS" | paste -s -d, -)

    echo "#### Re-running failed smoke tests: $FAILED_ITS"
    set +e
    ionice nice ../compile.pl \
      -DskipTests=false \
      -DskipITs=false \
      -DfailIfNoTests=false \
      -Dtest.fork.count=1 \
      -Dit.test="$FAILED_ITS" \
      --fail-at-end \
      --batch-mode \
      -N \
      '-P!smoke.all' \
      "-Psmoke.$SUITE" \
      install
    TEST_EXIT=$?
    set -e

    RETRIES_LEFT=$((RETRIES_LEFT - 1))
done

# Print retry summary
if [ -n "$RETRIED_TESTS" ]; then
    echo ""
    echo "========================================"
    echo "#### Retry Summary"
    echo "#### Retried tests: $(echo "$RETRIED_TESTS" | paste -s -d, -)"
    if [ "$TEST_EXIT" -eq 0 ]; then
        echo "#### Result: PASSED on retry (attempt $ATTEMPT)"
    else
        echo "#### Result: FAILED after $MAX_RETRIES retry attempt(s)"
    fi
    echo "========================================"
fi

exit $TEST_EXIT
