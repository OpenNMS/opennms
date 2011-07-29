#!/bin/bash

if [ `id -un` != "root" ]; then
	echo "You must be root to run this!"
	exit 1
fi

DIRNAME=`dirname $0`
ME=`cd $DIRNAME; pwd`

if [ -z "$MATCH_RPM" ]; then
	MATCH_RPM=no
fi
OPENNMS_HOME=/opt/opennms
SOURCEDIR="$ME/opennms-source"

die() {
	echo "exiting: $@"

	for file in manager.log output.log; do
		if [ -f "$OPENNMS_HOME/logs/daemon/$file" ]; then
			echo "=== contents of $file ==="
			cat "$OPENNMS_HOME/logs/daemon/$file"
		else
			echo "=== no $file file found ==="
		fi
	done
	exit 1
}

banner() {
	echo "=============================================================================="
	echo "$@"
	echo "=============================================================================="
}

get_branch_from_git() {
	git branch | grep -E '^\*' | awk '{ print $2 }'
}

get_branch_from_rpm() {
	rpm -qi opennms 2>&1 | grep 'This is an OpenNMS build from the' | sed -e 's,^.*build from the ,,' -e 's, branch.*$,,'
}

get_hash_from_rpm() {
	rpm -qi opennms 2>&1 | grep http://opennms.git.sourceforge.net | sed -e 's,^.*shortlog;h=,,'
}

reset_database() {
	banner "Resetting OpenNMS Database"

	dropdb -U postgres opennms
}

reset_opennms() {
	banner "Resetting OpenNMS Installation"

	/etc/init.d/opennms stop

	yum -y remove opennms-core
	rm -rf "$OPENNMS_HOME"/* /var/log/opennms /var/opennms
	yum -y install opennms || die "Unable to install OpenNMS."
}

get_source() {
	banner "Getting OpenNMS Source"

	if [ ! -d "$SOURCEDIR" ]; then
		git clone git://opennms.git.sourceforge.net/gitroot/opennms/opennms "$SOURCEDIR" || die "Unable to clone from git."
	fi
	pushd "$SOURCEDIR"
		CURRENT_BRANCH=`get_branch_from_git`
		RPM_BRANCH=`get_branch_from_rpm`

		if [ "$RPM_BRANCH" != "$CURRENT_BRANCH" ]; then
			git branch -t "$RPM_BRANCH" origin/"$RPM_BRANCH"
			git checkout "$RPM_BRANCH" || die "Unable to check out $RPM_BRANCH branch."
		fi
		git clean -fdx || die "Unable to clean source tree."
		git reset --hard HEAD
		git pull || die "Unable to pull latest code."

		# if $MATCH_RPM is set to "yes", then reset the code to the git hash the RPM was built from
		case $MATCH_RPM in
			yes|y)
				git reset --hard `get_hash_from_rpm` || die "Unable to reset git tree."
				;;
		esac
	popd
}

configure_opennms() {
	banner "Configuring OpenNMS"

	pushd opennms-home
		find * -type f | sort -u | while read FILE; do
			dir=`dirname "$FILE"`
			mkdir -p "$dir"
			mv "$OPENNMS_HOME/$FILE" "$OPENNMS_HOME/$FILE.bak"
			install -c "$FILE" "$OPENNMS_HOME/$FILE"
		done
	popd

	/opt/opennms/bin/runjava -s || die "'runjava -s' failed."
	/opt/opennms/bin/install -dis || die "Unable to run OpenNMS install."
}

start_opennms() {
	banner "Starting OpenNMS"

	/etc/init.d/opennms start || die "Unable to start OpenNMS."
}

run_tests() {
	banner "Running Tests"

	local RETVAL=0
	rm -rf ~/.m2/repository/org/opennms
	pushd "$SOURCEDIR/smoke-test"
		../maven/bin/mvn -Denable.snapshots=true test
		RETVAL=$?
	popd
	return $RETVAL
}

stop_opennms() {
	banner "Stopping OpenNMS"

	/etc/init.d/opennms stop
}

# DO IT!
reset_opennms
reset_database
get_source
configure_opennms
start_opennms

run_tests
RETVAL=$?

stop_opennms

exit $RETVAL
