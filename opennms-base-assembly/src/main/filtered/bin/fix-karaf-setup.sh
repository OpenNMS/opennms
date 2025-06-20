#!/usr/bin/env bash

set -e # exit when a command fails

INCOMING_ARGS=("$@")

if [ -z "${OPENNMS_HOME}" ]; then
  # shellcheck disable=SC2154
  OPENNMS_HOME="${install.dir}"
fi

if [ -r "${OPENNMS_HOME}/etc/opennms.conf" ]; then
  # shellcheck disable=SC1090,SC1091
  . "${OPENNMS_HOME}/etc/opennms.conf"
fi
[ -z "${RUNAS}" ] && RUNAS=opennms

DATA_DIR="${OPENNMS_HOME}/data"

RUNUSER="$(command -v runuser 2>/dev/null || which runuser 2>/dev/null || :)"
myuser="$(id -u -n)"
if [ "$myuser" != "$RUNAS" ]; then
  if [ "$myuser" = "root" ] &&  [ -x "$RUNUSER" ]; then
    echo "WARNING: relaunching as $RUNAS" >&2
    _cmd=("$RUNUSER" "-u" "$RUNAS" -- "$0" "${INCOMING_ARGS[@]}");
    exec "${_cmd[@]}"
  fi
  echo "ERROR: you should run this script as ${RUNAS}, not '${myuser}'." >&2
  echo "       To correct this, try 'sudo -u ${RUNAS} $0 $@'" >&2
  echo "       If you wish for OpenNMS to run as ${myuser} instead," >&2
  echo "       create or edit ${OPENNMS_HOME}/etc/opennms.conf and set 'RUNAS=${myuser}'." >&2
  exit 4 # According to LSB: 4 - user had insufficient privileges
fi

ASSUME_YES=0

while test $# -gt 0
do
  case "$1" in
    --yes) ASSUME_YES=1
        ;;
    --help)
      echo "Usage: fix-karaf-setup.sh [--help | --yes ]"
      echo "       --yes  : skips confirmation prompt"
      echo "       --help : displays this help message"
        ;;
    *)
        ;;
  esac
  shift
done

if [ "$ASSUME_YES" -eq 0 ]; then
  echo "This script will try to fix karaf configuration problems by:"
  echo "  - pruning $DATA_DIR directory. This is where the Karaf cache sits."
  echo "  - restore all Karaf related configuration files to a pristine state"
  echo
  echo "You should make a backup of $OPENNMS_HOME/etc before proceeding."
  echo

  # make sure user wants to proceed
  echo -e "Are you ready to continue? (y/n) \c"
  read -n1 -r answer
  echo
  if [ "$answer" != "y" ]
  then
    echo "Ok, goodbye!"
    exit 0
  fi
  echo
fi

# Prune data directory, except for history.txt
echo "Pruning data directory: $DATA_DIR"
find "$DATA_DIR" -mindepth 1 -maxdepth 1 -not -name 'history.txt' -exec rm -r {} \;

# Restore files
PRISTINE_DIR="$OPENNMS_HOME/share/etc-pristine"
if [ -d "$PRISTINE_DIR" ]
then
  ETC_DIR="$OPENNMS_HOME/etc"
  echo "Copying pristine config files to $ETC_DIR"
  cp -p "$PRISTINE_DIR/jmx."*".cfg" "$ETC_DIR"
  cp -p "$PRISTINE_DIR/org.apache."*".cfg" "$ETC_DIR/"
  cp -p "$PRISTINE_DIR/org.ops4j.pax."*".cfg" "$ETC_DIR/"
  cp -p "$PRISTINE_DIR/profile.cfg" "$ETC_DIR/"
else
  echo "WARNING: Directory ${PRISTINE_DIR} does not exist. Skipping config restoration."
fi

echo
echo "Done. Please try restarting OpenNMS."
