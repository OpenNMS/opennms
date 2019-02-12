#!/bin/bash

# shellcheck disable=SC1091
. ../init.sh

TESTDIR="$(get_testdir runjava)"
FAKE_JAVA_HOME="$TESTDIR/java_home"
RUNJAVA="$PROJECTDIR/src/main/filtered/bin/runjava"

oneTimeSetUp() {
  rm -rf "$TESTDIR"
  mkdir -p "$FAKE_JAVA_HOME"/{bin,include,jre/bin,jre/lib,lib}
  cp "./runjava-fakejava" "$FAKE_JAVA_HOME/bin/java"
  cp "./runjava-fakejava" "$FAKE_JAVA_HOME/jre/bin/java"
  chmod 755 "$FAKE_JAVA_HOME/bin/java" "$FAKE_JAVA_HOME/jre/bin/java"
  REAL_PATH="$PATH"
}

oneTimeTearDown() {
  PATH="$REAL_PATH"
  export PATH
}

setUp() {
  export FAKE_OPENJDK=false
  export FAKE_JAVA_VERSION="1.8.0_69"
  export FAKE_JAVA_BUILD="420-b42"
}

runRunjava() {
  runCommand runjava "$RUNJAVA" "$@"
}

testShellcheck() {
	if [ -n "$SHELLCHECK" ] && [ -x "$SHELLCHECK" ]; then
		"$SHELLCHECK" "$RUNJAVA"
		assertTrue "shellcheck on bin/runjava should pass" $?
	fi
}

testHelp() {
  output="$(runRunjava -h)"
  assertContains "$output" "Exactly one of the following options is required"
}

testSearch() {
  export JAVA_HOME="$FAKE_JAVA_HOME" PATH="$JAVA_HOME/bin:$REAL_PATH"
  # shellcheck disable=SC2154
  javaconf_dir="$TESTDIR/${_shunit_test_}"
  mkdir -p "$javaconf_dir"
  output="$(runRunjava -j "$javaconf_dir" -s)"
  assertContains "$output" "runjava: found: \"$FAKE_JAVA_HOME/bin/java\" is an appropriate JRE"
  assertTrue "[ -f '$TESTDIR/${_shunit_test_}/java.conf' ]"
}

testPrint() {
  export JAVA_HOME="$FAKE_JAVA_HOME" PATH="$JAVA_HOME/bin:$REAL_PATH"
  javaconf_dir="$TESTDIR/${_shunit_test_}"
  mkdir -p "$javaconf_dir"
  runRunjava -j "$javaconf_dir" -s >/dev/null 2>&1
  assertTrue $?

  output="$(runRunjava -j "$javaconf_dir" -q -p)"
  assertEquals "$output" "8.0.69"

  export FAKE_OPENJDK=true
  export FAKE_JAVA_VERSION="1.8.0_215"
  export FAKE_JAVA_BUILD="25.215-b09"
  output="$(runRunjava -j "$javaconf_dir" -q -p)"
  assertEquals "$output" "8.0.215"
}

. ../shunit2