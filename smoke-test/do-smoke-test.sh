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
SOURCEDIR=`cd "$ME"/..; pwd`

PACKAGES="$@"; shift
if [ -z "$PACKAGES" ]; then
	PACKAGES="opennms opennms-plugins"
fi

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

clean_maven() {
	banner "Cleaning out old Maven files"

	# delete things older than a week
	if [ -d "$HOME/.m2/repository" ]; then
		find "${HOME}/.m2/repository" -depth -ctime +7 -type f -print -exec rm {} \; >/dev/null
		find "${HOME}/.m2/repository" -depth -type d -print | while read LINE; do
			rmdir "$LINE" 2>/dev/null || :
		done
		BAD_JARS=`find "${HOME}/.m2/repository" -depth -type f -name \*.jar | xargs file | grep text | cut -d: -f1`
		if [ -n "$BAD_JARS" ]; then
			rm -f $BAD_JARS
		fi
		rm -f "${HOME}/.m2/repository/repository.xml"
	fi
}

clean_yum() {
	banner "Cleaning out old YUM RPMs."

	yum clean metadata
	# find RPMs more than a few days old and delete them
	find /var/cache/yum -type f -name \*.rpm -mtime +1 -print0 | xargs -0 rm -v -f
}

reset_database() {
	banner "Resetting OpenNMS Database"

	dropdb -U postgres opennms
}

reset_opennms() {
	banner "Resetting OpenNMS Installation"

	/etc/init.d/opennms stop

	clean_yum || die "Unable to clean up old RPM files."

	rpm -qa --queryformat='%{name}\n' | grep -E '^opennms' | xargs yum -y remove
	rm -rf "$OPENNMS_HOME"/* /var/log/opennms /var/opennms /etc/yum.repos.d/opennms*
	rpm -Uvh --force http://yum.opennms.org/repofiles/opennms-repo-testing-rhel5.noarch.rpm
	yum -y install $PACKAGES || die "Unable to install the following packages: $PACKAGES"
}

prepare_source() {
	banner "Getting OpenNMS Source"

	pushd "$SOURCEDIR"
		./clean.pl || die "Unable to clean source tree."

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
			if [ -f "$OPENNMS_HOME/$FILE" ]; then
				mv "$OPENNMS_HOME/$FILE" "$OPENNMS_HOME/$FILE.bak"
			fi
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
	pushd "$SOURCEDIR"
		./compile.pl -N -Denable.snapshots=true -DupdatePolicy=always install
	popd
	pushd "$SOURCEDIR/dependencies"
		../compile.pl -Denable.snapshots=true -DupdatePolicy=always install
	popd
	pushd "$SOURCEDIR/core"
		../compile.pl -Denable.snapshots=true -DupdatePolicy=always install
	popd
	pushd "$SOURCEDIR/smoke-test"
		../bin/bamboo.pl -t -Denable.snapshots=true -DupdatePolicy=always test
		RETVAL=$?
	popd
	return $RETVAL
}

stop_opennms() {
	banner "Stopping OpenNMS"

	/etc/init.d/opennms stop
	yum clean all || :
}


# DO IT!
clean_maven
reset_opennms
reset_database
prepare_source
configure_opennms
start_opennms

run_tests
RETVAL=$?

stop_opennms

exit $RETVAL
