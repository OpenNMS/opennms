#!/bin/bash

if [ -n "$1" ]; then
  OPENNMS_FULL_VERSION="$1"; shift
fi

if [ -n "$1" ]; then
  TYPE="$1"; shift
fi

set -e

if [ -z "${OPENNMS_FULL_VERSION}" ]; then
  echo "usage: $0 <opennms-version> [type]"
  echo ""
  exit 1
fi

if [ -z "$TYPE" ]; then
  TYPE="horizon"
fi

OPENNMS_VERSION="$(echo "${OPENNMS_FULL_VERSION}" | cut -d- -f1)"
WARN=1
FAILED=0

case "$OPENNMS_FULL_VERSION" in
  *-SNAPSHOT)
    WARN=1
    ;;
  *)
    WARN=0
    ;;
esac

echo "* validating copyright display"
CURRENT_YEAR="$(date '+%Y')"
JSP_COUNT="$(grep -c "1999-${CURRENT_YEAR}" opennms-webapp/src/main/webapp/{about,includes}/*.jsp | grep -c -v -E ':0$' || :)"
if [ "${JSP_COUNT}" -eq 0 ]; then
  echo "  WARNING: JSP files in opennms-webapp/src/main/webapp/about and opennms-webapp/src/main/webapp/includes are missing ${CURRENT_YEAR} in their copyright notices."
  FAILED=1
fi

echo "* validating documentation"
DOCDIR="opennms-doc/releasenotes/src/asciidoc"
if [ -d docs ] && [ -e "docs/antora.yml" ]; then
  DOCDIR="docs/modules/releasenotes/pages"
fi
DOC_VERSION_COUNT="$(find "${DOCDIR}" -type f -print0 | xargs -0 cat | grep -c "${OPENNMS_VERSION}" || :)"
if [ "${DOC_VERSION_COUNT}" -eq 0 ]; then
  echo "  WARNING: Release notes in ${DOCDIR} are missing an entry for ${OPENNMS_VERSION} -- this is required for release."
  FAILED=1
fi

echo "* validating changelog files"
for CHANGELOG_FILE in debian/changelog opennms-assemblies/minion/src/main/filtered/debian/changelog opennms-assemblies/sentinel/src/main/filtered/debian/changelog; do
  if [ -e "${CHANGELOG_FILE}" ]; then
    DEB_VERSION_COUNT="$(grep -c "${OPENNMS_VERSION}-" ${CHANGELOG_FILE} || :)"
    if [ "$TYPE" = "horizon" ] && [ "${DEB_VERSION_COUNT}" -eq 0 ]; then
      echo "  WARNING: ${CHANGELOG_FILE} is missing an entry for ${OPENNMS_VERSION} -- this is required for release."
      FAILED=1
    fi
  fi
done

echo "* validating OIA"
OIA_SNAPSHOT_COUNT="$(grep opennmsApiVersion pom.xml | grep -c -- -SNAPSHOT || :)"
if [ "$OIA_SNAPSHOT_COUNT" -gt 0 ]; then
  echo "  WARNING: \${opennmsApiVersion} is set to a -SNAPSHOT version in pom.xml; OIA must be bumped to a release version before we can continue."
  FAILED=1
fi

if [ "${FAILED}" -eq 1 ]; then
  echo ""
  echo "-------------------------------------------------------------"
  if [ "${WARN}" -eq 1 ]; then
    echo "WARNING: one or more validation issues were found."
    echo "Make sure they are fixed before making a release."
  else
    echo "ERROR: one or more validation issues were found."
    echo "Since this branch is not a -SNAPSHOT branch, this is fatal."
    echo "Please prepare this branch for release."
  fi
  echo "-------------------------------------------------------------"
  echo ""
fi

if [ "${WARN}" -eq 1 ]; then
  exit 0
fi

exit "${FAILED}"
