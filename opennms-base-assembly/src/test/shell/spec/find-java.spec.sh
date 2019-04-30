#!/bin/bash

# shellcheck disable=SC1091
. ../init.sh

TESTDIR="$(get_testdir find-java)"

makeFakeJava() {
  if [ -z "$4" ]; then
    echo "usage: makeFakeJava <java_home> <is_openjdk> <version> <build>"
    exit 1
  fi
  mkdir -p "$1"/{bin,include,jre/bin,jre/lib,lib}
  sed -e "s,@fake_java_version@,$3,g" \
    -e "s,@fake_java_build@,$4,g" \
    -e "s,@fake_openjdk@,$2,g" \
    "./runjava-fakejava" > "$1/bin/java"
    cp "$1/bin/java" "$1/jre/bin/java"
    chmod 755 "$1/bin/java" "$1/jre/bin/java"
}

oneTimeSetUp() {
  rm -rf "$TESTDIR"
  REAL_PATH="$PATH"
  export JAVA_SEARCH_DIRS="$TESTDIR"

  mkdir -p "${TESTDIR}/opennms_home/bin"
  cp "${PROJECTDIR}/src/main/filtered/bin/find-java.sh" \
    "${PROJECTDIR}/src/main/resources/bin/_lib.sh" \
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
  assertEquals "9.0.100" "$output"
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

testJavaSearch10() {
  output="$(runFindJava -s -v 10)"
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

. ../shunit2
