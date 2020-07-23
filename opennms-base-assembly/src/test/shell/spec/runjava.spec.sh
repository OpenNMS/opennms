#!/bin/bash

# shellcheck disable=SC1090,SC1091
. "$SHUNITDIR/init.sh"

TESTDIR="$(get_testdir runjava)"
FAKE_JAVA_HOME="$TESTDIR/java_home"
FAKE_OPENNMS_HOME="$TESTDIR/opennms_home"
find "$TESTDIR" -type f \( -name \*.sh -o -name runjava\* \) -exec chmod a+x {} \;

runRunjava() {
  runCommand runjava "$RUNJAVA" "$@"
}

oneTimeSetUp() {
  rm -rf "$TESTDIR"
  makeFakeJava "$FAKE_JAVA_HOME" false "1.8.0_69" "420-b42"

  mkdir -p "$FAKE_OPENNMS_HOME"/{bin,data,lib}
  install -m 755 "$PROJECTDIR/target/classes/bin/_lib.sh" "$FAKE_OPENNMS_HOME/bin/"
  install -m 755 "$PROJECTDIR/target/classes/bin/find-java.sh" "$FAKE_OPENNMS_HOME/bin/"
  sed -e "s,\${install.dir},${FAKE_OPENNMS_HOME},g" \
    "$PROJECTDIR/target/classes/bin/runjava" \
    > "$FAKE_OPENNMS_HOME/bin/runjava"
  chmod 755 "$FAKE_OPENNMS_HOME/bin/runjava"

  RUNJAVA="$FAKE_OPENNMS_HOME/bin/runjava"
  REAL_PATH="$PATH"
}

oneTimeTearDown() {
  PATH="$REAL_PATH"
  export PATH
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

testSearchWithJavaHome() {
  export JAVA_HOME="$FAKE_JAVA_HOME" PATH="$JAVA_HOME/bin:$REAL_PATH"
  # shellcheck disable=SC2154
  javaconf_dir="$TESTDIR/${_shunit_test_}"
  mkdir -p "$javaconf_dir"
  output="$(runRunjava -j "$javaconf_dir" -s)"
  assertContains "$output" "runjava: Found: \"$FAKE_JAVA_HOME/bin/java\" is an appropriate JVM"
  assertTrue "[ -f '$TESTDIR/${_shunit_test_}/java.conf' ]"
  found_java="$(cat "$TESTDIR/${_shunit_test_}/java.conf")"
  assertEquals "$FAKE_JAVA_HOME/bin/java" "$found_java"
}

testSearchWithJavaHomeUnset() {
  javaconf_dir="$TESTDIR/${_shunit_test_}"
  JAVA_HOME="" PATH="${javaconf_dir}/bin:$REAL_PATH"
  export JAVA_HOME PATH

  mkdir -p "$javaconf_dir/bin"
  ln -s "$FAKE_JAVA_HOME/bin/java" "$javaconf_dir/bin/java"

  output="$(runRunjava -j "$javaconf_dir" -s)"
  assertContains "$output" "Found an appropriate JVM in the PATH"
  assertTrue "[ -f '$TESTDIR/${_shunit_test_}/java.conf' ]"
  found_java="$(cat "$TESTDIR/${_shunit_test_}/java.conf")"
  assertEquals "it should follow the symlink to the 'real' file" "$FAKE_JAVA_HOME/bin/java" "$found_java"
}

testPrint() {
  export JAVA_HOME="$FAKE_JAVA_HOME" PATH="$JAVA_HOME/bin:$REAL_PATH"
  javaconf_dir="$TESTDIR/${_shunit_test_}"
  mkdir -p "$javaconf_dir"
  runRunjava -j "$javaconf_dir" -s >/dev/null 2>&1
  assertTrue $?

  output="$(runRunjava -j "$javaconf_dir" -q -p)"
  assertEquals "8.0.69" "$output"

  makeFakeJava "$FAKE_JAVA_HOME" true "1.8.0_215" "25.215-b09"
  output="$(runRunjava -j "$javaconf_dir" -q -p)"
  assertEquals "8.0.215" "$output"
}

# shellcheck disable=SC1090,SC1091
. "$SHUNITDIR/shunit2"
