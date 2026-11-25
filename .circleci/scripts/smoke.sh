#!/bin/bash
set -e
set -o pipefail

. "$(dirname "$0")/rerun-lib.sh"

SUITE="$1"; shift
if [ -z "$SUITE" ]; then
  SUITE="core"
fi

retry()
{
	"$@" || "$@"
}

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
  "testcontainersofficial/ryuk:0.11.0" \
  "selenium/standalone-firefox-debug:latest" \
  "cassandra:3.11.2" \
  "confluentinc/cp-kafka:5.2.1" \
  "confluentinc/cp-kafka:latest" \
  "docker.elastic.co/elasticsearch/elasticsearch:7.17.9" \
  "opennms/dummy-http-endpoint:0.0.2" \
  "postgres:14.22-alpine" \
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

# bound each attempt so a hung mirror fails fast instead of stalling the job
# (--foreground: sudo needs the tty's foreground process group or it gets stopped)
retry timeout --foreground -k 30s 5m sudo apt-get -y update </dev/null \
  && retry timeout --foreground -k 30s 5m sudo env DEBIAN_FRONTEND=noninteractive \
       apt-get -y install openjdk-25-jdk-headless </dev/null \
  || exit 1

RERUN_COUNT="${CCI_RERUN_FAILTEST:-0}"

# Pass 0 runs the tests of this node. A later pass runs only the failed classes.
run_tests()
{
  local pass="$1" units="$2" its="$3"
  local args=()

  if [ "$pass" -eq 0 ]; then
    args+=("-DskipITs=false" "-Dit.test=${IT_TESTS}")
  else
    # Failsafe writes its reports to target/surefire-reports in this module, so the class name gives the plugin.
    local all="${units}${units:+${its:+,}}${its}"
    its="$(echo "$all" | tr ',' '\n' | grep -E '(^|\.)(IT[^.]*|[^.]*IT|[^.]*ITCase)$' | paste -s -d, - || true)"
    units="$(echo "$all" | tr ',' '\n' | grep -v -E '(^|\.)(IT[^.]*|[^.]*IT|[^.]*ITCase)$' | paste -s -d, - || true)"

    # An empty it.test value runs all the ITs, so skip the plugin that has no failed class.
    if [ -n "$units" ]; then args+=("-Dtest=${units}"); else args+=("-Dskip.surefire.tests=true"); fi
    if [ -n "$its" ]; then args+=("-DskipITs=false" "-Dit.test=${its}"); else args+=("-DskipITs=true"); fi
  fi

  if [ "$RERUN_COUNT" -gt 0 ]; then
    args+=("${RERUN_MAVEN_ARGS[@]}")
  else
    args+=("--fail-fast" "-Dfailsafe.skipAfterFailureCount=1")
  fi

  # When we are ready to collect coverge on smoke tests, add "-Pcoverage" below
  ionice nice ../compile.pl \
    -DskipTests=false \
    "${args[@]}" \
    -DfailIfNoTests=false \
    -Dtest.fork.count=1 \
    --batch-mode \
    -N \
    '-P!smoke.all' \
    "-Psmoke.$SUITE" \
    install
}

run_tests_with_rerun "$RERUN_COUNT" "${CCI_RERUN_MAX_FAILED:-3}" run_tests
