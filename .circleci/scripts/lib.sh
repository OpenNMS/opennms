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

  # By default, include $CIRCLE_BRANCH with "/" turned into "-".
  DOCKER_BRANCH_TAG="${CIRCLE_BRANCH//\//-}"
  DOCKER_TAGS=("${DOCKER_BRANCH_TAG}")

  # In addition, set a few extra tag aliases for convenience.
  case "${CIRCLE_BRANCH}" in
    "master-"*)
      # Only point to the "real" version of the release, plus update the floating "latest" tag.
      # Don't create a master-* tag; it's redundant.
      DOCKER_TAGS=("${OPENNMS_VERSION}" "latest")
      ;;
    "release-"*)
      # Create a tag for the snapshot version, as well as floating "release-candidate".
      # Don't create a release-*.x tag; it's redundant.
      DOCKER_TAGS=("${OPENNMS_POM_VERSION}" "release-candidate-${OPENNMS_MAJOR_VERSION}" "release-candidate")
      ;;
    "develop")
      # Create a tag for the snapshot version, as well as floating "bleeding".
      # Also allow "develop" as an alias for "bleeding"; we don't reset $DOCKER_TAGS,
      # like we do for master and release.
      DOCKER_TAGS=("${DOCKER_TAGS[@]}" "${OPENNMS_POM_VERSION}" "bleeding")
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

# get the PR number from CircleCI environment; caches on success
__cache_pr_num=""
get_pr_num() {
  if [ -z "${__cache_pr_num}" ]; then
    local _pr_num=0
    if [ -n "${CIRCLE_PULL_REQUEST}" ]; then
      _pr_num="$(echo "${CIRCLE_PULL_REQUEST}" | sed -e 's,.*/,,')"
      if [ -n "${_pr_num}" ] && [ "${_pr_num}" -gt 0 ]; then
        __cache_pr_num="${_pr_num}"
      fi
    fi
    __cache_pr_num=0
  fi
  if [ ! "${__cache_pr_num}" -gt 0 ]; then
    return 1
  fi
  echo "${__cache_pr_num}"
}

# get the "reference" (merge/pr-parent) branch for this branch; caches on success
__cache_reference_branch=""
get_reference_branch() {
  if [ -z "${__cache_reference_branch}" ]; then
    local _parent_branch=""

    local _pr_num="$(get_pr_num || echo 0)"

    if [ "${_pr_num}" -gt 0 ] && [ -n "${GITHUB_API_TOKEN}" ]; then
      local _github_base="$(curl -s -H "Accept: application/vnd.github+json" -H "Authorization: Bearer ${GITHUB_API_TOKEN}" "https://api.github.com/repos/${CIRCLE_PROJECT_USERNAME}/${CIRCLE_PROJECT_REPONAME}/pulls/${_pr_num}" | jq -r '.base.ref')"
      if [ -n "${_github_base}" ]; then
        __cache_reference_branch="${_github_base}"
      fi
    fi

    if [ -z "${__cache_reference_branch}" ] && [ -e .nightly ]; then
      _parent_branch="$(cat .nightly | grep -E '^parent_branch:' | sed -e 's,parent_branch: *,,')"
      if [ -n "${_parent_branch}" ]; then
        __cache_reference_branch="${_parent_branch}"
      fi
    fi
  fi

  if [ -n "${__cache_reference_branch}" ]; then
    echo "${__cache_reference_branch}"
    return 0
  fi

  return 1
}

# retry a command 3 times
do_with_retries() {
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
