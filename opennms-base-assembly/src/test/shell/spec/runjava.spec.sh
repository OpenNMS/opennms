#!/bin/bash

# shellcheck disable=SC1091
. ../init.sh

TESTDIR="$(get_testdir runjava)"
FAKE_JAVA_HOME="$TESTDIR/java_home"
FAKE_OPENNMS_HOME="$TESTDIR/opennms_home"

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

runRunjava() {
  runCommand runjava "$RUNJAVA" "$@"
}

oneTimeSetUp() {
  rm -rf "$TESTDIR"
  makeFakeJava "$FAKE_JAVA_HOME" false "1.8.0_69" "420-b42"

  mkdir -p "$FAKE_OPENNMS_HOME"/{bin,data,lib}
  cp "$PROJECTDIR/src/main/resources/bin/_lib.sh" "$FAKE_OPENNMS_HOME/bin/"
  cp "$PROJECTDIR/src/main/filtered/bin/find-java.sh" "$FAKE_OPENNMS_HOME/bin/"
  sed -e "s,\${install.dir},${FAKE_OPENNMS_HOME},g" \
    "$PROJECTDIR/src/main/filtered/bin/runjava" \
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

. ../shunit2
