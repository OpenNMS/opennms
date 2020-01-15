#!/bin/bash

# this will return "1" for versions without -SNAPSHOT, and "0" for versions with
ONMS_MAJOR_REVISION="$(grep '<version>' pom.xml | head -n 1 | grep -c -v -- -SNAPSHOT)"

ONMS_MINOR_REVISION="$(date '+%Y%m%d')"
ONMS_MICRO_REVISION=1

if [ -n "$CIRCLE_BUILD_NUM" ] && [ -n "$CIRCLE_BRANCH" ]; then
  _branch_name="$(echo "${CIRCLE_BRANCH}" | sed -e 's,[^[:alnum:]][^[:alnum:]]*,.,g' -e 's,^\.,,' -e 's,\.$,,')"
  ONMS_MINOR_REVISION="${ONMS_MINOR_REVISION}.${_branch_name}"
  ONMS_MICRO_REVISION="${CIRCLE_BUILD_NUM}"
fi

export \
  ONMS_MAJOR_REVISION \
  ONMS_MINOR_REVISION \
  ONMS_MICRO_REVISION
