#!/bin/bash -e
test -d container || (echo "This command must be ran from the features/minion directory" && exit 1)

# Inclue the bundled Maven in the $PATH
MYDIR=$(dirname "$0")
MYDIR=$(cd "$MYDIR"; pwd)
export PATH="$MYDIR/../../bin:$MYDIR/../../maven/bin:$PATH"

cleanup_and_build() {
  should_use_sudo=$1

  cmd_prefix=""
  if [[ $should_use_sudo -eq 1 ]]; then
    cmd_prefix="sudo "
  fi

  # Kill off any existing instances
  did_kill_at_least_one_pid=0
  for pid_file in $(find container/karaf/target -name karaf.pid); do
    pid=$(cat "$pid_file")
    if [[ ! -z $pid ]]; then
      $cmd_prefix kill -9 "$pid" 2>/dev/null && did_kill_at_least_one_pid=1
    fi
  done

  # If we killed a container, wait a few seconds before cleaning up
  if [[ $did_kill_at_least_one_pid -eq 1 ]]; then
    sleep 2
  fi

  # Delete files owned by root
  $cmd_prefix rm -rf container/karaf/target/minion-karaf-*

  # Rebuild - we've already verified that we're in the right folder
  mvn clean install
}

set_instance_specific_configuration() {
  MINION_HOME="$1"
  idx=$2
  offset=$((idx - 1))

  # Here's a commented list of ports on which a default instance of Minion is listening:
  #$ sudo netstat -lnp | grep 23306
  #### JVM Debug - Used when 'debug' flag in passed to the 'karaf' command, configured via the $JAVA_DEBUG_PORT env. var.
  #tcp        0      0 0.0.0.0:5005            0.0.0.0:*               LISTEN      23306/java
  #### SSH  Set in 'etc/org.apache.karaf.shell.cfg' via sshPort=8201
  #tcp6       0      0 127.0.0.1:8201          :::*                    LISTEN      23306/java
  #### Random RMI port?
  #tcp6       0      0 :::38287                :::*                    LISTEN      23306/java
  #### RMI Registry - Set in 'etc/org.apache.karaf.management.cfg' via rmiRegistryPort=1299 and the serviceUrl
  #tcp6       0      0 127.0.0.1:1299          :::*                    LISTEN      23306/java
  #### Jetty - Set in 'etc/org.ops4j.pax.web.cfg' via :org.osgi.service.http.port=8181
  #tcp6       0      0 :::8181                 :::*                    LISTEN      23306/java
  #### Random port use for Karaf management - Stored in 'data/port'
  #tcp6       0      0 127.0.0.1:34947         :::*                    LISTEN      23306/java
  #### RMI Server - Set in 'etc/org.apache.karaf.management.cfg' via rmiServerPort=45444 and the serviceUrl
  #tcp6       0      0 127.0.0.1:45444         :::*                    LISTEN      23306/java
  #### Trap listener - Set in 'etc/org.opennms.netmgt.trapd.cfg' via trapd.listen.port=1162
  #udp6       0      0 127.0.0.1:1162          :::*                                23306/java
  #### Syslog listener - Set in 'etc/org.opennms.netmgt.syslogd.cfg' via syslog.listen.port=1514
  #udp6       0      0 :::1514                 :::*                                23306/java

  JAVA_DEBUG_PORT=$((5005 + offset))
  JETTY_PORT=$((8181 + offset))
  RMI_REGISTRY_PORT=$((1299 + offset))
  RMI_SERVER_PORT=$((45444 + offset))
  SNMP_TRAP_PORT=$((1162 + offset))
  SSH_PORT=$((8201 + offset))
  SYSLOG_PORT=$((1514 + offset))

  # No need to write this one anywhere, just export it
  export JAVA_DEBUG_PORT

  # Jetty
  #sed -i "s|org.osgi.service.http.port.*|org.osgi.service.http.port = $JETTY_PORT|g" "$MINION_HOME/etc/org.ops4j.pax.web.cfg"
  echo "org.osgi.service.http.port = $JETTY_PORT" > "$MINION_HOME/etc/org.ops4j.pax.web.cfg"

  # RMI
  sed -i "s|rmiRegistryPort.*|rmiRegistryPort = $RMI_REGISTRY_PORT|g" "$MINION_HOME/etc/org.apache.karaf.management.cfg"
  sed -i "s|rmiServerPort.*|rmiServerPort = $RMI_SERVER_PORT|g" "$MINION_HOME/etc/org.apache.karaf.management.cfg"
  sed -i "s|serviceUrl.*|serviceUrl = service:jmx:rmi://127.0.0.1:$RMI_SERVER_PORT/jndi/rmi://127.0.0.1:$RMI_REGISTRY_PORT/karaf-minion|g" "$MINION_HOME/etc/org.apache.karaf.management.cfg"

  # SNMP Traps
  echo "trapd.listen.port = $SNMP_TRAP_PORT" > "$MINION_HOME/etc/org.opennms.netmgt.trapd.cfg"

  # SSH
  sed -i "s|sshPort.*|sshPort = $SSH_PORT|g" "$MINION_HOME/etc/org.apache.karaf.shell.cfg"

  # Syslog
  echo "syslog.listen.port = $SYSLOG_PORT" > "$MINION_HOME/etc/org.opennms.netmgt.syslogd.cfg"

  # Use some fixed ids when the idx <= 3
  MINION_ID="00000000-0000-0000-0000-000000000000"
  case $idx in
    1 ) MINION_ID="00000000-0000-0000-0000-000000ddba11"
        ;;
    2 ) MINION_ID="00000000-0000-0000-0000-000000bad222"
        ;;
    3 ) MINION_ID="00000000-0000-0000-0000-000000d3c0d3"
        ;;
    * ) MINION_ID="test-$idx"
  esac
  echo "id=$MINION_ID" > "$MINION_HOME/etc/org.opennms.minion.controller.cfg"
}

