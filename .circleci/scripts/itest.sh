#!/bin/bash

set -e
set -o pipefail

FIND_TESTS_DIR="target/find-tests"

# attempt to work around repository flakiness
retry()
{
	"$@" || "$@"
}

find_tests()
{
    mkdir -p "${FIND_TESTS_DIR}"

    echo "#### Generate project structure .json"
    ./compile.pl -s .circleci/scripts/structure-settings.xml --batch-mode --fail-at-end -Prun-expensive-tasks -Pbuild-bamboo org.opennms.maven.plugins:structure-maven-plugin:1.0:structure

    # Generate surefire & failsafe test list based on current
    # branch and the list of files changed
    # (The format of the output files contains the canonical class names i.e. org.opennms.core.soa.filter.FilterTest)
    python3 .circleci/scripts/find-tests/find-tests.py generate-test-lists \
      --changes-only="${CCI_CHANGES_ONLY:-true}" \
      --output-unit-test-classes="${FIND_TESTS_DIR}/surefire_classnames" \
      --output-integration-test-classes="${FIND_TESTS_DIR}/failsafe_classnames" \
      .

    # Now determine the tests for this particular container based on the parallelism level and the test timings
    < "${FIND_TESTS_DIR}/surefire_classnames" circleci tests split --split-by=timings --timings-type=classname > /tmp/this_node_tests
    < "${FIND_TESTS_DIR}/failsafe_classnames" circleci tests split --split-by=timings --timings-type=classname > /tmp/this_node_it_tests

    # Now determine the Maven modules related to the tests we need to run
    cat /tmp/this_node* | python3 .circleci/scripts/find-tests/find-tests.py generate-test-modules \
      --output=/tmp/this_node_projects \
      .
}

. ./.circleci/scripts/lib.sh
 
REFERENCE_BRANCH="$(get_reference_branch || echo "develop")"

echo "#### Making sure git is up-to-date"
git remote prune origin || :
git fetch origin "${REFERENCE_BRANCH}"

echo "#### Determining tests to run"
perl -pi -e "s,/home/circleci,${HOME},g" target/structure-graph.json
find_tests
if [ ! -s /tmp/this_node_projects ]; then
  echo "No tests to run."
  exit 0
fi

echo "#### Set loopback to 127.0.0.1"
sudo sed -i 's/127.0.1.1/127.0.0.1/g' /etc/hosts

echo "#### Allowing non-root ICMP"
sudo sysctl net.ipv4.ping_group_range='0 429496729'

echo "#### Setting up Postgres"
./.circleci/scripts/postgres.sh

echo "#### Installing other dependencies"
# limit the sources we need to update
sudo rm -f /etc/apt/sources.list.d/*
 
# kill other apt commands first to avoid problems locking /var/lib/apt/lists/lock - see https://discuss.circleci.com/t/could-not-get-lock-var-lib-apt-lists-lock/28337/6
sudo killall -9 apt || true && \
            retry sudo apt update && \
            retry sudo env DEBIAN_FRONTEND=noninteractive apt -y --no-install-recommends install \
                ca-certificates \
                tzdata \
                software-properties-common \
                debconf-utils
 
# install some keys
curl -sSf https://cloud.r-project.org/bin/linux/ubuntu/marutter_pubkey.asc | sudo tee -a /etc/apt/trusted.gpg.d/cran_ubuntu_key.asc
curl -sSf https://debian.opennms.org/OPENNMS-GPG-KEY | sudo tee -a /etc/apt/trusted.gpg.d/opennms_key.asc
 
# limit more sources and add mirrors
echo "deb mirror://mirrors.ubuntu.com/mirrors.txt $(lsb_release -cs) main restricted universe multiverse
deb http://archive.ubuntu.com/ubuntu/ $(lsb_release -cs) main restricted" | sudo tee -a /etc/apt/sources.list
sudo add-apt-repository -y 'deb http://debian.opennms.org stable main'
 
# add the R repository
sudo add-apt-repository -y "deb https://cloud.r-project.org/bin/linux/ubuntu $(lsb_release -cs)-cran40/"

retry sudo apt update && \
            RRDTOOL_VERSION=$(apt-cache show rrdtool | grep Version: | grep -v opennms | awk '{ print $2 }') && \
            echo '* libraries/restart-without-asking boolean true' | sudo debconf-set-selections && \
            retry sudo env DEBIAN_FRONTEND=noninteractive apt -f --no-install-recommends install \
                openjdk-17-jdk-headless \
                r-base \
                "rrdtool=$RRDTOOL_VERSION" \
                jrrd2 \
                jicmp \
                jicmp6 \
            || exit 1

export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export MAVEN_OPTS="$MAVEN_OPTS -Xmx4g -XX:ReservedCodeCacheSize=1g"

# shellcheck disable=SC3045
ulimit -n 65536

MAVEN_ARGS=()

case "${CIRCLE_BRANCH}" in
  "master"*|"release-"*|develop)
    MAVEN_ARGS+=("-Dbuild.type=production")
  ;;
esac

# if node tests does not exist or is empty, skip surefire
if [ ! -s /tmp/this_node_tests ]; then
  MAVEN_ARGS+=("-DskipSurefire=true")
fi
 
# if node ITs does not exist or is empty, skip surefire
if [ ! -s /tmp/this_node_it_tests ]; then
  MAVEN_ARGS+=("-DskipFailsafe=true")
fi

if [ "${CCI_FAILURE_OPTION:--fail-fast}" = "--fail-fast" ]; then
  MAVEN_ARGS+=("-Dfailsafe.skipAfterFailureCount=1")
fi

MAVEN_COMMANDS=("install")

echo "#### Building Assembly Dependencies"
./compile.pl "${MAVEN_ARGS[@]}" \
           -P'!checkstyle' \
           -P'!production' \
           -Pbuild-bamboo \
           -Dbuild.skip.tarball=true \
           -Dmaven.test.skip.exec=true \
           -DskipTests=true \
           -DskipITs=true \
           --batch-mode \
           "${CCI_FAILURE_OPTION:--fail-fast}" \
           --also-make \
           --projects "$(< /tmp/this_node_projects paste -s -d, -)" \
           install

echo "#### Executing tests"
ionice nice ./compile.pl "${MAVEN_ARGS[@]}" \
           -P'!checkstyle' \
           -P'!production' \
           -Pbuild-bamboo \
           -Pcoverage \
           -Dbuild.skip.tarball=true \
           -DfailIfNoTests=false \
           -Dsurefire.failIfNoSpecifiedTests=false \
           -Dfailsafe.failIfNoSpecifiedTests=false \
           -DrunPingTests=false \
           -DskipITs=false \
           --batch-mode \
           "${CCI_FAILURE_OPTION:--fail-fast}" \
           -Dorg.opennms.core.test-api.dbCreateThreads=1 \
           -Dorg.opennms.core.test-api.snmp.useMockSnmpStrategy=false \
           -Dtest="$(< /tmp/this_node_tests paste -s -d, -)" \
           -Dit.test="$(< /tmp/this_node_it_tests paste -s -d, -)" \
           --projects "$(< /tmp/this_node_projects paste -s -d, -)" \
           install
