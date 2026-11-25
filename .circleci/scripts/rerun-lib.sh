#!/bin/bash
#
# Functions to run failed test classes again in the same job.
# Source this file, then call run_tests_with_rerun.

RERUN_EVIDENCE_DIR="${RERUN_EVIDENCE_DIR:-/tmp/flaky-evidence}"
RERUN_LOG_DIR="${RERUN_LOG_DIR:-/tmp/rerun-logs}"

# Maven arguments for each test pass when reruns are on.
# A test failure does not fail its module, so Maven also builds and tests the modules that depend on it.
# A build error still gives a non-zero exit code.
RERUN_MAVEN_ARGS=("--fail-at-end" "-Dmaven.test.failure.ignore=true")

# Print the report files that contain a failure or an error.
rerun_failed_reports()
{
  find . \( -path "*/target/failsafe-reports/TEST-*.xml" -o -path "*/target/surefire-reports/TEST-*.xml" \) \
    -exec grep -l -E '<testsuite [^>]*(failures|errors)="[1-9]' {} + 2>/dev/null | sort -u || true
}

# Print the class names for the given type of report (surefire or failsafe) as a comma-separated list.
rerun_failed_classes()
{
  local type="$1" reports="$2"
  echo "$reports" | grep "/target/${type}-reports/" | sed -e 's|.*/TEST-||' -e 's|\.xml$||' | sort -u | paste -s -d, - || true
}

# Print the module directories of the given reports as a comma-separated list.
rerun_failed_modules()
{
  echo "$1" | sed -e 's|/target/[^/]*-reports/.*||' | sort -u | paste -s -d, - || true
}

# Surefire does not fail the module on a fork crash or a fork timeout when test failures are ignored.
# The log is the only record of these.
rerun_fork_failed()
{
  grep -q -E 'The forked VM terminated without|There was a timeout (or other error )?in the fork|Crashed tests:' "$1"
}

# Copy the reports and the container logs of the failed classes, then delete the failed reports.
# The next pass writes new reports.
rerun_save_evidence()
{
  local attempt="$1" reports="$2"
  local report class module dest logdir

  echo "$reports" | while IFS= read -r report; do
    [ -n "$report" ] || continue
    class="$(basename "$report" .xml)"
    class="${class#TEST-}"
    module="${report%%/target/*}"
    dest="${RERUN_EVIDENCE_DIR}/attempt-${attempt}/${class}"
    mkdir -p "$dest"

    cp "$report" "$dest/" || true
    cp "$(dirname "$report")/${class}.txt" "$dest/" 2>/dev/null || true
    cp "$(dirname "$report")/${class}-output.txt" "$dest/" 2>/dev/null || true

    # Smoke tests keep the container logs of a failed test in a target/logs directory that has the class name in its name.
    find "${module}/target/logs" -mindepth 1 -maxdepth 1 -type d -name "*${class}*" 2>/dev/null | while IFS= read -r logdir; do
      mkdir -p "$dest/$(basename "$logdir")"
      cp -r "${logdir}/." "$dest/$(basename "$logdir")/" || true
    done || true

    # A container startup failure keeps its logs in target/logs/startup-failures/<name>/.
    # The reports of the class contain that path.
    grep -o -h -E 'startup-failures/[A-Za-z0-9._-]+' "$dest"/*.xml "$dest"/*.txt 2>/dev/null | sort -u | while IFS= read -r logdir; do
      if [ -d "${module}/target/logs/${logdir}" ]; then
        mkdir -p "$dest/startup-failures"
        cp -r "${module}/target/logs/${logdir}" "$dest/startup-failures/" || true
      fi
    done || true

    rm -f "$report"
  done
}

# Print the classes from the comma-separated list that have no report in the given modules.
rerun_missing_reports()
{
  local classes="$1" modules="$2" present class module

  present="$(echo "$modules" | tr ',' '\n' | while IFS= read -r module; do
    find "${module}/target" -path "*-reports/TEST-*.xml" 2>/dev/null || true
  done | sed -e 's|.*/TEST-||' -e 's|\.xml$||' | sort -u)"

  echo "$classes" | tr ',' '\n' | while IFS= read -r class; do
    [ -n "$class" ] || continue
    echo "$present" | grep -q -x -F "$class" || echo "$class"
  done | paste -s -d, -
}

