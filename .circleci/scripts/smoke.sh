#!/bin/sh -e

# If ran with 'true' then run a small subset of the tests
MINIMAL=0
if [ "$1" = "true" ]; then
  MINIMAL=1
fi

find_tests()
{
    # Generate surefire test list
    circleci tests glob **/src/test/java/**/*Test*.java |\
        sed -e 's#^.*src/test/java/\(.*\)\.java#\1#' | tr "/" "." > surefire_classnames
    cat surefire_classnames | circleci tests split --split-by=timings --timings-type=classname > /tmp/this_node_tests

    # Generate failsafe list
    circleci tests glob **/src/test/java/**/*IT*.java |\
        sed -e 's#^.*src/test/java/\(.*\)\.java#\1#' | tr "/" "." > failsafe_classnames
    cat failsafe_classnames | circleci tests split --split-by=timings --timings-type=classname > /tmp/this_node_it_tests
}

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
  find_tests
  # Iterate through the tests and stop after the first failure
  for TEST_CLASS in $(cat /tmp/this_node_it_tests)
  do
    echo "###### Testing: ${TEST_CLASS}"
    mvn -N -DskipTests=false -DskipITs=false -DfailIfNoTests=false -Dit.test=$TEST_CLASS install verify
  done
fi 

