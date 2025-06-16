#!/usr/bin/env bash

# shellcheck disable=SC1090,SC1091
. "$SHUNITDIR/init.sh"

TESTDIR="$(get_testdir find-java)"
find "$TESTDIR" -type f \( -name \*.sh -o -name runjava\* \) -exec chmod a+x {} \;

oneTimeSetUp() {
  rm -rf "$TESTDIR"
  REAL_PATH="$PATH"
  export JAVA_SEARCH_DIRS="$TESTDIR"

  mkdir -p "${TESTDIR}/opennms_home/bin"
  install -m 755 "${PROJECTDIR}/target/classes/bin/find-java.sh" \
    "${PROJECTDIR}/target/classes/bin/_lib.sh" \
    "${TESTDIR}/opennms_home/bin/"
  FIND_JAVA="${TESTDIR}/opennms_home/bin/find-java.sh"

  makeFakeJava "$TESTDIR/1.6-oracle" false "1.6.0_900" "b52"
  makeFakeJava "$TESTDIR/1.6-openjdk" true "1.6.0_32" "b41"
  makeFakeJava "$TESTDIR/1.7-oracle" false "1.7.0_800" "b15"
  makeFakeJava "$TESTDIR/1.7-openjdk" true "1.7.0_80" "b11"
  makeFakeJava "$TESTDIR/1.8-oracle" false "1.8.0_850" "b42"
  makeFakeJava "$TESTDIR/1.8-openjdk" true "1.8.0_191" "b12"
  makeFakeJava "$TESTDIR/9-oracle" false "9.0.100" "11"
  makeFakeJava "$TESTDIR/9-openjdk" true "9.0.4" "13"
  makeFakeJava "$TESTDIR/11-openjdk" true "11.0.2" "9-LTS"
  makeFakeJava "$TESTDIR/17-openjdk" true "17-ea" "26-2439"
}

oneTimeTearDown() {
  PATH="$REAL_PATH"
  export PATH
}

runFindJava() {
  runCommand find-java "$FIND_JAVA" "$@"
}

testShellcheck() {
  if [ -n "$SHELLCHECK" ] && [ -x "$SHELLCHECK" ]; then
    "$SHELLCHECK" "$FIND_JAVA"
    assertTrue "shellcheck on bin/find-java.sh should pass" $?
  fi
}

testHelp() {
  output="$(runFindJava -h)"
  assertContains "$output" "This script will print the location of the newest JDK"
}

testJavaSearch() {
  output="$(runFindJava -s -v)"
  assertTrue "$?"
  assertEquals "17" "$output"
}

testJavaSearch6to8() {
  output="$(runFindJava -s -v 6 9)"
  assertTrue "$?"
  assertEquals "1.8.0_850" "$output"
}

testJavaSearch6to6() {
  output="$(runFindJava -s -v 6 7)"
  assertTrue "$?"
  assertEquals "1.6.0_900" "$output"
}

testJavaSearch8to11() {
  output="$(runFindJava -s -v 8 12)"
  assertTrue "$?"
  assertEquals "11.0.2" "$output"
}

testJavaSearch18() {
  output="$(runFindJava -s -v 18)"
  assertFalse "$?"
  assertEquals "No match found!" "$output"
}

testJavaSearchSameMinMax() {
  output="$(runFindJava -s -v 8 8)"
  assertFalse "$?"
  assertEquals "No match found!" "$output"
}

testMatchingJavaSpecifiedIsOK() {
  output="$(runFindJava -s -v 8 9 "$TESTDIR/1.8-oracle/bin/java")"
  assertTrue "$?"
  assertEquals "1.8.0_850" "$output"
}

testMatchingJavaSpecifiedIsNotInRange() {
  output="$(runFindJava -s -v 8 9 "$TESTDIR/1.6-oracle/bin/java")"
  assertFalse "$?"
  assertEquals "No match found!" "$output"
}

# shellcheck disable=SC1090,SC1091
. "$SHUNITDIR/shunit2"
