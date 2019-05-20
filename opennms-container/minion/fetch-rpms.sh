#!/usr/bin/env bash

# Exit script if a statement returns a non-true return value.
set -o errexit

# Use the error status of the first failure, rather than that of the last item in a pipeline.
set -o pipefail

# shellcheck source=opennms-container/minion/config.sh
source ./config.sh

for PKG in ${MINION_PACKAGES}; do 
  cp ../../target/rpm/RPMS/noarch/"${PKG}"*.rpm rpms
done