#!/bin/sh -e

find_tests()
{
    # Generate surefire & failsafe test list based on current
    # branch and the list of files changed
    # (The format of the output files contains the canonical class names i.e. org.opennms.core.soa.filter.FilterTest)
    pyenv local 3.5.2
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

echo "#### Generate project structure .json"
(cd /tmp && mvn -llr org.apache.maven.plugins:maven-dependency-plugin:3.1.1:get \
      -DremoteRepositories=http://maven.opennms.org/content/groups/opennms.org-release/ \
      -Dartifact=org.opennms.maven.plugins:structure-maven-plugin:1.0)
mvn org.opennms.maven.plugins:structure-maven-plugin:1.0:structure

echo "#### Determining tests to run"
cd ~/project
find_tests
if [ ! -s /tmp/this_node_projects ]; then
  echo "No tests to run."
  exit 0
fi

echo "#### Allowing non-root ICMP"
sudo sysctl net.ipv4.ping_group_range='0 429496729'

echo "#### Setting up Postgres"
cd ~/project
./.circleci/scripts/postgres.sh

echo "#### Installing other dependencies"
# limit the sources we need to update
sudo rm -f /etc/apt/sources.list.d/*
# limit more sources and add mirrors
echo 'deb mirror://mirrors.ubuntu.com/mirrors.txt trusty main restricted universe multiverse
deb http://archive.ubuntu.com/ubuntu/ trusty main restricted
deb-src http://archive.ubuntu.com/ubuntu/ trusty main restricted' | sudo tee /etc/apt/sources.list

# kill other apt-gets first to avoid problems locking /var/lib/apt/lists/lock - see https://discuss.circleci.com/t/could-not-get-lock-var-lib-apt-lists-lock/28337/6
sudo killall -9 apt-get || true && \
            sudo apt-get update && \
            sudo apt-get install -f R-base rrdtool

echo "#### Executing tests"
mvn verify -P'!checkstyle' \
           -DupdatePolicy=never \
           -Dbuild.skip.tarball=true \
           -DfailIfNoTests=false \
           -DskipITs=false \
           -Dci.instance="${CIRCLE_NODE_INDEX:-0}" \
           -Dci.rerunFailingTestsCount="${CCI_RERUN_FAILTEST:-0}" \
           -Dcode.coverage="${CCI_CODE_COVERAGE:-false}" \
           -B \
           "${CCI_FAILURE_OPTION:--fae}" \
           -Dtest="$(< /tmp/this_node_tests paste -s -d, -)" \
           -Dit.test="$(< /tmp/this_node_it_tests paste -s -d, -)" \
           -pl "$(< /tmp/this_node_projects paste -s -d, -)"