# Stop Maven when more test classes fail than the limit permits.
# This stops a run early when Postgres or Docker is not available.
rerun_watch_failures()
{
  local max_failed="$1" count

  while sleep "${RERUN_WATCH_INTERVAL:-60}"; do
    count="$(rerun_failed_reports | grep -c . || true)"
    if [ "$count" -gt "$max_failed" ]; then
      echo "#### ${count} test classes failed. The limit is ${max_failed}. Stopping Maven."
      pkill -f 'org.codehaus.plexus.classworlds.launcher.Launcher' || true
      return 0
    fi
  done
}

# Run the tests, then run the failed classes again.
#
#   run_tests_with_rerun <max reruns> <max failed classes> <function>
#
# The function gets: <pass number> <unit test classes> <IT classes> <modules>.
# Pass 0 is the full run and gets empty lists. The lists are comma-separated.
# The function must add RERUN_MAVEN_ARGS to the Maven command when the max reruns value is not 0.
#
# The return value is 0 only when Maven has no error, no report contains a failure,
# and each class of a rerun has a new report.
run_tests_with_rerun()
{
  local max_reruns="$1" max_failed="$2" run_fn="$3"
  local pass=0 exit_code log reports units its modules count missing watcher retried=""

  mkdir -p "$RERUN_LOG_DIR"

  while true; do
    log="${RERUN_LOG_DIR}/pass-${pass}.log"
    watcher=""
    if [ "$max_reruns" -gt 0 ] && [ "$pass" -eq 0 ]; then
      rerun_watch_failures "$max_failed" &
      watcher=$!
    fi

    set +e
    "$run_fn" "$pass" "$units" "$its" "$modules" 2>&1 | tee "$log"
    exit_code="${PIPESTATUS[0]}"
    if [ -n "$watcher" ]; then
      pkill -P "$watcher" 2>/dev/null
      kill "$watcher" 2>/dev/null
      wait "$watcher" 2>/dev/null
    fi
    set -e

    if [ "$max_reruns" -eq 0 ]; then
      return "$exit_code"
    fi

    reports="$(rerun_failed_reports)"
    count="$(echo "$reports" | grep -c . || true)"

    if [ "$count" -gt "$max_failed" ]; then
      echo "#### ${count} test classes failed. The limit for a rerun is ${max_failed}, no rerun."
      return 1
    fi
    if [ "$exit_code" -ne 0 ]; then
      echo "#### Maven failed with exit code ${exit_code}. This is not a test failure, no rerun."
      return "$exit_code"
    fi
    if rerun_fork_failed "$log"; then
      echo "#### A test JVM crashed or timed out, no rerun."
      return 1
    fi
    if [ "$pass" -gt 0 ]; then
      missing="$(rerun_missing_reports "$retried" "$modules")"
      if [ -n "$missing" ]; then
        echo "#### The rerun did not run these classes: ${missing}"
        return 1
      fi
    fi

    if [ -z "$reports" ]; then
      if [ -n "$retried" ]; then
        echo "#### Rerun result: PASSED in rerun ${pass}. Flaky tests: ${retried}"
      fi
      return 0
    fi

    units="$(rerun_failed_classes surefire "$reports")"
    its="$(rerun_failed_classes failsafe "$reports")"
    modules="$(rerun_failed_modules "$reports")"
    echo "#### Failed tests (unit: ${units:-none}, IT: ${its:-none})"

    if [ "$pass" -ge "$max_reruns" ]; then
      echo "#### Rerun result: FAILED after ${pass} rerun(s)"
      return 1
    fi

    pass=$((pass + 1))
    retried="${units}${units:+${its:+,}}${its}"
    rerun_save_evidence "$pass" "$reports"
    echo "#### Rerun ${pass} of ${max_reruns} in modules: ${modules}"
  done
}
