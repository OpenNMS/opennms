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

SUDO="$(command -v sudo 2>/dev/null || which sudo 2>/dev/null || :)"
myuser="$(id -u -n)"
if [ "$myuser" != "$RUNAS" ]; then
  if [ "$myuser" = "root" ] && [ -n "$SUDO" ] && [ -x "$SUDO" ]; then
    echo "WARNING: relaunching as $RUNAS" >&2
    _cmd=("$SUDO" "-u" "$RUNAS" "$0" "${INCOMING_ARGS[@]}");
    exec "${_cmd[@]}"
  fi
  echo "ERROR: you must run this script as ${RUNAS}, not '${myuser}'." >&2
  echo "       Create or edit ${OPENNMS_HOME}/etc/opennms.conf and set 'RUNAS=${myuser}'" >&2
  echo "       if you wish for OpenNMS to run as ${myuser} instead." >&2
  exit 4 # According to LSB: 4 - user had insufficient privileges
fi

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
