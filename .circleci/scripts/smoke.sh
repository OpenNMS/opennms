#!/bin/sh -e

SUITE="$1"; shift
if [ -z "$SUITE" ]; then
  SUITE="core"
fi

find_tests()
{
    # Generate failsafe test list filtered to match the active suite's category
    # annotations, so circleci tests split only distributes tests that will
    # actually execute under -Psmoke.$SUITE (instead of splitting all 143 IT
    # files and having Maven silently skip the excluded categories).
    case "$SUITE" in
      core)
        # Core: all IT tests NOT tagged as Minion, Sentinel, or Flaky
        circleci tests glob '**/src/test/java/**/*IT*.java' \
          | xargs grep -L 'MinionTests\|SentinelTests\|FlakyTests' \
          | sed -e 's#^.*src/test/java/\(.*\)\.java#\1#' | tr "/" "." \
          > failsafe_classnames
        ;;
      minion)
        circleci tests glob '**/src/test/java/**/*IT*.java' \
          | xargs grep -l 'MinionTests' \
          | sed -e 's#^.*src/test/java/\(.*\)\.java#\1#' | tr "/" "." \
          > failsafe_classnames
        ;;
      sentinel)
        circleci tests glob '**/src/test/java/**/*IT*.java' \
          | xargs grep -l 'SentinelTests' \
          | sed -e 's#^.*src/test/java/\(.*\)\.java#\1#' | tr "/" "." \
          > failsafe_classnames
        ;;
      flaky)
        circleci tests glob '**/src/test/java/**/*IT*.java' \
          | xargs grep -l 'FlakyTests' \
          | sed -e 's#^.*src/test/java/\(.*\)\.java#\1#' | tr "/" "." \
          > failsafe_classnames
        ;;
      *)
        # Fallback: include all IT tests (e.g. smoke.all)
        circleci tests glob '**/src/test/java/**/*IT*.java' \
          | sed -e 's#^.*src/test/java/\(.*\)\.java#\1#' | tr "/" "." \
          > failsafe_classnames
        ;;
    esac

    circleci tests split --split-by=timings --timings-type=classname < failsafe_classnames > /tmp/this_node_it_tests
}

# Prime Docker to already contain the images we need in parallel, since
# testcontainers downloads them serially
echo "#### Priming Docker container cache"
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
  (docker pull "$CONTAINER" || true) &
done
wait
echo "#### All docker containers have now been pulled to the local cache"

# Configure the heap for the Maven JVM - the tests themselves are forked out in separate JVMs
# The heap size should be sufficient to buffer the output (stdout/stderr) from the test
export MAVEN_OPTS="-Xmx2g -Xms2g"

# shellcheck disable=SC3045
# Set higher open files limit
ulimit -n 65536

cd ~/project/smoke-test
if [ "$SUITE" = "minimal" ]; then
  echo "#### Executing minimal set smoke/system tests"
  IT_TESTS="MenuHeaderIT,SinglePortFlowsIT"
  SUITE=core
else
  find_tests
  echo "#### Executing complete suite of smoke/system tests"
  IT_TESTS="$(< /tmp/this_node_it_tests paste -s -d, -)"
fi

# When we are ready to collect coverage on smoke tests, add "-Pcoverage" below
../compile.pl \
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
