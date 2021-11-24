#!/bin/bash

# This script will return the code from the python generator
# 0 indicates all files have been mapped
# 1 indicates there are files missing component info

RELDIR="$1"
TOPDIR=$(cd $(RELDIR); pwd -P)
# Make sure at least one Maven is in the path
PATH="$PATH:$TOPDIR/maven/bin"

set -e
set -o pipefail
JUNIT_PLUGIN_VERSION="$(grep '<maven.testing.plugin.version>' "${TOPDIR}/pom.xml" | sed -e 's,<[^>]*>,,g' -e 's, *,,g')"

# Ensure the plugin is available
# We swap to another directory so that we don't need to spend time parsing our current pom
(cd /tmp && mvn -llr org.apache.maven.plugins:maven-dependency-plugin:3.1.1:get \
      -DremoteRepositories=http://maven.opennms.org/content/groups/opennms.org-release/ \
      -Dartifact=org.opennms.maven.plugins:structure-maven-plugin:1.0 \
      && mvn org.apache.maven.plugins:maven-dependency-plugin::get \
        -DartifactId=surefire-junit4 -DgroupId=org.apache.maven.surefire -Dversion="$JUNIT_PLUGIN_VERSION")

# Now execute the plugin if the structure has not been generated yet
STRUCTURE_GRAPH_JSON="target/structure-graph.json"
cd $TOPDIR
if [ ! -e $STRUCTURE_GRAPH_JSON ]; then
  mvn org.opennms.maven.plugins:structure-maven-plugin:1.0:structure
else
  echo "Found existing Maven project structure map. Skipping generation."
  echo "(If you have modified the Maven project modules or structure in some way since then delete $STRUCTURE_GRAPH_JSON and run the script again.)"
fi

python3 opennms20/componentmapper/find-components.py generate-components .
exit $?

