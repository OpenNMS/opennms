#!/bin/sh -e

remove_paths()
{
    while read -r data; do
        echo "$data" |\
        sed '/^opennms-tools[/,].*/d' |\
        sed '/^smoke-test-v2[/,].*/d' |\
        sed '/^smoke-test[/,].*/d' |\
        sed '/^opennms-assemblies[/,].*/d' |\
        sed '/^opennms-full-assembly[/,].*/d' |\
        sed '/.*\/remote-poller-18[/,].*/d' |\
        sed '/^org.opennms.features.topology.plugins.ssh[/,].*/d' |\
        sed '/^opennms-install[/,].*/d'
    done
}

find_tests()
{
    # Generate project name list with path
    pyenv local 3.5.2
    python3 .circleci/scripts/find-tests.py . -p |\
        remove_paths > projects_with_path

    # Generate surefire test list
    circleci tests glob ./**/src/test/java/**/*Test*.java |\
        remove_paths |\
        sed -e 's#^.*src/test/java/\(.*\)\.java#\1#' | tr "/" "." > surefire_classnames
    circleci tests glob ./**/src/test/java/**/*Test*.java |\
        remove_paths |\
        sed -e 's#^\(.*\)\/src/test/java/\(.*\)\.java#\1,\2#' |\
        sed -e ':a' -e 's_^\([^,]*\)/_\1\\_;t a' |\
        sed 's#/#.#g;s#\\#/#g' > surefire_classnames_with_path

    # Generate failsafe test list
    circleci tests glob ./**/src/test/java/**/*IT*.java |\
        remove_paths |\
        sed -e 's#^.*src/test/java/\(.*\)\.java#\1#' | tr "/" "." > failsafe_classnames
    circleci tests glob ./**/src/test/java/**/*IT*.java |\
        remove_paths |\
        sed -e 's#^\(.*\)\/src/test/java/\(.*\)\.java#\1,\2#' |\
        sed -e ':a' -e 's_^\([^,]*\)/_\1\\_;t a' |\
        sed 's#/#.#g;s#\\#/#g' > failsafe_classnames_with_path

    # Generate tests for this container
    < surefire_classnames circleci tests split --split-by=timings --timings-type=classname > /tmp/this_node_tests
    < failsafe_classnames circleci tests split --split-by=timings --timings-type=classname > /tmp/this_node_it_tests

    # Match project name with test classname
    cat /tmp/this_node* | while IFS= read -r classname
    do
        grep -h "$classname" ./*_classnames_with_path |\
            cut -d',' -f1
    done | sort | uniq | xargs -I {} grep -h {} projects_with_path |\
        cut -d',' -f2 | sort | uniq > /tmp/this_node_projects
}

echo "#### Allowing non-root ICMP"
sudo sysctl net.ipv4.ping_group_range='0 429496729'

echo "#### Setting up Postgres"
cd ~/project
./.circleci/scripts/postgres.sh

echo "#### Installing other dependencies"

# kill other apt-gets first to avoid problems locking /var/lib/apt/lists/lock - see https://discuss.circleci.com/t/could-not-get-lock-var-lib-apt-lists-lock/28337/6
sudo killall -9 apt-get || true && \
            sudo apt-get update && \
            sudo apt-get install -f R-base rrdtool

echo "#### Executing tests"
cd ~/project
find_tests
echo "Build complete. Verifying..."
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
