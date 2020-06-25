#!/bin/sh -e

# If ran with 'true' then run a small subset of the tests
MINIMAL=0
if [ "$1" = "true" ]; then
  MINIMAL=1
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
CONTAINER_COUNT=12
for CONTAINER in \
  "alpine:3.5" \
  "testcontainersofficial/ryuk:0.3.0" \
  "selenium/standalone-firefox-debug:latest" \
  "cassandra:3.11.2" \
  "confluentinc/cp-kafka:5.2.1" \
  "confluentinc/cp-kafka:latest" \
  "docker.elastic.co/elasticsearch/elasticsearch-oss:7.2.0" \
  "docker.elastic.co/elasticsearch/elasticsearch-oss:latest" \
  "opennms/dummy-http-endpoint:0.0.2" \
  "opennms/dummy-http-endpoint:latest" \
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

cd ~/project/smoke-test
if [ $MINIMAL -eq 1 ]; then
  echo "#### Executing minimal set smoke/system tests"
  # Run a set of known tests
  for TEST_CLASS in "MenuHeaderIT" "SinglePortFlowsIT"
  do
    echo "###### Testing: ${TEST_CLASS}"
    mvn -N -DskipTests=false -DskipITs=false -Dit.test=$TEST_CLASS install verify
  done
else
  echo "#### Executing complete suite of smoke/system tests"
  find_tests
  # Iterate through the tests and stop after the first failure
  while read -r TEST_CLASS
  do
    echo "###### Testing: ${TEST_CLASS}"
    mvn -N -DskipTests=false -DskipITs=false -DfailIfNoTests=false -Dit.test="$TEST_CLASS" install verify
  done < /tmp/this_node_it_tests
fi

