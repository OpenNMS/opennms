#!/bin/sh -e

PING_USER="$1"

log_error() {
	case "$QUIET" in
		1|[Tt][Rr][Uu][Ee])
			# do nothing in quiet mode
			;;
		*)
			# shellcheck disable=SC2059
			>&2 printf "$@"
			;;
	esac
}

fail() {
	log_error "$@"
	exit 1
}

if [ "$(id -u)" -gt 0 ]; then
	fail 'You must run this script as root!\n'
fi

if [ -z "$PING_USER" ]; then
	fail 'usage: %s <user>\n' "$0"
fi

PING_USER_UID="$(id -u "${PING_USER}")"
PING_USER_GID="$(id -g "${PING_USER}")"

# root has ping permission implicitly
if [ "${PING_USER_UID}" -eq 0 ]; then
	exit 0
fi

# this script only applies to Linux
if [ "$(uname -s)" != "Linux" ]; then
	exit 0
fi

get_group_range() {
	_group_range="$( (sysctl net.ipv4.ping_group_range 2>/dev/null || :) | cut -d= -f2 | sed -e 's,^ *,,' -e 's, *$,,' -e 's,\t, ,g')"
	if [ -n "${_group_range}" ]; then
		echo "${_group_range}"
	else
		# shellcheck disable=SC2016
		fail 'ERROR: `sysctl net.ipv4.ping_group_range` gave no output! Unable to validate non-root ping support.\n'
	fi
}

get_group_start() {
	_group_range="$(get_group_range)"
	_group_range_start="$(echo "${_group_range}" | cut -d' ' -f1)"
	if [ -n "${_group_range_start}" ]; then
		echo "${_group_range_start}"
	else
		fail 'Unable to determine group range start from "%s".\n' "${_group_range}"
	fi
}

get_group_end() {
	_group_range="$(get_group_range)"
	_group_range_end="$(echo "${_group_range}" | cut -d' ' -f2)"
	if [ -n "${_group_range_end}" ]; then
		echo "${_group_range_end}"
	else
		fail 'Unable to determine group range end from "%s".\n' "${_group_range}"
	fi
}

validate_permissions() {
	group_start="$(get_group_start)"
	group_end="$(get_group_end)"
	
	if [ "${group_start}" -lt "${PING_USER_GID}" ] && [ "${PING_USER_GID}" -le "${group_end}" ]; then
		return 0
	fi

	fail '%s is not in the valid ping range (%s-%s)\n' "${PING_USER_GID}" "${group_start}" "${group_end}"
}

configure_permissions() {
	group_start="$(get_group_start)"
	group_end="$(get_group_end)"
	
	if [ "${PING_USER_GID}" -lt "${group_start}" ]; then
		group_start="${PING_USER_GID}"
	fi
	
	if [ "${group_end}" -lt "${PING_USER_GID}" ]; then
		group_end="${PING_USER_GID}"
	fi
	
	if [ "${group_end}" -eq 0 ]; then
		rm -f /etc/sysctl.d/99-opennms-non-root-icmp.conf || :
	else
		install -d -m 755 /etc/sysctl.d
		echo "net.ipv4.ping_group_range=${group_start} ${group_end}" > /etc/sysctl.d/99-opennms-non-root-icmp.conf
	fi
	
	if ! sysctl -w "net.ipv4.ping_group_range=${group_start} ${group_end}" >/tmp/$$.sysctlout 2>&1; then
		log_error "$(cat /tmp/$$.sysctlout)"
	fi
	rm -f /tmp/$$.sysctlout || :
}

configure_permissions
validate_permissions
