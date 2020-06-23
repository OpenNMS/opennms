#!/bin/bash

__onms_is_array() {
	__onms_is_array_name="$1"; shift
	__onms_is_array_declaration="$(declare -p "${__onms_is_array_name}" 2>/dev/null)"
	echo "${__onms_is_array_declaration}" | grep -q '^declare -a'
}

__onms_convert_to_array() {
	__onms_convert_to_array_name="$1"; shift
	__onms_convert_to_array_declaration="$(declare -p "${__onms_convert_to_array_name}" 2>/dev/null)"
	if [ -z "${__onms_convert_to_array_declaration}" ]; then
		# variable is not set
		eval "${__onms_convert_to_array_name}=()"
	elif __onms_is_array "${__onms_convert_to_array_name}"; then
		# variable is an array
		:
	else
		# variable is not an array
		__onms_convert_to_array_existing_value="$(eval echo "\${$__onms_convert_to_array_name}" | sed -e 's,^[[:space:]]*,,' -e 's,[[:space:]]*$,,')"
		unset "${__onms_convert_to_array_name?}"
		eval "${__onms_convert_to_array_name}=()"
		IFS=" " read -r -a "${__onms_convert_to_array_name?}" <<< "${__onms_convert_to_array_existing_value}"
	fi
}

__onms_read_conf() {
	__onms_read_conf_file="$1"; shift
	if [ -z "$TMPDIR" ]; then
		TMPDIR="/tmp/opennms.$$"
		mkdir "$TMPDIR"
	fi
	__onms_read_conf_tmp_file="$(mktemp "$TMPDIR/opennms-conf.XXXX")"
	if [ -f "${__onms_read_conf_file}" ]; then
		cp "${__onms_read_conf_file}" "${__onms_read_conf_tmp_file}"
		for __onms_read_conf_overrideable in "${OVERRIDEABLE_ARRAYS[@]}"; do
			unset "__opennms_conf_override_${__onms_read_conf_overrideable?}"
			sed -i -e "s,${__onms_read_conf_overrideable},__opennms_conf_override_${__onms_read_conf_overrideable},g" "${__onms_read_conf_tmp_file}"
		done
		# shellcheck disable=SC1090
		. "${__onms_read_conf_tmp_file}"
		for __onms_read_conf_overrideable in "${OVERRIDEABLE_ARRAYS[@]}"; do
			__onms_convert_to_array "${__onms_read_conf_overrideable}"
			__onms_convert_to_array "__opennms_conf_override_${__onms_read_conf_overrideable}"
			eval "${__onms_read_conf_overrideable}+=(\"\${__opennms_conf_override_${__onms_read_conf_overrideable}[@]}\")"
		done
		rm "${__onms_read_conf_tmp_file}"
	fi
}

__onms_is_absolute() {
	case "$1" in
		[/]*)
			return 0
			;;
		*)
			;;
	esac
	return 1
}

# convert any path into an absolute path
__onms_get_absolute_path() {
	__abspath_dir="$(dirname "$1")"
	__basename="$(basename "$1")"
	pushd "$__abspath_dir" >/dev/null 2>&1 || exit 1
		echo "${PWD}/${__basename}"
	popd >/dev/null 2>&1 || exit 1
}

__onms_bin_readlink="$(command -v readlink 2>/dev/null)"
__onms_bin_realpath="$(command -v realpath 2>/dev/null)"

# resolve a file name to its real path (following multiple links if necessary)
# WARNING: assumes the existence of `readlink`, use __onms_get_real_path instead
__lib_resolve_symbolic_links() {
	file_to_find="$1"

	if [ -L "$file_to_find" ]; then
		__new_file_name="$("$__onms_bin_readlink" "$file_to_find")"

		if ! __onms_is_absolute "$__new_file_name"; then
			# we got a relative file, make it absolute again
			__prefix="$(dirname "$file_to_find")"
			__new_file_name="${__prefix}/${__new_file_name}"
		fi

		__lib_resolve_symbolic_links "${__new_file_name}"
		return
	fi

	echo "$file_to_find"
}

# resolve a file name to its real path (following multiple links if necessary)
__onms_get_real_path() {
	file_to_find="$1"

	if [ -n "$__onms_bin_realpath" ]; then
		"$__onms_bin_realpath" "$1"
		return
	fi

	file_to_find="$(__onms_get_absolute_path "$1")"
	if [ -n "$__onms_bin_readlink" ]; then
		__lib_resolve_symbolic_links "$file_to_find"
		return
	fi

	echo "$file_to_find"
}

# given a JAVA_HOME, output a major.minor.micro version string for that Java version
__onms_get_java_version_string() {
	home="$1"; shift
	full_version_string="$("${home}"/bin/java -version 2>&1 | grep ' version ')"
	#version_string="$(printf '%s' "${full_version_string}" | sed -e 's,^.* version ,,' -e 's,^"\(.*\)"$,\1,' -e 's,-[A-Za-z]*$,,' -e 's,^1\.,,')"
	version_string="$(printf '%s' "${full_version_string}" | sed -e 's,^.* version ,,' -e 's, LTS$,,' -e 's, [0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]$,,' -e 's,^"\(.*\)"$,\1,' -e 's,-[A-Za-z]*$,,')"
	if (printf '%s' "${version_string}" | grep -Eq '^[0-9\._]+$'); then
		# valid parsed version string, only numbers and periods
		printf '%s\n' "${version_string}"
	else
		(>&2 printf 'WARNING: unsure how to handle Java version output: %s\n' "${full_version_string}")
		printf ''
	fi
}
