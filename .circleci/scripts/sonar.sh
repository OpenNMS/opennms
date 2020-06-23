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

echo "#### Executing Sonar"
cd ~/project
mvn sonar:sonar \
  -Dsonar.projectKey="$SONARCLOUD_PROJECTKEY" \
  -Dsonar.organization="$SONARCLOUD_ORG" \
  -Dsonar.host.url="https://sonarcloud.io" \
  -Dsonar.login="$SONARCLOUD_LOGIN" \
  -Dsonar.branch.name="$CIRCLE_BRANCH" \
  -Dsonar.coverage.jacoco.xmlReportPaths="$(generate_jacoco_report_names)" \
  -Dsonar.junit.reportPaths="$(generate_junit_report_names)"
