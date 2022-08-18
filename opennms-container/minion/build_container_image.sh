#!/usr/bin/env bash

# Exit immediately if anything returns non-zero
set -e

# Exit script if a statement returns a non-true return value.
set -o errexit

# Use the error status of the first failure, rather than that of the last item in a pipeline.
set -o pipefail

MYDIR="$(cd "$(dirname "$0")"; pwd)";
cd "${MYDIR}"

# shellcheck disable=SC1091
source ../set-build-environment.sh

TARBALL="$(find ../../opennms-assemblies/minion -name \*-minion.tar.gz -type f | head -n 1)"
if [ -z "${TARBALL}" ]; then
  echo "unable to find minion tarball in opennms-assemblies"
  exit 1
fi
rm -rf "${MYDIR}/tarball-root"
mkdir -p "${MYDIR}/tarball-root"
tar -x -z --strip-components 1 \
        -C "${MYDIR}/tarball-root" \
        -f "${TARBALL}"

sed -e "s,@VERSION@,${VERSION}," \
  -e "s,@REVISION@,${REVISION}," \
  -e "s,@BRANCH@,${BRANCH}," \
  -e "s,@BUILD_NUMBER@,${BUILD_NUMBER}," \
  minion-config-schema.yml.in > minion-config-schema.yml

docker build -t minion \
  --build-arg BUILD_DATE="${BUILD_DATE}" \
  --build-arg VERSION="${VERSION}" \
  --build-arg SOURCE="${SOURCE}" \
  --build-arg REVISION="${REVISION}" \
  --build-arg BUILD_JOB_ID="${BUILD_JOB_ID}" \
  --build-arg BUILD_NUMBER="${BUILD_NUMBER}" \
  --build-arg BUILD_URL="${BUILD_URL}" \
  --build-arg BUILD_BRANCH="${BUILD_BRANCH}" \
  .

docker image save minion -o "images/minion-${VERSION}.oci"