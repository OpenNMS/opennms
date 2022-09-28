#!/bin/bash

OPENNMS_POM_VERSION="$(~/project/.circleci/scripts/pom2version.sh ~/project/pom.xml || echo "0.0.0")"
# shellcheck disable=SC2001
OPENNMS_VERSION="$(echo "${OPENNMS_POM_VERSION}" | sed -e 's,^\([0-9\.][0-9\.]*\).*$,\1,g')"
OPENNMS_SHORT_VERSION="$(echo "${OPENNMS_VERSION}" | cut -d. -f 1-2)"
OPENNMS_MAJOR_VERSION="$(echo "${OPENNMS_VERSION}" | cut -d. -f1)"

# if $CIRCLE_BRANCH is not set, tag it as a "local" build
DOCKER_TAGS=("local" "latest")

if [ -n "${CIRCLE_BRANCH}" ]; then
  # If $CIRCLE_BRANCH _is_ set, dump the local and latest tags,
  # and instead set tags based on the branch.

  # Always include $CIRCLE_BRANCH with "/" turned into "-".
  DOCKER_BRANCH_TAG="${CIRCLE_BRANCH//\//-}"
  DOCKER_TAGS=("${DOCKER_BRANCH_TAG}")

  # In addition, set a few extra tag aliases for convenience.
  case "${CIRCLE_BRANCH}" in
    "master-"*)
      DOCKER_TAGS=("${DOCKER_TAGS[@]}" "${OPENNMS_VERSION}" "${OPENNMS_SHORT_VERSION}-latest" "${OPENNMS_MAJOR_VERSION}-latest" "latest")
      ;;
    "release-"*)
      DOCKER_TAGS=("${DOCKER_TAGS[@]}" "${OPENNMS_POM_VERSION}" "${OPENNMS_VERSION}-rc" "${OPENNMS_SHORT_VERSION}-rc" "${OPENNMS_MAJOR_VERSION}-rc" "release-candidate")
      ;;
    "develop")
      DOCKER_TAGS=("${DOCKER_TAGS[@]}" "${OPENNMS_POM_VERSION}" "${OPENNMS_MAJOR_VERSION}-dev" "bleeding")
      ;;
  esac
fi

# this will return "1" for versions without -SNAPSHOT, and "0" for versions with
ONMS_MAJOR_REVISION="$(echo "${OPENNMS_POM_VERSION}" | (grep -c -v -- -SNAPSHOT || :))"
ONMS_MINOR_REVISION="$(date '+%Y%m%d')"
ONMS_MICRO_REVISION=1

if [ -n "$CIRCLE_BUILD_NUM" ] && [ -n "$CIRCLE_BRANCH" ]; then
  _branch_name="$(echo "${CIRCLE_BRANCH}" | sed -e 's,[^[:alnum:]][^[:alnum:]]*,.,g' -e 's,^\.,,' -e 's,\.$,,')"
  ONMS_MINOR_REVISION="${ONMS_MINOR_REVISION}.${_branch_name}"
  ONMS_MICRO_REVISION="${CIRCLE_BUILD_NUM}"
fi

# retry a command 3 times
do_with_retries() {
  # shellcheck disable=SC2034
  for try in 1 2 3; do
    if "$@"; then return 0; fi
  done
  return 1
}

export \
  OPENNMS_POM_VERSION \
  OPENNMS_VERSION \
  OPENNMS_SHORT_VERSION \
  OPENNMS_MAJOR_VERSION \
  \
  ONMS_MAJOR_REVISION \
  ONMS_MINOR_REVISION \
  ONMS_MICRO_REVISION \
  \
  DOCKER_TAGS
