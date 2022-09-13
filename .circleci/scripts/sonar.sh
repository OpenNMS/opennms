#!/bin/bash

set -e

FIND_TESTS_DIR="target/find-tests"

generate_jacoco_report_names()
{
  find . -type f '!' -path './.git/*' -name jacoco.xml | paste -s -d, -
}

generate_junit_report_names()
{
  find . -type d '!' -path './.git/*' -name surefire-reports-\* -o -name failsafe-reports-\* | paste -s -d, -
}

find_tests()
{
    perl -pi -e 's,/home/circleci,/root,g' target/structure-graph.json

    # Now determine the Maven modules related to the tests we need to run
    cat "${FIND_TESTS_DIR}"/*_classnames | python3 .circleci/scripts/find-tests/find-tests.py generate-test-modules \
      --output=/tmp/this_node_projects \
      .
}

dnf -y remove npm nodejs-full-i18n
dnf module reset -y nodejs
dnf module enable -y nodejs:14
dnf module switch-to -y nodejs:14
dnf -y install npm nodejs-full-i18n

# shellcheck disable=SC1091
. ./.circleci/scripts/lib.sh

PR_NUM="$(get_pr_num || echo 0)"
REFERENCE_BRANCH="$(get_reference_branch || echo "")"

echo "#### Making sure git is up-to-date"
if [ -n "${REFERENCE_BRANCH}" ]; then
  git fetch origin "${REFERENCE_BRANCH}"
fi

echo "#### Determining Maven Arguments for Sonar"
MAVEN_ARGS="sonar:sonar"

if [ "${PR_NUM}" -gt 0 ]; then
  MAVEN_ARGS="${MAVEN_ARGS} -Dsonar.pullrequest.key=${PR_NUM} -Dsonar.pullrequest.branch=${CIRCLE_BRANCH} -Dsonar.pullrequest.base=${REFERENCE_BRANCH}"
  MAVEN_ARGS="${MAVEN_ARGS} -Dsonar.pullrequest.provider=GitHub -Dsonar.pullrequest.github.repository=OpenNMS/opennms"
else
  MAVEN_ARGS="${MAVEN_ARGS} -Dsonar.branch.name=${CIRCLE_BRANCH}"
  if [ -n "${REFERENCE_BRANCH}" ] && [ "${REFERENCE_BRANCH}" != "${CIRCLE_BRANCH}" ]; then
    MAVEN_ARGS="$MAVEN_ARGS -Dsonar.newCode.referenceBranch=${REFERENCE_BRANCH}"
  fi
fi

find_tests

PROJECT_LIST="$(< /tmp/this_node_projects paste -s -d, -)"

if [ -z "${PROJECT_LIST}" ]; then
  echo "WARNING: no projects found, skipping sonar run"
  exit 0
fi

echo "#### Executing Sonar"
cd ~/project
# shellcheck disable=SC2086
./compile.pl $MAVEN_ARGS \
  -Dsonar.projectKey="$SONARCLOUD_PROJECTKEY" \
  -Dsonar.organization="$SONARCLOUD_ORG" \
  -Dsonar.host.url="https://sonarcloud.io" \
  -Dsonar.login="$SONARCLOUD_LOGIN" \
  -Dsonar.coverage.jacoco.xmlReportPaths="$(generate_jacoco_report_names)" \
  -Dsonar.junit.reportPaths="$(generate_junit_report_names)" \
  -P'!checkstyle' \
  -P'!production' \
  -Pbuild-bamboo \
  -Dbuild.skip.tarball=true \
  -DfailIfNoTests=false \
  -Djava.security.egd=file:/dev/./urandom \
  --batch-mode \
  --also-make \
  --projects="${PROJECT_LIST}"
