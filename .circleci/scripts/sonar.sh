#!/bin/sh -e

generate_report_names()
{
  REPORT_PREFIX=$1
  REPORT_SUFFIX=$2
  SEQ_END=$(($(find ~/code-coverage/target-*.zip | wc -l) - 1))

  for COUNT in $(seq 0 $SEQ_END); do
    echo "$REPORT_PREFIX$COUNT$REPORT_SUFFIX"
  done | paste -s -d, -
}

generate_jacoco_report_names()
{
  generate_report_names 'target/jacoco-merged-report-' '/jacoco.xml'
}

generate_junit_report_names()
{
  echo "$(generate_report_names 'target/surefire-reports-'),$(generate_report_names 'target/failsafe-reports-')"
}

find_tests()
{
    # Generate surefire & failsafe test list based on current
    # branch and the list of files changed
    # (The format of the output files contains the canonical class names i.e. org.opennms.core.soa.filter.FilterTest)
    SYSTEM_PYTHON="$(python3 --version | sed -e 's,^Python *,,')"
    pyenv local "${SYSTEM_PYTHON}"
    python3 .circleci/scripts/find-tests/find-tests.py generate-test-lists \
      --changes-only="${CCI_CHANGES_ONLY:-true}" \
      --output-unit-test-classes=surefire_classnames \
      --output-integration-test-classes=failsafe_classnames \
      .

    # Now determine the Maven modules related to the tests we need to run
    cat /tmp/*_classnames | python3 .circleci/scripts/find-tests/find-tests.py generate-test-modules \
      --output=/tmp/this_node_projects \
      .
}

dnf -y remove npm nodejs-full-i18n
dnf module reset -y nodejs
dnf module enable -y nodejs:14
dnf module switch-to -y nodejs:14
dnf -y install npm nodejs-full-i18n

find_tests

echo "#### Executing Sonar"
cd ~/project
./compile.pl sonar:sonar \
  -Dsonar.projectKey="$SONARCLOUD_PROJECTKEY" \
  -Dsonar.organization="$SONARCLOUD_ORG" \
  -Dsonar.host.url="https://sonarcloud.io" \
  -Dsonar.login="$SONARCLOUD_LOGIN" \
  -Dsonar.branch.name="$CIRCLE_BRANCH" \
  -Dsonar.coverage.jacoco.xmlReportPaths="$(generate_jacoco_report_names)" \
  -Dsonar.junit.reportPaths="$(generate_junit_report_names)" \
  --also-make \
  --projects "$(< /tmp/this_node_projects paste -s -d, -)"
