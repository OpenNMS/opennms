#!/bin/bash
set -e
set -o pipefail

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

    # Split by count, not by timings: the JUnit testcase times CircleCI records leave out
    # @ClassRule setup (starting the OpenNMS/Minion/Sentinel stack), which is most of the
    # cost of a smoke test class. Timing-based splits therefore treat several 3-4 minute
    # classes as nearly free and pile them onto one node.
    circleci tests split < failsafe_classnames > /tmp/this_node_it_tests
}

# Prime Docker with the images this suite needs, in parallel, since testcontainers
# downloads them serially. This is only a warm-up: any image not listed here is still
# pulled on demand by testcontainers, so only list images that most tests in the suite
# use. Tags must match what the tests request, or the pull is wasted:
#   - ryuk: bundled with testcontainers (testcontainers.version in the root pom)
#   - firefox: OpenNMSImageNameSubstitutor rewrites selenium/standalone-firefox:4.x to
#     seleniarm/standalone-firefox:${seleniarm.version} on every arch, so read that tag
#     from smoke-test/pom.xml rather than pulling the selenium/* image nobody uses
#   - vnc-recorder: started next to every BrowserWebDriverContainer (testcontainers.version)
#   - the rest: hard-coded in smoke-test/src/main/java/org/opennms/smoketest/containers/
SMOKE_POM="$(cd "$(dirname "$0")/../.." && pwd)/smoke-test/pom.xml"
SELENIARM_VERSION="$(sed -n 's:.*<seleniarm.version>\(.*\)</seleniarm.version>.*:\1:p' "$SMOKE_POM" | head -n 1)"
IMAGE_RYUK="testcontainers/ryuk:0.6.0"
IMAGE_POSTGRES="postgres:14.22-alpine"
IMAGE_FIREFOX="${SELENIARM_VERSION:+seleniarm/standalone-firefox:${SELENIARM_VERSION}}"
IMAGE_VNC_RECORDER="testcontainers/vnc-recorder:1.3.0"
IMAGE_KAFKA="confluentinc/cp-kafka:7.2.0"
IMAGE_CASSANDRA="cassandra:3"
IMAGE_ELASTICSEARCH="docker.elastic.co/elasticsearch/elasticsearch:7.17.9"

if [ -z "$IMAGE_FIREFOX" ]; then
  echo "WARNING: could not read seleniarm.version from $SMOKE_POM; not pre-pulling the browser image"
fi

PRIME_IMAGES=("$IMAGE_RYUK" "$IMAGE_POSTGRES")
case "$SUITE" in
  core)
    PRIME_IMAGES+=(${IMAGE_FIREFOX:+"$IMAGE_FIREFOX"} "$IMAGE_VNC_RECORDER")
    ;;
  minion)
    PRIME_IMAGES+=("$IMAGE_KAFKA")
    ;;
  sentinel)
    PRIME_IMAGES+=("$IMAGE_KAFKA" "$IMAGE_CASSANDRA" "$IMAGE_ELASTICSEARCH")
    ;;
  *)
    # minimal, flaky, and anything else: mixed suites, prime everything
    PRIME_IMAGES+=(${IMAGE_FIREFOX:+"$IMAGE_FIREFOX"} "$IMAGE_VNC_RECORDER" "$IMAGE_KAFKA" "$IMAGE_CASSANDRA" "$IMAGE_ELASTICSEARCH")
    ;;
esac

# Start the pulls in the background so they overlap with the JDK install below
echo "#### Priming Docker container cache for suite '$SUITE': ${PRIME_IMAGES[*]}"
PULL_PIDS=()
for CONTAINER in "${PRIME_IMAGES[@]}"; do
  (docker pull --quiet "$CONTAINER" || echo "WARNING: failed to pre-pull $CONTAINER; testcontainers will retry on demand") &
  PULL_PIDS+=($!)
done

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

wait "${PULL_PIDS[@]}"
echo "#### All docker containers have now been pulled to the local cache"

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

    # Preserve failing test evidence as flaky evidence before overwriting with retry results.
    # The XML alone is often not enough to diagnose testcontainers-based IT failures (e.g. a
    # ContainerLaunchException just shows up as errors="1"), so also grab the plain-text
    # failsafe reports (captured stdout/stderr) and the per-container logs/thread dumps under
    # target/logs/<test>/<container>/ that DevDebugUtils.clearLogs() will otherwise wipe as
    # soon as the retry's teardown runs.
    FLAKY_EVIDENCE_DIR="/tmp/flaky-evidence/attempt-${ATTEMPT}"
    mkdir -p "${FLAKY_EVIDENCE_DIR}"
    set +e +o pipefail

    # Group all evidence for a given failing test class under its own directory, so
    # the XML/text reports and the container logs for that test sit together instead
    # of being split between a flat root and a path mirroring the Maven build tree.
    echo "$FAILED_TESTS" | while IFS= read -r TEST_CLASS; do
      [ -z "$TEST_CLASS" ] && continue

      TEST_EVIDENCE_DIR="${FLAKY_EVIDENCE_DIR}/${TEST_CLASS}"
      mkdir -p "$TEST_EVIDENCE_DIR"

      find . \( -path "*/failsafe-reports/TEST-${TEST_CLASS}.xml" -o -path "*/surefire-reports/TEST-${TEST_CLASS}.xml" \) \
        -exec grep -l -E 'failures="[1-9]|errors="[1-9]' {} + 2>/dev/null \
        | xargs -I{} cp {} "$TEST_EVIDENCE_DIR/"

      find . \( -path "*/surefire-reports/${TEST_CLASS}.txt" -o -path "*/failsafe-reports/${TEST_CLASS}.txt" \) \
        | xargs -I{} cp {} "$TEST_EVIDENCE_DIR/"

      # A class can have more than one failing method, and each gets its own
      # target/logs/<class>-<method>/ directory, so nest by that method-level name
      # (rather than flattening containers directly under the class) to avoid two
      # methods' same-named container folders (e.g. "opennms") overwriting each other.
      find . -type d -path "*/target/logs/${TEST_CLASS}-*" | while IFS= read -r LOGDIR; do
        [ "$(basename "$(dirname "$LOGDIR")")" = "logs" ] || continue
        DEST="$TEST_EVIDENCE_DIR/$(basename "$LOGDIR")"
        mkdir -p "$DEST"
        cp -r "${LOGDIR}/." "$DEST/"
      done

      # Minion/Sentinel startup failures are captured under target/logs/startup-failures/<alias>-<id>/,
      # which carries no test name. The capture logs that path, and the class's report files hold
      # its stdout, so copy only the startup-failure dirs this class's own output refers to.
      find "$TEST_EVIDENCE_DIR" -maxdepth 1 -type f \( -name '*.xml' -o -name '*.txt' \) \
        -exec grep -ohE 'startup-failures/[A-Za-z0-9._-]+' {} + 2>/dev/null \
        | sort -u | while IFS= read -r SFREL; do
          find . -type d -path "*/target/logs/${SFREL}" | while IFS= read -r SFDIR; do
            mkdir -p "$TEST_EVIDENCE_DIR/startup-failures"
            cp -r "$SFDIR" "$TEST_EVIDENCE_DIR/startup-failures/"
          done
        done
    done

    # Now delete originals so fresh results are written by the retry
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
