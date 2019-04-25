#!/bin/sh -e
#echo "#### Restoring Docker images..."
#for IMAGE in "opennms" "minion" "snmpd" "tomcat"
#do
#  docker image load -i "${ARTIFACT_DIRECTORY}/docker/stests-$IMAGE-docker-image"
#done
#
#echo "#### Pulling OpenNMS images..."
#docker pull opennms/horizon-core-web:24.0.0-rc
#docker pull opennms/minion:24.0.0-rc
#docker pull opennms/sentinel:24.0.0-rc
#
#docker tag opennms/horizon-core-web:24.0.0-rc horizon
#docker tag opennms/minion:24.0.0-rc minion
#docker tag opennms/sentinel:24.0.0-rc sentinel

#echo "#### Building dependencies"
#cd ~/project
#mvn -DupdatePolicy=never -DskipTests=true -DskipITs=true -P'!checkstyle' -Psmoke --projects :smoke-test --also-make install

echo "#### Executing tests"
cd ~/project/smoke-test-v2
# Iterate through the tests instead of running a single command, since I can't find a way to make the later stop
# after the first failure
pyenv local 3.5.2
for TEST_CLASS in $(python3 ../.circleci/scripts/find-tests.py --use-class-names . | circleci tests split)
do
  echo "###### Testing: ${TEST_CLASS}"
  mvn -N -DskipTests=false -DskipITs=false -DfailIfNoTests=false -Dit.test=$TEST_CLASS install verify
done
