#!/bin/sh -e
echo "#### Allowing non-root ICMP"
sudo sysctl net.ipv4.ping_group_range='0 429496729'

echo "#### Setting up Postgres"
cd ~/project
./.circleci/scripts/postgres.sh

echo "#### Installing other dependencies"
sudo apt update
sudo apt -y install R-base rrdtool

echo "#### Executing tests"
cd ~/project
pyenv local 3.5.2
MODULES_TO_TEST=$(python3 .circleci/scripts/find-tests.py . | circleci tests split | paste -s -d, -)
#echo "Building modules and dependencies for: ${MODULES_TO_TEST}"
#mvn -P'!checkstyle' -P'!enable.tarball' -DupdatePolicy=never -Dbuild.skip.tarball=true -DskipTests=true -am -pl ${MODULES_TO_TEST} install
echo "Build complete. Verifying..."
mvn -P'!checkstyle' -DupdatePolicy=never -DskipITs=false -DskipTests=false --batch-mode -pl ${MODULES_TO_TEST} verify

