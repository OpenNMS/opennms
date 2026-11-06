#!/usr/bin/env bash

# Karaf removes data/cache on start when karaf.clean.cache is true. The feature repositories that the
# KAR deployer registered are kept there, but the .kar files it extracted are kept in data/kar. On the
# next start the deployer finds them in data/kar, skips them as "already installed", and their features
# are never installed again. Remove data/kar as well, so the deployer installs them again from the
# deploy folder.
#
# Run this before starting Karaf, never while it is running. When karaf.clean.cache can't be determined,
# nothing is removed.

usage() {
	cat <<END
usage: $0 [-h] <karaf-home>

Removes data/kar under <karaf-home> (or \$KARAF_DATA) when karaf.clean.cache is enabled.

	-h      this help
END
}

# Escapes a string for use in an extended regular expression
escape_regex() {
	printf '%s' "$1" | sed -e 's/[][\.*^$/{}()+?|]/\\&/g'
}

# read_property <file> <key>: prints the value of <key> in the properties file, the last one wins.
# Returns 1 if the key isn't set.
read_property() {
	__key="$(escape_regex "$2")"
	[ -r "$1" ] && grep -Eq "^[[:space:]]*${__key}([[:space:]]*[=:]|[[:space:]]|$)" "$1" || return 1
	sed -n -E -e "s/^[[:space:]]*${__key}([[:space:]]*[=:]|[[:space:]]|$)[[:space:]]*//p" "$1" | tail -n 1 | sed -e 's/[[:space:]]*$//'
}

# properties_files <system.properties>: prints the files Karaf merges, from lowest to highest precedence
properties_files() {
	printf '%s\n' "$1"
	# the keys are meant literally
	# shellcheck disable=SC2016
	for __include in '${includes}' '${optionals}'; do
		# shellcheck disable=SC2046
		for __file in $(read_property "$1" "$__include" | tr -d '"'); do
			case "$__file" in
				/*) printf '%s\n' "$__file" ;;
				*)  printf '%s\n' "$(dirname "$1")/$__file" ;;
			esac
		done
	done
}

# merged_property <system.properties> <key>: prints the value of <key> after merging the included files.
# Returns 1 if the key isn't set.
merged_property() {
	__found=1
	while IFS= read -r __file; do
		if __v="$(read_property "$__file" "$2")"; then
			__merged="$__v"
			__found=0
		fi
	done <<EOF
$(properties_files "$1")
EOF
	[ "$__found" -eq 0 ] && printf '%s' "$__merged"
	return "$__found"
}

# Resolves ${env:NAME} and ${env:NAME:-default}, the only substitutions handled here
resolve_value() {
	# the patterns are meant literally
	# shellcheck disable=SC2016
	case "$1" in
		'${env:'*':-'*'}')
			__name="${1#\$\{env:}"; __name="${__name%%:-*}"
			__default="${1#*:-}"; __default="${__default%\}}"
			# Like Karaf, use the default when the variable is unset or empty
			__env="$(printenv "$__name")"
			if [ -n "$__env" ]; then
				printf '%s' "$__env"
			else
				printf '%s' "$__default"
			fi
			;;
		'${env:'*'}')
			__name="${1#\$\{env:}"; __name="${__name%\}}"
			printenv "$__name"
			;;
		*)
			printf '%s' "$1"
			;;
	esac
}

# Prints the value of karaf.clean.cache the way Karaf resolves it: karaf.override.karaf.clean.cache in the
# properties files, then a -D option, then karaf.clean.cache in the properties files
get_clean_cache() {
	if __value="$(merged_property "$1" karaf.override.karaf.clean.cache)"; then
		resolve_value "$__value"
		return
	fi

	__value=""
	__found=1
	# The order bin/karaf passes them in, the last one wins
	# shellcheck disable=SC2086
	for __opt in ${JAVA_DEBUG_OPTS:-} ${JAVA_NON_DEBUG_OPTS:-} ${JAVA_OPTS:-} ${EXTRA_JAVA_OPTS:-} ${KARAF_SYSTEM_OPTS:-} ${KARAF_OPTS:-}; do
		case "$__opt" in
			-Dkaraf.clean.cache=*) __value="${__opt#-Dkaraf.clean.cache=}"; __found=0 ;;
		esac
	done
	if [ "$__found" -eq 0 ]; then
		printf '%s' "$__value"
		return
	fi

	if __value="$(merged_property "$1" karaf.clean.cache)"; then
		resolve_value "$__value"
	fi
}

if [ "$1" = "-h" ] || [ -z "$1" ]; then
	usage
	[ "$1" = "-h" ]
	exit $?
fi

KARAF_HOME="$1"
KARAF_DATA="${KARAF_DATA:-$KARAF_HOME/data}"
KARAF_ETC="${KARAF_ETC:-$KARAF_HOME/etc}"

CLEAN_CACHE="$(get_clean_cache "$KARAF_ETC/system.properties")"
# Same as Boolean.getBoolean()
if [ "$(printf '%s' "$CLEAN_CACHE" | tr '[:upper:]' '[:lower:]')" != "true" ]; then
	exit 0
fi

if [ -e "$KARAF_DATA/kar" ] || [ -L "$KARAF_DATA/kar" ]; then
	echo "karaf.clean.cache is enabled, removing $KARAF_DATA/kar so that the .kar files in the deploy folder are installed again"
	if ! rm -rf -- "$KARAF_DATA/kar"; then
		echo "WARNING: failed to remove $KARAF_DATA/kar, the .kar files in it will not be installed again" >&2
		exit 1
	fi
fi
exit 0
