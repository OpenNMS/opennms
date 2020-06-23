#!/bin/sh -
set -e # exit when a command fails

OPENNMS_HOME="${install.dir}"
# The user that OpenNMS needs to run as.
RUNAS="root"
DATA_DIR="$OPENNMS_HOME/data"

myuser="$(id -u -n)"
if [ x"$myuser" = x"$RUNAS" ]; then
	true # all is well
else
	echo "Error: you must run this script as $RUNAS, not '$myuser'" >&2
	exit 4	# According to LSB: 4 - user had insufficient privileges
fi

echo "This script will try to fix karaf configuration problems by:"
echo "- pruning $DATA_DIR directory. This is where the Karaf cache sits."
echo "- restore all Karaf related configuration files to a pristine state"
echo "We recommend making a backup of $OPENNMS_HOME/etc before proceeding."

# make sure user wants to proceed
echo "Are you ready to continue? (y/n)"
read -r answer
echo
if [ "$answer" != "y" ]
then
   echo "Ok, goodbye!"
   exit 0
fi

# Prune data directory, except for history.txt
echo "Pruning directory $DATA_DIR"
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
    echo "Directory $PRISTINE_DIR does not exist. Cannot restore config files."
fi

echo "Done. Please try restarting OpenNMS."
