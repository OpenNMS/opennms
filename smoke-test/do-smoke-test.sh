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

do_log() {
	echo "=== $@ ==="
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

	if [ -d "$HOME/.m2/repository" ]; then
		rm -rf "${HOME}"/.m2/repository
	fi
}

clean_yum() {
	banner "Cleaning out old YUM RPMs."

	do_log "yum clean metadata"
	yum clean metadata

	# find RPMs more than a few days old and delete them
	do_log "removing old RPMs"
	find /var/cache/yum -type f -name \*.rpm -mtime +1 -print0 | xargs -0 rm -v -f
}

reset_database() {
	banner "Resetting OpenNMS Database"

	# easy way to make sure no one is holding on to any pg sockets
	do_log "/etc/init.d/postgresql restart"
	/etc/init.d/postgresql restart

	sleep 5

	do_log "dropdb -U postgres opennms"
	dropdb -U postgres opennms
}

reset_opennms() {
	banner "Resetting OpenNMS Installation"

	do_log "opennms stop"
	ps auxwww | grep opennms_bootstrap | awk '{ print $2 }' | xargs kill -9

	do_log "clean_yum"
	clean_yum || die "Unable to clean up old RPM files."

	do_log "removing existing opennms RPMs"
	rpm -qa --queryformat='%{name}\n' | grep -E '^opennms' | grep -v -E '^opennms-repo-' | xargs yum -y remove

	do_log "wiping out \$OPENNMS_HOME"
	rm -rf "$OPENNMS_HOME"/* /var/log/opennms /var/opennms

	if [ `ls "$ME"/../../rpms/*.rpm | wc -l` -gt 0 ]; then
		do_log "rpm -Uvh $ME/../../rpms/*.rpm"
		rpm -Uvh "$ME"/../../rpms/*.rpm
	else
		echo "Unable to locate RPMs for installing!"
		exit 1
	fi
}

get_source() {
	banner "Getting OpenNMS Source"

	do_log "rsync source from $ME to $SOURCEDIR"
	rsync -ar --exclude=target --exclude=smoke-test --delete "$ME"/../  "$SOURCEDIR"/ || die "Unable to create source dir."

	pushd "$SOURCEDIR"
		do_log "cleaning git"
		git clean -fdx || die "Unable to clean source tree."
		git reset --hard HEAD

		# if $MATCH_RPM is set to "yes", then reset the code to the git hash the RPM was built from
		case $MATCH_RPM in
			yes|y)
				do_log "resetting git hash"
				git reset --hard `get_hash_from_rpm` || die "Unable to reset git tree."
				;;
		esac
	popd
}

configure_opennms() {
	banner "Configuring OpenNMS"

	do_log "replacing configuration files"
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

	do_log "runjava -s"
	$OPENNMS_HOME/bin/runjava -s || die "'runjava -s' failed."

	do_log "install -dis"
	$OPENNMS_HOME/bin/install -dis || die "Unable to run OpenNMS install."
}

start_opennms() {
	banner "Starting OpenNMS"

	do_log "opennms start"
	/etc/init.d/opennms start || die "Unable to start OpenNMS."
	# wait a little longer for OSGi to settle down after we know OpenNMS came up
	sleep 20
}

run_tests() {
	banner "Running Tests"

	local RETVAL=0
	rm -rf ~/.m2/repository/org/opennms

	pushd "$SOURCEDIR"
		do_log "bin/bamboo.pl -Psmoke --projects :smoke-test --also-make install"
		bin/bamboo.pl -Psmoke --projects :smoke-test --also-make install
	popd

	do_log "compile.pl test"
	pushd "$SOURCEDIR/smoke-test"
		../compile.pl -t -Denable.snapshots=true -DupdatePolicy=always -Dorg.opennms.smoketest.logLevel=INFO test
		RETVAL=$?
	popd

	return $RETVAL
}

post_clean() {
	rsync -ar "${SOURCEDIR}/smoke-test/target/" target/ || :
	rm -rf "${SOURCEDIR}" || :
	rm -rf "${HOME}"/.m2/repository || :
	rm -rf "${ME}"/../target || :
}

stop_opennms() {
	banner "Stopping OpenNMS"

	do_log "opennms kill"
	/etc/init.d/opennms kill

	#do_log "yum clean all"
	#yum clean all || :
}


# DO IT!
clean_maven
reset_opennms
reset_database
get_source
configure_opennms
start_opennms

run_tests
RETVAL=$?

post_clean
stop_opennms

exit $RETVAL
