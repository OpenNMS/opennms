#!/bin/sh -e

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
  "postgres:10.7-alpine" \
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

sudo apt update && sudo apt -y install openjdk-11-jdk-headless

# When we are ready to collect coverge on smoke tests, add "-Pcoverage" below
ionice nice ../compile.pl \
  -DskipTests=false \
  -DskipITs=false \
  -DfailIfNoTests=false \
  -Dtest.fork.count=1 \
  -Dit.test="$IT_TESTS" \
  --fail-fast \
  --batch-mode \
  -Dfailsafe.skipAfterFailureCount=1 \
  -N \
  '-P!smoke.all' \
  "-Psmoke.$SUITE" \
  install
