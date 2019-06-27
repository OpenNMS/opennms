#!/bin/sh -e

echo "#### Executing Sonar"
cd ~/project
mvn sonar:sonar \
  -Dsonar.projectKey=$SONARCLOUD_PROJECTKEY \
  -Dsonar.organization=$SONARCLOUD_ORG \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.login=$SONARCLOUD_LOGIN \
  -Dsonar.branch.name=$CIRCLE_BRANCH \
  -Dsonar.coverage.jacoco.xmlReportPaths=target/jacoco-surefire-0/jacoco.xml,target/jacoco-surefire-1/jacoco.xml,target/jacoco-surefire-2/jacoco.xml,target/jacoco-surefire-3/jacoco.xml,target/jacoco-surefire-4/jacoco.xml,target/jacoco-surefire-5/jacoco.xml,target/jacoco-surefire-6/jacoco.xml,target/jacoco-surefire-7/jacoco.xml,target/jacoco-surefire-8/jacoco.xml,target/jacoco-surefire-9/jacoco.xml,target/jacoco-surefire-10/jacoco.xml,target/jacoco-surefire-11/jacoco.xml,target/jacoco-failsafe-0/jacoco.xml,target/jacoco-failsafe-1/jacoco.xml,target/jacoco-failsafe-2/jacoco.xml,target/jacoco-failsafe-3/jacoco.xml,target/jacoco-failsafe-4/jacoco.xml,target/jacoco-failsafe-5/jacoco.xml,target/jacoco-failsafe-6/jacoco.xml,target/jacoco-failsafe-7/jacoco.xml,target/jacoco-failsafe-8/jacoco.xml,target/jacoco-failsafe-9/jacoco.xml,target/jacoco-failsafe-10/jacoco.xml,target/jacoco-failsafe-11/jacoco.xml \
  -Dsonar.junit.reportPaths=target/surefire-reports-0,target/surefire-reports-1,target/surefire-reports-2,target/surefire-reports-3,target/surefire-reports-4,target/surefire-reports-5,target/surefire-reports-6,target/surefire-reports-7,target/surefire-reports-8,target/surefire-reports-9,target/surefire-reports-10,target/surefire-reports-11,target/failsafe-reports-0,target/failsafe-reports-1,target/failsafe-reports-2,target/failsafe-reports-3,target/failsafe-reports-4,target/failsafe-reports-5,target/failsafe-reports-6,target/failsafe-reports-7,target/failsafe-reports-8,target/failsafe-reports-9,target/failsafe-reports-10,target/failsafe-reports-11
