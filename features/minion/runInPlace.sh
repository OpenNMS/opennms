#!/bin/bash -e
test -d container || (echo "This command must be ran from the features/minion directory" && exit 1)

MYDIR=`dirname $0`
MYDIR=`cd "$MYDIR"; pwd`
export PATH="$MYDIR/../../bin:$MYDIR/../../maven/bin:$PATH"

# Delete files owned by root
sudo rm -rf container/karaf/target/karaf

# Rebuild
mvn clean install

# Extract the container
pushd container/karaf/target
mkdir -p karaf
tar zxvf karaf-*.tar.gz -C karaf --strip-components 1
popd
MINION_HOME="$(pwd)/container/karaf/target/karaf"

# Extract the core repository
pushd core/repository/target
mkdir -p "$MINION_HOME/repositories/core"
tar zxvf core-repository-*-repo.tar.gz -C "$MINION_HOME/repositories/core"
popd

# Extract the default repository
pushd repository/target
mkdir -p "$MINION_HOME/repositories/default"
tar zxvf repository-*-repo.tar.gz -C "$MINION_HOME/repositories/default"
popd

# Enable Hawtio
echo 'hawtio-offline' > "$MINION_HOME/etc/featuresBoot.d/hawtio.boot"

# Start the container as root (currently required for ICMP)
pushd "$MINION_HOME"
sudo ./bin/karaf debug
popd
