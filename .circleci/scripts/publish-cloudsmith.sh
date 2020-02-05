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

publishPackage() {
  local _tmpdir;
  _tmpdir="$(mktemp -d 2>/dev/null || mktemp -d -t 'publish_cloudsmith_')"
  echo "$@"
  "$@" >"${_tmpdir}/publish.log" 2>&1
  ret="$?"
  cat "${_tmpdir}/publish.log"
  if [ "$(grep -c "This package duplicates the attributes of another package" < "${_tmpdir}/publish.log")" -gt 0 ]; then
    echo "Duplicate upload... skipping."
    return 0
  fi
  rm "${_tmpdir}/publish.log"
  rmdir "${_tmpdir}" || :
  return "$ret"
}

for FILE in target/rpm/RPMS/noarch/*.rpm; do
  # give it 3 tries then die
  publishPackage cloudsmith push rpm "${PROJECT}/$REPO/any-distro/any-version" "$FILE" ||
  publishPackage cloudsmith push rpm "${PROJECT}/$REPO/any-distro/any-version" "$FILE" ||
  publishPackage cloudsmith push rpm "${PROJECT}/$REPO/any-distro/any-version" "$FILE" || exit 1
done
for FILE in target/debs/*.deb; do
  # give it 3 tries then die
  publishPackage cloudsmith push deb "${PROJECT}/$REPO/any-distro/any-version" "$FILE" ||
  publishPackage cloudsmith push deb "${PROJECT}/$REPO/any-distro/any-version" "$FILE" ||
  publishPackage cloudsmith push deb "${PROJECT}/$REPO/any-distro/any-version" "$FILE" || exit 1
done
