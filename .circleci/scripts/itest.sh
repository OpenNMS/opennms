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
mvn --batch-mode --fail-at-end --legacy-local-repository --offline -Prun-expensive-tasks -Pbuild-bamboo org.opennms.maven.plugins:structure-maven-plugin:1.0:structure

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
	    sudo apt-get -y install debconf-utils && \
	    echo '* libraries/restart-without-asking boolean true' | sudo debconf-set-selections && \
            sudo env DEBIAN_FRONTEND=noninteractive apt-get install -f nsis R-base rrdtool

echo "#### Building Assembly Dependencies"
mvn install -P'!checkstyle' \
           -Pbuild-bamboo \
           -DupdatePolicy=never \
           -Dbuild.skip.tarball=true \
           -Dmaven.test.skip.exec=true \
           -DskipTests=true \
           -DskipITs=true \
           -Dci.instance="${CIRCLE_NODE_INDEX:-0}" \
           -Dnsis.makensis.bin="$(which makensis)" \
           -B \
           "${CCI_FAILURE_OPTION:--fae}" \
           -am \
           -pl "$(< /tmp/this_node_projects paste -s -d, -)"

echo "#### Executing tests"
mvn install -P'!checkstyle' \
           -Pbuild-bamboo \
           -DupdatePolicy=never \
           -Dbuild.skip.tarball=true \
           -DfailIfNoTests=false \
           -DskipITs=false \
           -Dci.instance="${CIRCLE_NODE_INDEX:-0}" \
           -Dci.rerunFailingTestsCount="${CCI_RERUN_FAILTEST:-0}" \
           -Dcode.coverage="${CCI_CODE_COVERAGE:-false}" \
           -Dnsis.makensis.bin="$(which makensis)" \
           -B \
           "${CCI_FAILURE_OPTION:--fae}" \
           -Dorg.opennms.core.test-api.dbCreateThreads=1 \
           -Dorg.opennms.core.test-api.snmp.useMockSnmpStrategy=false \
           -Djava.security.egd=file:/dev/./urandom \
           -Dtest="$(< /tmp/this_node_tests paste -s -d, -)" \
           -Dit.test="$(< /tmp/this_node_it_tests paste -s -d, -)" \
           -pl "$(< /tmp/this_node_projects paste -s -d, -)"

