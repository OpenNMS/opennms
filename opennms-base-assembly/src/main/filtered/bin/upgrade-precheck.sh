#!/bin/bash

TRANSACTION_TYPE="$1"
VERSION="$2"

[ -n "$DEBUG" ] && echo "upgrading/installing: transaction=${TRANSACTION_TYPE}, version=${VERSION}"

OK=1
LOCKFILE=/var/run/opennms-upgrade.lock
UPGRADE_VERSION="$VERSION"
NOW="$(date '+%s')"
TIMESTAMP="0"

if [ -e "$LOCKFILE" ]; then
	UPGRADE_VERSION="$(head -n 1 $LOCKFILE)"
	TIMESTAMP="$(tail -n 1 $LOCKFILE)"
fi

[ -n "$DEBUG" ] && echo "* upgrade version: ${UPGRADE_VERSION}"
[ -n "$DEBUG" ] && echo "* timestamp: ${TIMESTAMP}"
[ -n "$DEBUG" ] && echo "* now: ${NOW}"
[ -n "$DEBUG" ] && echo ""

# shellcheck disable=SC2154
if [ ! -e "${install.etc.dir}/upgradewhilerunning" ]; then
	if [ -x /bin/systemctl ]; then
		[ -n "$DEBUG" ] && echo "checking systemctl:"
		if /bin/systemctl -q is-active opennms.service; then
			[ -n "$DEBUG" ] && echo "- systemctl found OpenNMS is running"
			OK=0
		else
			[ -n "$DEBUG" ] && echo "- systemctl found OpenNMS is NOT running"
		fi
	fi

	if [ -x /usr/sbin/service ] && [ $OK -eq 1 ]; then
		[ -n "$DEBUG" ] && echo "checking /usr/sbin/service:"
		if /usr/sbin/service opennms status >/dev/null 2>&1; then
			[ -n "$DEBUG" ] && echo "- 'service opennms status' found OpenNMS is running"
			OK=0
		else
			[ -n "$DEBUG" ] && echo "- 'service opennms status' found OpenNMS is NOT running"
		fi
	fi

	# belt and suspenders time
	if [ -e /var/run/opennms.pid ] && [ "$OK" -eq 1 ]; then
		[ -n "$DEBUG" ] && echo "checking opennms.pid:"
		OPENNMS_PID="$(cat /var/run/opennms.pid)"
		MATCH="$(ps -ef | awk '{ print $2 }' | grep -c -E "${OPENNMS_PID}")"
		if [ "$MATCH" -gt 0 ]; then
			[ -n "$DEBUG" ] && echo "- PID file was found (PID=${OPENNMS_PID}), and that OpenNMS PID is still active"
			OK=0
		else
			[ -n "$DEBUG" ] && echo "- PID file was found (PID=${OPENNMS_PID}), but that OpenNMS PID was NOT still active"
		fi
	fi

	if [ "${UPGRADE_VERSION}" = "${VERSION}" ]; then
		[ -n "$DEBUG" ] && echo "checking lockfile threshold:"
		THRESHOLD="$((NOW - 60))"
		if [ "$TIMESTAMP" -ge "$THRESHOLD" ]; then
			[ -n "$DEBUG" ] && echo "- ${VERSION} lockfile was found with timestamp $TIMESTAMP (now=$NOW), OpenNMS was running when upgrade started"
			# another upgrade from the same version, and we've had another failure within the last minute
			OK=0
		elif [ "$TIMESTAMP" -gt 0 ]; then
			[ -n "$DEBUG" ] && echo "- ${VERSION} lockfile was found with timestamp $TIMESTAMP (now=$NOW), which is too old"
		fi
	fi
fi

if [ "$OK" -eq 0 ]; then
	echo "OpenNMS is running! Please stop OpenNMS before doing package changes."

	[ -n "$DEBUG" ] && echo "setting version to $VERSION in lockfile"
	echo "$VERSION" > "$LOCKFILE"

	[ -n "$DEBUG" ] && echo "setting timestamp to $NOW in lockfile"
	echo "$NOW" >> "$LOCKFILE"

	exit 1
else
	rm -f "$LOCKFILE"
fi

if [ "$OPENNMS_PRECHECK_BACK_UP_ETC" = 1 ] && [ -d "${install.share.dir}" ] && [ -d "${install.etc.dir}" ] && [ -e "${install.etc.dir}/service-configuration.xml" ]; then
	rsync -aqr --delete --exclude=\*.dpkg-new "${install.etc.dir}"/ "${install.share.dir}/etc-pre-upgrade/" || :
fi

exit 0
