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
SOURCEDIR="$ME/.."

PACKAGES="$@"; shift
if [ -z "$PACKAGES" ]; then
	PACKAGES="opennms opennms-plugins"
fi
PACKAGE_NAME=""
for PACK in $PACKAGES; do
	if [ `echo "$PACK" | grep -c -- -` -eq 0 ] && [ -z "$PACKAGE_NAME" ]; then
		echo "Assuming '$PACK' is the 'main' package."
		PACKAGE_NAME="$PACK"
		break;
	fi
done
if [ -z "$PACKAGE_NAME" ]; then
	echo "Unable to determine main package name."
	exit 1
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
	rpm -qi "$PACKAGE_NAME" 2>&1 | grep ' build from the' | sed -e 's,^.*build from the ,,' -e 's, branch.*$,,'
}

get_hash_from_rpm() {
	rpm -qi "$PACKAGE_NAME" 2>&1 | grep -E '(opennms.git.sourceforge.net|github.com)' | sed -e 's,^.*shortlog;h=,,'
}

clean_maven() {
	banner "Cleaning out old Maven files that can conflict or have issues."

	if [ -d "$HOME/.m2/repository" ]; then
		rm -rf "${HOME}"/.m2/repository/org/opennms "${HOME}"/.m2/repository/org/springframework
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
	do_log "/sbin/service postgresql restart"
	/sbin/service postgresql restart

	sleep 5

	do_log "dropdb -U postgres opennms"
	dropdb -U postgres opennms
}

reset_opennms() {
	banner "Resetting OpenNMS Installation"

	do_log "opennms stop"
	/sbin/service "opennms" stop
	ps auxwww | grep opennms_bootstrap | awk '{ print $2 }' | xargs kill -9

	do_log "clean_yum"
	clean_yum || die "Unable to clean up old RPM files."

	do_log "removing existing RPMs"
	rpm -qa --queryformat='%{name}\n' | grep -E "^(opennms|${PACKAGE_NAME}|meridian)" | grep -v -E '^opennms-repo-' | xargs yum -y remove

	do_log "wiping out \$OPENNMS_HOME"
	rm -rf "$OPENNMS_HOME"/* /var/log/opennms /var/opennms

	if [ `ls "$ME"/../../rpms/*.rpm | wc -l` -gt 0 ]; then
		do_log rpm -Uvh --force "$ME"/../../rpms/*.rpm
		rpm -Uvh --force "$ME"/../../rpms/*.rpm
	else
		echo "Unable to locate RPMs for installing!"
		exit 1
	fi
}

build_tests() {
	banner "Compiling Tests"

	pushd "$SOURCEDIR"
		do_log "./compile.pl -Psmoke --projects :smoke-test --also-make clean install"
		./compile.pl -Psmoke --projects :smoke-test --also-make clean install || die "failed to compile smoke tests"
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
	"$OPENNMS_HOME/bin/runjava" -s || die "'runjava -s' failed."

	do_log "install -dis"
	"$OPENNMS_HOME/bin/install" -dis || die "Unable to run OpenNMS install."
}

start_opennms() {
	banner "Starting OpenNMS"

	do_log "find \*.rpmorig -o -name \*.rpmnew"
	find "$OPENNMS_HOME" -type f -name \*.rpmorig -o -name \*.rpmnew

	do_log "opennms restart"
	/sbin/service "opennms" restart
	RETVAL=$?

	if [ $? -gt 0 ]; then
		if [ -x /usr/bin/systemctl ]; then
			/usr/bin/systemctl status "opennms".service
		fi
		die "OpenNMS failed to start."
	fi

#	COUNT=0
#	do_log "Waiting for OpenNMS to start..."
#	while true; do
#		if [ $COUNT -gt 300 ]; then
#			do_log "We've waited 5 minutes and OpenNMS still hasn't started.  Bailing."
#			exit 1
#		fi
#		COUNT=`expr $COUNT + 1`
#		MANAGER_LOG=`find "$OPENNMS_HOME"/logs -name manager.log 2>/dev/nul`
#		if [ -n "$MANAGER_LOG" ] && [ -e "$MANAGER_LOG" ]; then
#			if [ `grep -c "Startup complete" "$MANAGER_LOG"` -gt 0 ]; then
#				do_log "OpenNMS startup complete."
#				break
#			fi
#		fi
#	done
}

clean_firefox() {
	rm -rf "$HOME"/.mozilla
}

run_tests() {
	banner "Running Tests"

	local RETVAL=0

	EXTRA_ARGS=""
	do_log "compile.pl test"
	pushd "$SOURCEDIR/smoke-test"
		../compile.pl -t -Dorg.opennms.smoketest.logLevel=INFO $EXTRA_ARGS test
		RETVAL=$?
	popd

	return $RETVAL
}

stop_opennms() {
	banner "Stopping OpenNMS"

	do_log "opennms kill"
	/etc/init.d/"opennms" kill

	#do_log "yum clean all"
	#yum clean all || :
}


# DO IT!
clean_maven
reset_opennms
reset_database
configure_opennms
start_opennms
clean_firefox

build_tests
run_tests
RETVAL=$?

stop_opennms

exit $RETVAL
