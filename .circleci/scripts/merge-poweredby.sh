#!/bin/sh

set -e
#set -o pipefail

YEAR=""

case "${CIRCLE_BRANCH}" in
  foundation)
    YEAR=2015
    ;;
  foundation-20*)
    # shellcheck disable=SC2001
    YEAR="$(echo "${CIRCLE_BRANCH}" | sed -e 's,^foundation-,,')"
    ;;
esac

if [ -z "${YEAR}" ]; then
  echo "Unable to determine what year this branch applies to.  Skipping build."
  exit 0
fi

if [ "${YEAR}" = "${CIRCLE_BRANCH}" ]; then
  echo "'${CIRCLE_BRANCH}' *looks* like a foundation branch, but it isn't of the format foundation(-XXXX)? -- something went wrong."
  exit 1
fi

POWEREDBY="poweredby-${YEAR}"
export GIT_MERGE_AUTOEDIT=no

echo "=== Found an appropriate Foundation branch.  Merging ${CIRCLE_BRANCH} to ${POWEREDBY}."

REMOTE_BRANCH="$(git remote | grep -c -E "^${POWEREDBY}\$" || :)"
if [ "${REMOTE_BRANCH}" -gt 0 ]; then
  echo "=== ${POWEREDBY} origin exists in git"
else
  echo "=== adding origin ${POWEREDBY} to git"
  git remote add "${POWEREDBY}" "git@github.com:OpenNMS/${POWEREDBY}.git"
fi
git config merge.renameLimit 999999

echo "=== remotes:"
git remote --verbose

echo "=== fetching from git"
git fetch --all

echo "=== pushing ${CIRCLE_BRANCH} to ${POWEREDBY}/from-${CIRCLE_BRANCH}"
git push --force "${POWEREDBY}" "${CIRCLE_BRANCH}:from-${CIRCLE_BRANCH}"

MAIN_BRANCH="${POWEREDBY}-master"
if ! [ "$(git branch | grep -c -E "\\b${MAIN_BRANCH}\$" || :)" -gt 0 ]; then
  echo "=== creating local checkout of master from ${POWEREDBY} - ${MAIN_BRANCH}"
  git checkout -b "${MAIN_BRANCH}" "${POWEREDBY}/master"
fi

echo "=== checking out master from ${POWEREDBY}"
git checkout "${MAIN_BRANCH}"
git reset --hard "${POWEREDBY}/master"

echo "=== merging ${CIRCLE_BRANCH} to ${MAIN_BRANCH}"
git merge "origin/${CIRCLE_BRANCH}"

echo "=== pushing ${MAIN_BRANCH} to ${POWEREDBY}/master"
git push "${POWEREDBY}" "${MAIN_BRANCH}:master"
