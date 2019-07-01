#!/usr/bin/env bash
# Script to show diffs from the etc to a etc-pristine directory

# Error codes
E_ILLEGAL_ARGS=126

usage() {
  echo ""
  echo "This tool helps to identify configuration files which need to mirgrated during"
  echo "updates."
  echo ""
  echo "Usage:"
  echo "  -h  Show help and usage hints."
  echo "  -d  Use quick defaults and use relative script path,"
  echo "      i.e. ../etc with ../share/etc-pristine."
  echo "  -p  Path to an OpenNMS etc pristine folder,"
  echo "      e.g. /opt/opennms/share/etc-pristine"
  echo "  -r  Path to a OpenNMS etc directory which need to be analysed,"
  echo "      e.g. /opt/opennms/etc"
  echo ""
  echo "Examples:"
  echo "  ${0} -d"
  echo "  ${0} -r /opt/opennms/etc -p /opt/opennms/share/etc-pristine"
  echo ""
}

getdiff() {
  local OPENNMS_ETC="${1}"
  local OPENNMS_ETC_PRISTINE="${2}"

  if [[ ! -d ${OPENNMS_ETC} ]]; then
    echo "Directory ${OPENNMS_ETC} does not exist."
    exit ${E_ILLEGAL_ARGS=126}
  fi

  if [[ ! -d ${OPENNMS_ETC_PRISTINE} ]]; then
    echo "Directory ${OPENNMS_ETC_PRISTINE} does not exist."
    exit ${E_ILLEGAL_ARGS=126}
  fi

  echo ""
  echo "Compare running configuration with pristine configuration"
  echo ""
  echo "Running config path : ${OPENNMS_ETC}"
  echo "Pristine config path: ${OPENNMS_ETC_PRISTINE}"
  echo ""
  echo ""
  echo "Configuration files only in /opt/opennms/etc"
  echo "------------------------------------------------------------"
  diff -rq -EBbw "${OPENNMS_ETC}" "${OPENNMS_ETC_PRISTINE}" | grep "Only in .*/etc[/|:]"
  echo ""
  echo "Configuration files modified by the user"
  echo "------------------------------------------------------------"
  diff -rq -EBbw "${OPENNMS_ETC}" "${OPENNMS_ETC_PRISTINE}" | grep "differ"
  echo ""
  echo "Configuration only in /opt/opennms/share/etc-pristine"
  echo "------------------------------------------------------------"
  diff -rq -EBbw "${OPENNMS_ETC}" "${OPENNMS_ETC_PRISTINE}" | grep "Only in .*/etc-pristine[/|:]"
  echo ""
}

# Evaluate arguments for build script.
if [[ "${#}" == 0 ]]; then
  usage
  exit ${E_ILLEGAL_ARGS}
fi

# Evaluate arguments for build script.
while getopts "hdr:p:" OPT; do
  case "${OPT}" in
    h)
      usage
      exit 0
      ;;
    d)
      OPENNMS_ETC_PRISTINE="../share/etc-pristine"
      OPENNMS_ETC="../etc"
      ;;
    p)
      OPENNMS_ETC_PRISTINE="${OPTARG}"
      ;;
    r)
      OPENNMS_ETC="${OPTARG}"
      ;;
    \?)
      echo "Invalid option: ${OPTARG}" 1>&2
      usage
      exit ${E_ILLEGAL_ARGS=126}
      ;;
    :)
      echo "Invalid option: ${OPTARG} requires an argument" 1>&2
      usage
      exit ${E_ILLEGAL_ARGS=126}
      ;;
    *)
      usage
      exit 0
      ;;
  esac
done

shift $((OPTIND -1))

# Check if there are remaining arguments
if [[ "${#}" -gt 0 ]]; then
  echo "Error: To many arguments: ${*}."
  usage
  exit ${E_ILLEGAL_ARGS}
fi

getdiff "${OPENNMS_ETC}" "${OPENNMS_ETC_PRISTINE}"
