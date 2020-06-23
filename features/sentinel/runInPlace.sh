#!/bin/bash -e
test -d repository || (echo "This command must be ran from the features/sentinel directory" && exit 1)

# Inclue the bundled Maven in the $PATH
MYDIR=$(dirname "$0")
MYDIR=$(cd "$MYDIR"; pwd)
export PATH="$MYDIR/../../bin:$MYDIR/../../maven/bin:$PATH"
export CONTAINERDIR="${MYDIR}/../container/sentinel"

cleanup_and_build() {
  should_use_sudo=$1

  cmd_prefix=""
  if [[ $should_use_sudo -eq 1 ]]; then
    cmd_prefix="sudo "
  fi

  # Kill off any existing instances
  did_kill_at_least_one_pid=0
  for pid_file in $(find "${CONTAINERDIR}/target" -name karaf.pid); do
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
  $cmd_prefix rm -rf "${CONTAINERDIR}"/target/sentinel-karaf-*

  # Rebuild - we've already verified that we're in the right folder
  mvn clean install && \
    (cd "${CONTAINERDIR}"; mvn clean install)
}

set_instance_specific_configuration() {
  SENTINEL_HOME="$1"
  idx=$2
  offset=$((idx - 1))

  # Here's a commented list of ports on which a default instance of Sentinel is listening:
  #$ sudo netstat -lnp | grep 23306
  #### JVM Debug - Used when 'debug' flag in passed to the 'karaf' command, configured via the $JAVA_DEBUG_PORT env. var.
  #tcp        0      0 0.0.0.0:5005            0.0.0.0:*               LISTEN      23306/java
  #### SSH  Set in 'etc/org.apache.karaf.shell.cfg' via sshPort=8201
  #tcp6       0      0 127.0.0.1:8301          :::*                    LISTEN      23306/java
  #### Random RMI port?
  #tcp6       0      0 :::38287                :::*                    LISTEN      23306/java
  #### RMI Registry - Set in 'etc/org.apache.karaf.management.cfg' via rmiRegistryPort=1299 and the serviceUrl
  #tcp6       0      0 127.0.0.1:1399          :::*                    LISTEN      23306/java
  #### Jetty - Set in 'etc/org.ops4j.pax.web.cfg' via :org.osgi.service.http.port=8181
  #tcp6       0      0 :::8181                 :::*                    LISTEN      23306/java
  #### Random port use for Karaf management - Stored in 'data/port'
  #tcp6       0      0 127.0.0.1:34947         :::*                    LISTEN      23306/java
  #### RMI Server - Set in 'etc/org.apache.karaf.management.cfg' via rmiServerPort=45444 and the serviceUrl
  #tcp6       0      0 127.0.0.1:46444         :::*                    LISTEN      23306/java
  #### Trap listener - Set in 'etc/org.opennms.netmgt.trapd.cfg' via trapd.listen.port=1162
  #udp6       0      0 127.0.0.1:1162          :::*                                23306/java
  #### Syslog listener - Set in 'etc/org.opennms.netmgt.syslogd.cfg' via syslog.listen.port=1514
  #udp6       0      0 :::1514                 :::*                                23306/java

  JAVA_DEBUG_PORT=$((5005 + offset))
  JETTY_PORT=$((8181 + offset))
  RMI_REGISTRY_PORT=$((1399 + offset))
  RMI_SERVER_PORT=$((46444 + offset))
  SNMP_TRAP_PORT=$((1162 + offset))
  SSH_PORT=$((8301 + offset))
  SYSLOG_PORT=$((1514 + offset))

  # No need to write this one anywhere, just export it
  export JAVA_DEBUG_PORT

  # Jetty
  #perl -pi -e "s|org.osgi.service.http.port.*|org.osgi.service.http.port = $JETTY_PORT|g" "$SENTINEL_HOME/etc/org.ops4j.pax.web.cfg"
  echo "org.osgi.service.http.port = $JETTY_PORT" > "$SENTINEL_HOME/etc/org.ops4j.pax.web.cfg"

  # RMI
  perl -pi -e "s|rmiRegistryPort.*|rmiRegistryPort = $RMI_REGISTRY_PORT|g" "$SENTINEL_HOME/etc/org.apache.karaf.management.cfg"
  perl -pi -e "s|rmiServerPort.*|rmiServerPort = $RMI_SERVER_PORT|g" "$SENTINEL_HOME/etc/org.apache.karaf.management.cfg"
  perl -pi -e "s|serviceUrl.*|serviceUrl = service:jmx:rmi://127.0.0.1:$RMI_SERVER_PORT/jndi/rmi://127.0.0.1:$RMI_REGISTRY_PORT/karaf-sentinel|g" "$SENTINEL_HOME/etc/org.apache.karaf.management.cfg"

  # SNMP Traps
  echo "trapd.listen.port = $SNMP_TRAP_PORT" > "$SENTINEL_HOME/etc/org.opennms.netmgt.trapd.cfg"

  # SSH
  perl -pi -e "s|sshPort.*|sshPort = $SSH_PORT|g" "$SENTINEL_HOME/etc/org.apache.karaf.shell.cfg"

  # Syslog
  echo "syslog.listen.port = $SYSLOG_PORT" > "$SENTINEL_HOME/etc/org.opennms.netmgt.syslogd.cfg"
}

spawn_sentinel() {
  idx=$1
  detached=$2
  should_use_sudo=$3
  SENTINEL_HOME="${CONTAINERDIR}/target/sentinel-karaf-$idx"

  echo "Extracting container for Sentinel #$idx..."
  # Extract the container
  pushd "${CONTAINERDIR}"/target > /dev/null
  mkdir -p "$SENTINEL_HOME"
  tar zxvf sentinel-*.tar.gz -C "$SENTINEL_HOME" --strip-components 1 > /dev/null
  popd > /dev/null

  # Extract the default repository
  pushd repository/target > /dev/null
  tar zxvf repository-*-repo.tar.gz -C "${SENTINEL_HOME}/system" > /dev/null
  popd > /dev/null

  echo "Updating configuration for Sentinel #$idx..."
  # Instance specific configuration
  set_instance_specific_configuration "$SENTINEL_HOME" "$idx"

  echo "Starting Sentinel #$idx (detached=$detached)..."
  KARAF_ARGS="debug"
  cmd_prefix=""
  pushd "$SENTINEL_HOME" > /dev/null
  if [[ $detached -eq 1 ]]; then
    # shellcheck disable=SC2086
    # shellcheck disable=SC2024
    if [[ $should_use_sudo -eq 1 ]]; then
      cmd_prefix="sudo -E -b "
    fi
    $cmd_prefix nohup ./bin/karaf daemon $KARAF_ARGS &> "$SENTINEL_HOME/output.log" &
  else
    # shellcheck disable=SC2086
    if [[ $should_use_sudo -eq 1 ]]; then
      cmd_prefix="sudo -E "
    fi
    $cmd_prefix ./bin/karaf $KARAF_ARGS
  fi
  popd > /dev/null
}

spawn_sentinels() {
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
    spawn_sentinel "$i" "$instance_detached" "$use_sudo"
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

spawn_sentinels "$NUM_INSTANCES" "$DETACHED" "$SUDO"
