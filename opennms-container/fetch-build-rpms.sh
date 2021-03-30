#!/usr/bin/env bash

BAMBOO_HOST="https://bamboo.opennms.org"

# We need a url
if [ "${#}" -lt 1 ]; then
    echo ""
    echo "Script to download RPM artifacts from OpenNMS Bamboo system."
    echo "The argument is a Bamboo URL from a specific build. The rpms"
    echo "will be downloaded into an rpms subdirectory."
    echo ""
    echo "If you are downloading Meridian RPMs, you will need to pass"
    echo "the auth parameter as well."
    echo ""
    echo "Example:"
    echo "  url: https://${BAMBOO_HOST}/browse/OPENNMS-ONMS2852-6"
    echo ""
    echo "Usage: $0 <url> [user:pass]"
    exit 1
fi

# PARSE URL
URL=$1
AUTH=$2
BUILD=$(echo "${URL}" | awk -F'/' '{ print $NF }')
PLAN_KEY=$(echo "${BUILD}" | awk -F'-' '{ print $(NF-2) "-" $(NF-1) }')
BUILD_ID=$(echo "${BUILD}" | awk -F'-' '{ print $NF }')

CURL_CMD=(curl -s -L)
if [ -n "$AUTH" ]; then
    CURL_CMD+=(-u "$AUTH")
fi

# Figure out RPM_VERSION
RPM_VERSION=$("${CURL_CMD[@]}" "${BAMBOO_HOST}/artifact/${PLAN_KEY}/shared/build-${BUILD_ID}/RPMs/" | grep -i meridian-core | sed -E 's/(.*>)(meridian-core-)(.*)\.noarch.rpm<\/a>.*/\3/g')

RPMS_MERIDIAN=("meridian-core-${RPM_VERSION}.noarch.rpm"
              "meridian-webapp-jetty-${RPM_VERSION}.noarch.rpm"
              "meridian-webapp-hawtio-${RPM_VERSION}.noarch.rpm")

RPMS_MINION=("meridian-minion-${RPM_VERSION}.noarch.rpm")

RPMS_SENTINEL=("meridian-sentinel-${RPM_VERSION}.noarch.rpm")

# Start Downloading

echo "Downloading rpms to build docker images"
echo "BUILD: ${BUILD}"
echo "PLAN_KEY: ${PLAN_KEY}"
echo "BUILD_ID: ${BUILD_ID}"
echo "RPM_VERSION: ${RPM_VERSION}"
echo "RPMS: ${RPMS[*]}"

# ensure everything is initialized
if [ -z "${BUILD}" ] || [ -z "${PLAN_KEY}" ] || [ -z "${BUILD_ID}" ] || [ -z "${RPM_VERSION}" ]; then
    echo "Something went wrong, not initialized correctly. Bailing.."
    exit 2
fi

pushd meridian/rpms || exit 1
for RPM in ${RPMS_MERIDIAN[*]}; do
    echo "RPM: ${RPM}"
    if [ ! -f "${RPM}" ]; then
        "${CURL_CMD[@]}" -O "${BAMBOO_HOST}/artifact/${PLAN_KEY}/shared/build-${BUILD_ID}/RPMs/${RPM}"
    fi
done
popd || exit 1

pushd minion/rpms || exit 1
for RPM in ${RPMS_MINION[*]}; do
    echo "RPM: ${RPM}"
    if [ ! -f "${RPM}" ]; then
        "${CURL_CMD[@]}" -O "${BAMBOO_HOST}/artifact/${PLAN_KEY}/shared/build-${BUILD_ID}/RPMs/${RPM}"
    fi
done
popd || exit 1

pushd sentinel/rpms || exit 1
for RPM in ${RPMS_SENTINEL[*]}; do
    echo "RPM: ${RPM}"
    if [ ! -f "${RPM}" ]; then
        "${CURL_CMD[@]}" -O "${BAMBOO_HOST}/artifact/${PLAN_KEY}/shared/build-${BUILD_ID}/RPMs/${RPM}"
    fi
done
popd || exit 1

cat <<END

=== ALL DONE ===

Note that if you are trying to just build local versions of these images for
running smoke tests, you don't need to use the "build_container_image.sh"
scripts, you can just go into the individual projects and run
"docker build -t meridian ." and so on...

END
