#!/bin/bash

set -e
set -o pipefail

FIND_TESTS_DIR="target/find-tests"

filter_exists()
{
  while read -r CHECK; do
    [ -e "${CHECK}" ] && echo "${CHECK}"
  done
}

generate_jacoco_report_files()
{
  find . -type f '!' -path './.git/*' -name jacoco.xml | sort -u
}

generate_junit_report_folders()
{
  find . -type d '!' -path './.git/*' -a \( -name surefire-reports\* -o -name failsafe-reports\* \) | sort -u
}

generate_class_folders()
{
  generate_junit_report_folders \
    | sed -e 's,/\(surefire-reports\|failsafe-reports\).*$,,' \
    | while read -r DIR; do
      find "$DIR" -maxdepth 1 -type d -name classes
    done \
    | sort -u \
    | filter_exists
}

generate_test_class_folders()
{
  generate_junit_report_folders \
    | sed -e 's,/\(surefire-reports\|failsafe-reports\).*$,,' \
    | while read -r DIR; do
      find "$DIR" -maxdepth 1 -type d -name test-classes
    done \
    | sort -u \
    | filter_exists
}

generate_source_folders()
{
  find . -type d '!' -path './.git/*' -name target \
    | sed -e 's,/target,/src,' \
    | while read -r DIR; do
      echo "${DIR}/main"
      echo "${DIR}/assembly"
    done \
    | sort -u \
    | filter_exists
}

generate_test_folders()
{
  find . -type d '!' -path './.git/*' -name target \
    | sed -e 's,/target,/src,' \
    | while read -r DIR; do
      echo "${DIR}/test"
    done \
    | sort -u \
    | filter_exists
}

find_tests()
{
    perl -pi -e "s,/(root|home/circleci)/project/,${HOME}/project/,g" target/structure-graph.json

    # Now determine the Maven modules related to the tests we need to run
    cat "${FIND_TESTS_DIR}"/*_classnames | python3 .circleci/scripts/find-tests/find-tests.py generate-test-modules \
      --output=/tmp/this_node_projects \
      .
}

# shellcheck disable=SC1091
. ./.circleci/scripts/lib.sh

PR_NUM="$(get_pr_num || echo 0)"
REFERENCE_BRANCH="$(get_reference_branch || echo "develop")"

echo "#### Making sure git is up-to-date"
if [ -n "${REFERENCE_BRANCH}" ]; then
  git remote prune origin || :
  git fetch origin "${REFERENCE_BRANCH}"
fi

echo "#### Enumerating Affected Tests and Projects"
find_tests
PROJECT_LIST="$(< /tmp/this_node_projects paste -s -d, -)"
if [ -z "${PROJECT_LIST}" ]; then
  echo "WARNING: no projects found, skipping sonar run"
  exit 0
fi

echo "#### Unpacking Sonar CLI"
unzip -o -q -d /tmp /tmp/sonar-scanner-cli.zip
SONAR_DIR="$(find /tmp/sonar* -type d -name sonar-scanner\*)"

echo "#### Determining Arguments for Sonar CLI"
declare -a SONAR_ARGS=(
  -Dsonar.login="$SONARCLOUD_LOGIN"
  -Dsonar.host.url="https://sonarcloud.io"
)

if [ "${PR_NUM}" -gt 0 ]; then
  SONAR_ARGS=("${SONAR_ARGS[@]}" "-Dsonar.pullrequest.key=${PR_NUM}" "-Dsonar.pullrequest.branch=${CIRCLE_BRANCH}" "-Dsonar.pullrequest.base=${REFERENCE_BRANCH}")
  SONAR_ARGS=("${SONAR_ARGS[@]}" "-Dsonar.pullrequest.provider=GitHub" "-Dsonar.pullrequest.github.repository=${CIRCLE_PROJECT_USERNAME}/${CIRCLE_PROJECT_REPONAME}")
else
  SONAR_ARGS=("${SONAR_ARGS[@]}" "-Dsonar.branch.name=${CIRCLE_BRANCH}")
  if [ -n "${REFERENCE_BRANCH}" ] && [ "${REFERENCE_BRANCH}" != "${CIRCLE_BRANCH}" ]; then
    SONAR_ARGS=("${SONAR_ARGS[@]}" "-Dsonar.newCode.referenceBranch=${REFERENCE_BRANCH}")
  fi
fi

mkdir -p /tmp/sonar-cache
export SONAR_USER_HOME=/tmp/sonar-cache
export SONAR_SCANNER_OPTS="${MAVEN_OPTS:=-Xmx7g} -verbose:gc"

echo "#### Executing Sonar"
# shellcheck disable=SC2086
"${SONAR_DIR}/bin/sonar-scanner" \
  "${SONAR_ARGS[@]}" \
  -Djava.security.egd=file:/dev/./urandom \
  -Dsonar.coverage.jacoco.xmlReportPaths="$(generate_jacoco_report_files | paste -s -d, -)" \
  -Dsonar.junit.reportPaths="$(generate_junit_report_folders | paste -s -d, -)" \
  -Dsonar.sources="$(generate_source_folders | paste -s -d, -)" \
  -Dsonar.tests="$(generate_test_folders | paste -s -d, -)" \
  -Dsonar.java.binaries="$(generate_class_folders | paste -s -d, -)" \
  -Dsonar.java.libraries="${HOME}/.m2/repository/**/*.jar,**/*.jar" \
  -Dsonar.java.test.binaries="$(generate_test_class_folders | paste -s -d, -)" \
  -Dsonar.java.test.libraries="${HOME}/.m2/repository/**/*.jar,**/*.jar"
