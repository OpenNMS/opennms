#!/bin/sh -e

# If ran with 'true' then run a small subset of the tests
MINIMAL=0
if [ "$1" = "true" ]; then
  MINIMAL=1
fi

cd ~/project/smoke-test
if [ $MINIMAL -eq 1 ]; then
  echo "#### Executing minimal set smoke/system tests"
  # Run a set of known tests
  for TEST_CLASS in "MenuHeaderIT" "SinglePortFlowsIT"
  do
    echo "###### Testing: ${TEST_CLASS}"
    mvn -N -DskipTests=false -DskipITs=false -Dit.test=$TEST_CLASS install verify
  done
else
  echo "#### Executing complete suite of smoke/system tests"
  # Iterate through the tests and stop after the first failure
  pyenv local 3.5.2
  for TEST_CLASS in $(python3 ../.circleci/scripts/find-tests.py --use-class-names . | circleci tests split)
  do
    echo "###### Testing: ${TEST_CLASS}"
    mvn -N -DskipTests=false -DskipITs=false -DfailIfNoTests=false -Dit.test=$TEST_CLASS install verify
  done
fi 

