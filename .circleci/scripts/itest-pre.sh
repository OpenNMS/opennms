#!/bin/bash

set -e
set -o pipefail

find_tests()
{
    # Generate surefire & failsafe test list based on current
    # branch and the list of files changed
    # (The format of the output files contains the canonical class names i.e. org.opennms.core.soa.filter.FilterTest)
    python3 .circleci/scripts/find-tests/find-tests.py generate-test-lists \
      --changes-only="${CCI_CHANGES_ONLY:-true}" \
      --output-unit-test-classes=surefire_classnames \
      --output-integration-test-classes=failsafe_classnames \
      .

    # Now determine the tests for this particular container based on the parallelism level and the test timings
    < surefire_classnames circleci tests split --split-by=timings --timings-type=classname > /tmp/this_node_tests
    < failsafe_classnames circleci tests split --split-by=timings --timings-type=classname > /tmp/this_node_it_tests

    # Now determine the Maven modules related to the tests we need to run
    cat /tmp/this_node* | python3 .circleci/scripts/find-tests/find-tests.py generate-test-modules \
      --output=/tmp/this_node_projects \
      .
}

echo "#### Making sure git is up-to-date"
git fetch --all >/dev/null 2>&1

echo "#### Determining tests to run"
perl -pi -e "s,/home/circleci,${HOME},g" target/structure-graph.json

find_tests
if [ ! -s /tmp/this_node_projects ]; then
  # no tests to run
  exit 0
fi

exit 1
