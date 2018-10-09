#!/bin/sh

JAVA_SEARCH_DIRS="$JAVA_SEARCH_DIRS /usr/lib/jvm /usr/java /System/Library/Java/JavaVirtualMachines /Library/Java/JavaVirtualMachines /Library/Java/Home /opt"
# set DEBUG=true to enable debugging

compare_versions() {
	a="$(printf '%s.0.0.0' "${1}" | sed -e 's,^1\.\([123456789]\),\1.0,' -e 's,_,.,g')"
	b="$(printf '%s.0.0.0' "${2}" | sed -e 's,^1\.\([123456789]\),\1.0,' -e 's,_,.,g')"

	for place in 1 2 3 4; do
		aplace="$(printf '%s' "$a" | cut -d. "-f${place}")"
		bplace="$(printf '%s' "$b" | cut -d. "-f${place}")"

		if [ "$aplace" -eq "$bplace" ]; then
			continue
		elif [ "$aplace" -lt "$bplace" ]; then
			printf '%s' '1'
			return
		else
			printf '%s' '-1'
			return
		fi
	done

	printf '%s' '0'
}

get_java_version_string() {
	home="$1"; shift
	full_version_string="$("${home}"/bin/java -version 2>&1 | grep ' version ')"
	#version_string="$(printf '%s' "${full_version_string}" | sed -e 's,^.* version ,,' -e 's,^"\(.*\)"$,\1,' -e 's,-[A-Za-z]*$,,' -e 's,^1\.,,')"
	version_string="$(printf '%s' "${full_version_string}" | sed -e 's,^.* version ,,' -e 's, [0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]$,,' -e 's,^"\(.*\)"$,\1,' -e 's,-[A-Za-z]*$,,')"
	if (printf '%s' "${version_string}" | grep -Eq '^[0-9\._]+$'); then
		# valid parsed version string, only numbers and periods
		printf '%s\n' "${version_string}"
	else
		(>&2 printf 'WARNING: unsure how to handle Java version output: %s\n' "${full_version_string}")
		printf ''
	fi
}

usage() {
	cat <<END
usage: $0 [-h] [-v] [minimum_jdk_version] [maximum_jdk_version]

	-h   this help
	-v   print the version matched, rather than the JAVA_HOME

This script will print the location of the newest JDK in the range
of minimum_jdk_version (inclusive) to maximum_jdk_version (exclusive).

END
}

main() {
	SHOW_VERSION=0
	PRINT_HELP=0

	while getopts dhv OPT; do
		case "${OPT}" in
			d)
				DEBUG=1
				#shift
				;;
			h)
				PRINT_HELP=1
				#shift
				;;
			v)
				SHOW_VERSION=1
				#shift
				;;
			*)
				;;
		esac
	done
	shift $((OPTIND -1))

	if [ "$PRINT_HELP" -eq 1 ]; then
		usage
		exit 1
	fi

	current_java_home=""
	current_java_version="0"

	min_java_version="$1"
	#min_java_version="$(printf '%s' "${min_java_version}" | sed -e 's,^1\.\([123456789]\),\1,')"
	if [ -z "${min_java_version}" ]; then
		min_java_version=0
	else
		shift
	fi

	max_java_version="$1"
	#max_java_version="$(printf '%s' "${max_java_version}" | sed -e 's,^1\.\([123456789]\),\1,')"
	if [ -z "${max_java_version}" ]; then
		max_java_version=99999
	else
		shift
	fi

	[ -n "$DEBUG" ] && (>&2 printf 'Minimum Java version (inclusive): %s\n' "${min_java_version}")
	[ -n "$DEBUG" ] && (>&2 printf 'Maximum Java version (exclusive): %s\n' "${max_java_version}")

	for dir in $JAVA_SEARCH_DIRS; do
		if [ -d "${dir}" ]; then
			[ -n "$DEBUG" ] && (>&2 printf 'Scanning: %s\n' "${dir}")
			find "$dir" -type f -name java 2>/dev/null | grep -E '/bin/java$' | sort -u > /tmp/$$.javabins
			while read -r javabin; do
				javahome="$(printf '%s' "${javabin}" | sed -e 's,/bin/java$,,')"
				version_string="$(get_java_version_string "$javahome")"
				[ -n "$DEBUG" ] && (>&2 printf '* %s = %s\n' "${javahome}" "${version_string}")

				comparison="$(compare_versions "${min_java_version}" "${version_string}")"
				if [ 1 = "${comparison}" ] || [ 0 = "${comparison}" ]; then
					[ -n "$DEBUG" ] && (>&2 printf '* detected version (%s) is greater than or equal to the minimum version (%s)\n' "${version_string}" "${min_java_version}")
				else
					[ -n "$DEBUG" ] && (>&2 printf '* detected version (%s) is lower than the minimum version (%s) -- skipping\n' "${version_string}" "${min_java_version}")
					continue
				fi

				comparison="$(compare_versions "${version_string}" "${max_java_version}")"
				if [ 1 = "${comparison}" ]; then
					[ -n "$DEBUG" ] && (>&2 printf '* detected version (%s) is lower than the maximum version (%s)\n' "${version_string}" "${max_java_version}")
				else
					[ -n "$DEBUG" ] && (>&2 printf '* detected version (%s) is higher than the maximum version (%s) -- skipping\n' "${version_string}" "${max_java_version}")
					continue
				fi

				comparison="$(compare_versions "${current_java_version}" "${version_string}")"
				if [ 1 = "${comparison}" ]; then
					[ -n "$DEBUG" ] && (>&2 printf '* detected version (%s) is higher than current matching version (%s) -- updating best match\n' "${version_string}" "${current_java_version}")
					current_java_home="${javahome}"
					current_java_version="${version_string}"
				elif [ 0 = "${comparison}" ] && [ -z "${current_java_home}" ]; then
					[ -n "$DEBUG" ] && (>&2 printf '* detected version (%s) matches the current matching version (%s) -- updating best match\n' "${version_string}" "${current_java_version}")
					current_java_home="${javahome}"
					current_java_version="${version_string}"
				fi
			done < /tmp/$$.javabins
		fi
	done

	if [ -z "${current_java_home}" ]; then
		printf 'No match found!\n'
		exit 1
	fi

	[ -n "$DEBUG" ] && (>&2 printf '* Found best JAVA_HOME (%s): %s\n' "${current_java_version}"  "${current_java_home}")
	if [ "$SHOW_VERSION" -eq "1" ]; then
		printf '%s\n' "${current_java_version}"
	else
		printf '%s\n' "${current_java_home}"
	fi
}

main "$@"
