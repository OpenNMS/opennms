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
