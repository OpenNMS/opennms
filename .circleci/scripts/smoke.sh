#!/bin/sh -e

SUITE="$1"; shift
if [ -z "$SUITE" ]; then
  SUITE="core"
fi

find_tests()
{
    circleci tests glob 'src/test/java/**/*.java' > /tmp/test-files.txt
    circleci tests split --split-by=timings < /tmp/test-files.txt | sed -e 's#^.*src/test/java/\(.*\)\.java#\1#' | tr "/" "."  > /tmp/this_node_tests
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
  "docker.elastic.co/elasticsearch/elasticsearch-oss:7.2.0" \
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
export MAVEN_OPTS="-Xmx1g -Xms1g"

# shellcheck disable=SC3045
# Set higher open files limit
ulimit -n 65536

cd smoke-test

if [ $SUITE = "minimal" ]; then
  echo "#### Executing minimal set smoke/system tests"
  IT_TESTS="MenuHeaderIT,SinglePortFlowsIT"
  SUITE=core
else
  find_tests
  echo "#### Executing complete suite of smoke/system tests"
  IT_TESTS="$(< /tmp/this_node_tests paste -s -d, -)"
fi

echo "tests: $IT_TESTS"
if [ -z "$IT_TESTS" ]; then
  echo "something went wrong, there are no tests to run"
  exit 1
fi

../compile.pl \
  -DskipTests=false \
  -DskipITs=false \
  -DfailIfNoTests=false \
  -Dtest.fork.count=0 \
  -Dit.test="$IT_TESTS" \
  --fail-fast \
  -N \
  '-P!smoke.all' \
  "-Psmoke.$SUITE" \
  install verify