spawn_minion() {
  idx=$1
  detached=$2
  should_use_sudo=$3
  MINION_HOME="$(pwd)/container/karaf/target/minion-karaf-$idx"

  echo "Extracting container for Minion #$idx..."
  # Extract the container
  pushd container/karaf/target > /dev/null
  mkdir -p "$MINION_HOME"
  tar zxvf karaf-*.tar.gz -C "$MINION_HOME" --strip-components 1 > /dev/null
  popd > /dev/null

  # Extract the core repository
  pushd core/repository/target > /dev/null
  mkdir -p "$MINION_HOME/repositories/core"
  tar zxvf core-repository-*-repo.tar.gz -C "$MINION_HOME/repositories/core" > /dev/null
  popd > /dev/null

  # Extract the default repository
  pushd repository/target > /dev/null
  mkdir -p "$MINION_HOME/repositories/default"
  tar zxvf repository-*-repo.tar.gz -C "$MINION_HOME/repositories/default" > /dev/null
  popd > /dev/null

  echo "Updating configuration for Minion #$idx..."
  # Enable Hawtio
  echo 'hawtio-offline' > "$MINION_HOME/etc/featuresBoot.d/hawtio.boot"

  # Instance specific configuration
  set_instance_specific_configuration "$MINION_HOME" "$idx"

  echo "Starting Minion #$idx (detached=$detached)..."
  KARAF_ARGS="debug"
  cmd_prefix=""
  pushd "$MINION_HOME" > /dev/null
  if [[ $detached -eq 1 ]]; then
    # shellcheck disable=SC2086
    # shellcheck disable=SC2024
    if [[ $should_use_sudo -eq 1 ]]; then
      cmd_prefix="sudo -E -b "
    fi
    $cmd_prefix nohup ./bin/karaf daemon $KARAF_ARGS &> "$MINION_HOME/output.log" &
  else
    # shellcheck disable=SC2086
    if [[ $should_use_sudo -eq 1 ]]; then
      cmd_prefix="sudo -E "
    fi
    $cmd_prefix ./bin/karaf $KARAF_ARGS
  fi
  popd > /dev/null
}

spawn_minions() {
  num_instances=$1
  should_detach=$2
  use_sudo=$3

  cleanup_and_build "$use_sudo"
  for ((i=num_instances; i >= 1; i--)); do
    # Only attach the last instance, unless we're detached, then don't attach at all
    instance_detached=1
    if [[ $i -eq 1 ]]; then
      instance_detached=$should_detach
    fi
    spawn_minion "$i" "$instance_detached" "$use_sudo"
  done
}


usage() {
    echo "usage: runInPlace.sh [[[-n num_instances] [-d] [-s]] | [-h]]"
}

NUM_INSTANCES=1
DETACHED=0
SUDO=0

while [ "$1" != "" ]; do
    case $1 in
        -n | --num-instances )  shift
                                NUM_INSTANCES=$1
                                ;;
        -d | --detached )       DETACHED=1
                                ;;
        -s | --sudo )           SUDO=1
                                ;;
        -h | --help )           usage
                                exit
                                ;;
        * )                     usage
                                exit 1
    esac
    shift
done

NUMBER_RE='^[0-9]+$'
if ! [[ $NUM_INSTANCES =~ $NUMBER_RE ]] ; then
  echo "Number of instances is not a number: $NUM_INSTANCES" >&2; exit 1
fi

if [ "$NUM_INSTANCES" -lt 1 ]; then
  echo "Number of instances must be strictly positive: ${NUM_INSTANCES}" >&2; exit 1
fi

spawn_minions "$NUM_INSTANCES" "$DETACHED" "$SUDO"
