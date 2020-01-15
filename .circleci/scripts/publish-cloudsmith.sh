#!/bin/bash

set -e
set -o pipefail

PROJECT="opennms"
REPO=""
case "${CIRCLE_BRANCH}" in
  develop)
    REPO="develop"
    ;;
  foundation-*)
    REPO="${CIRCLE_BRANCH}"
    ;;
  release-*)
    REPO="testing"
    ;;
  master)
    REPO="stable"
    ;;
  ranger/cloudsmith)
    REPO="foundation-2019"
    ;;
  *)
    echo "This branch is not eligible for deployment: ${CIRCLE_BRANCH}"
    exit 0
    ;;
esac

find target -type f | sort -u

for FILE in target/rpm/RPMS/noarch/*.rpm; do
  # give it 3 tries then die
  cloudsmith push rpm "${PROJECT}/$REPO/el/5" "$FILE" ||
  cloudsmith push rpm "${PROJECT}/$REPO/el/5" "$FILE" ||
  cloudsmith push rpm "${PROJECT}/$REPO/el/5" "$FILE" || exit 1
done
for FILE in target/debs/*.deb; do
  # give it 3 tries then die
  cloudsmith push deb "${PROJECT}/$REPO/debian/etch" "$FILE" ||
  cloudsmith push deb "${PROJECT}/$REPO/debian/etch" "$FILE" ||
  cloudsmith push deb "${PROJECT}/$REPO/debian/etch" "$FILE" || exit 1
done
