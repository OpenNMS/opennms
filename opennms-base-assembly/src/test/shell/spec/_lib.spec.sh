#!/bin/bash

# shellcheck disable=SC2034
OVERRIDEABLE_ARRAYS=(TEST_FOO TEST_BAR TEST_BAZ TEST_ADDITIONAL_OPTIONS)

# shellcheck disable=SC1091
. ../init.sh

# shellcheck disable=SC1090
. "$PROJECTDIR/src/main/resources/bin/_lib.sh"

TESTDIR="$(get_testdir _lib)"

createConfFile() {
  __conf_filename="opennms.conf"
  # shellcheck disable=SC2154
  mkdir -p "$TESTDIR/${_shunit_test_}"
  CONFTEMPDIR="$(mktemp -d "$TESTDIR/${_shunit_test_}/conf-XXXX")"
  touch "${CONFTEMPDIR}/${__conf_filename}"
  echo "${CONFTEMPDIR}/${__conf_filename}"
}

assertIsArray() {
  if [ -n "$2" ]; then
    __onms_is_array "$2"
    assertTrue "$1" "$?"
  else
    __onms_is_array "$1"
    assertTrue "$1 should be an array" "$?"
  fi
}

assertIsNotArray() {
  if [ -n "$2" ]; then
    __onms_is_array "$2"
    assertFalse "$1" "$?"
  else
    __onms_is_array "$1"
    assertFalse "$1 should not be an array" "$?"
  fi
}

assertArrayLengthEquals() {
  if [ -n "$3" ]; then
    __assert_description="$1"
    __assert_array_name="$2"
    __assert_expected_length="$3"
  else
    __assert_description="$1 should be of length $2"
    __assert_array_name="$1"
    __assert_expected_length="$2"
  fi
  __onms_is_array "${__assert_array_name}"
  assertTrue "${__assert_array_name} should be an array" "$?"
  __assert_actual_length="$(eval "echo \${#${__assert_array_name}[@]}")"
  assertEquals "${__assert_description}" "${__assert_expected_length}" "${__assert_actual_length}"
}

setUp() {
  unset TEST_FOO
  unset TEST_BAR
  unset TEST_BAZ
  unset TEST_ADDITIONAL_OPTIONS
}

testShellcheck() {
  if [ -n "$SHELLCHECK" ] && [ -x "$SHELLCHECK" ]; then
    "$SHELLCHECK" "$PROJECTDIR/src/main/resources/bin/_lib.sh"
    assertTrue "shellcheck on bin/_lib.sh should pass" $?
  fi
}

testIsArrayUnsetVariable() {
  unset TEST_FOO
  assertIsNotArray "unset variable" TEST_FOO
}
testIsArrayEmptScalarVariable() {
  TEST_FOO=""
  assertIsNotArray "empty scalar variable" TEST_FOO
}
testIsArrayScalarVariable() {
  TEST_FOO="blah"
  assertIsNotArray "scalar variable" TEST_FOO
}
testIsArrayScalarVariableWithSpaces() {
  TEST_FOO="blah blah blah"
  assertIsNotArray "scalar variable with spaces" TEST_FOO
}
testIsArrayEmptyArray() {
  TEST_FOO=()
  assertIsArray "empty array" TEST_FOO
}
testIsArrayNonEmptyArray() {
  TEST_FOO=(bar baz)
  assertIsArray "array" TEST_FOO
}
testIsArrayArrayWithSpaces() {
  TEST_FOO=(bar baz "blah blah blah")
  assertIsArray "array with spaces" TEST_FOO
}

testConvertArrayUnset() {
  unset TEST_FOO
  __onms_convert_to_array TEST_FOO
  assertIsArray TEST_FOO
  assertArrayLengthEquals TEST_FOO 0
}

testConvertArrayEmptyScalar() {
  # shellcheck disable=2178
  TEST_FOO=""
  __onms_convert_to_array TEST_FOO
  assertIsArray TEST_FOO
  assertArrayLengthEquals TEST_FOO 0
  assertEquals "" "${TEST_FOO[0]}"
}

testConvertArrayScalarOneVar() {
  # shellcheck disable=2178
  TEST_FOO="bar"
  __onms_convert_to_array TEST_FOO
  assertIsArray TEST_FOO
  assertArrayLengthEquals TEST_FOO 1
  assertEquals "bar" "${TEST_FOO[0]}"
}

testConvertArrayScalarMultiVar() {
  # shellcheck disable=2178
  TEST_FOO="bar baz"
  __onms_convert_to_array TEST_FOO
  assertIsArray TEST_FOO
  assertArrayLengthEquals TEST_FOO 2
  assertEquals "bar" "${TEST_FOO[0]}"
  assertEquals "baz" "${TEST_FOO[1]}"
}

testConvertArrayFromEmptyArray() {
  TEST_FOO=()
  __onms_convert_to_array TEST_FOO
  assertIsArray TEST_FOO
  assertArrayLengthEquals TEST_FOO 0
}

testConvertArrayFromArrayWithOneEntry() {
  TEST_FOO=("bar")
  __onms_convert_to_array TEST_FOO
  assertIsArray TEST_FOO
  assertArrayLengthEquals TEST_FOO 1
  assertEquals "bar" "${TEST_FOO[0]}"
}

testConvertArrayFromArrayWithTwoEntries() {
  TEST_FOO=("bar" "baz")
  __onms_convert_to_array TEST_FOO
  assertIsArray TEST_FOO
  assertArrayLengthEquals TEST_FOO 2
  assertEquals "bar" "${TEST_FOO[0]}"
  assertEquals "baz" "${TEST_FOO[1]}"
}

testConvertArrayFromArrayWithTwoAndSpaces() {
  TEST_FOO=("bar" "baz zoom")
  __onms_convert_to_array TEST_FOO
  assertIsArray TEST_FOO
  assertArrayLengthEquals TEST_FOO 2
  assertEquals "bar" "${TEST_FOO[0]}"
  assertEquals "baz zoom" "${TEST_FOO[1]}"
}

testReadNonexistentConf() {
  file="$(createConfFile)"
  rm "${file}"
  __onms_read_conf "${empty_file}"
  # shellcheck disable=SC2128
  assertEquals "" "${TEST_FOO}"
  assertEquals "" "${TEST_FOO[0]}"
  # shellcheck disable=SC2128
  assertEquals "" "${TEST_BAR}"
  assertEquals "" "${TEST_BAR[0]}"
  # shellcheck disable=SC2128
  assertEquals "" "${TEST_BAZ}"
  assertEquals "" "${TEST_BAZ[0]}"
  # shellcheck disable=SC2128
  assertEquals "" "${TEST_ADDITIONAL_OPTIONS}"
  assertEquals "" "${TEST_ADDITIONAL_OPTIONS[0]}"
}

testReadEmptyConf() {
  empty_file="$(createConfFile)"
  assertTrue "[ -f '${empty_file}' ]"
  __onms_read_conf "${empty_file}"
  assertIsArray TEST_FOO
  assertIsArray TEST_BAR
  assertIsArray TEST_BAZ
  assertIsArray TEST_ADDITIONAL_OPTIONS
  assertArrayLengthEquals TEST_FOO 0
  assertArrayLengthEquals TEST_BAR 0
  assertArrayLengthEquals TEST_BAZ 0
  assertArrayLengthEquals TEST_ADDITIONAL_OPTIONS 0
}

testReadSingleConfOneScalarVariable() {
  file="$(createConfFile)"
  cat <<END >"${file}"
TEST_FOO=blah
END
  __onms_read_conf "${file}"
  assertIsArray TEST_FOO
  assertIsArray TEST_BAR
  assertIsArray TEST_BAZ
  assertIsArray TEST_ADDITIONAL_OPTIONS
  assertArrayLengthEquals TEST_FOO 1
  assertArrayLengthEquals TEST_BAR 0
  assertArrayLengthEquals TEST_BAZ 0
  assertArrayLengthEquals TEST_ADDITIONAL_OPTIONS 0
  assertEquals "blah" "${TEST_FOO[0]}"
}

testReadSingleConfTwoScalarVariable() {
  file="$(createConfFile)"
  cat <<END >"${file}"
TEST_FOO=blah
TEST_BAR=baz
END
  __onms_read_conf "${file}"
  assertIsArray TEST_FOO
  assertIsArray TEST_BAR
  assertIsArray TEST_BAZ
  assertIsArray TEST_ADDITIONAL_OPTIONS
  assertArrayLengthEquals TEST_FOO 1
  assertArrayLengthEquals TEST_BAR 1
  assertArrayLengthEquals TEST_BAZ 0
  assertArrayLengthEquals TEST_ADDITIONAL_OPTIONS 0
  assertEquals "blah" "${TEST_FOO[0]}"
  assertEquals "baz" "${TEST_BAR[0]}"
}

testReadSingleConfOneScalarVariableOneArray() {
  file="$(createConfFile)"
  cat <<END >"${file}"
TEST_FOO=blah
TEST_BAR=(baz)
END
  __onms_read_conf "${file}"
  assertIsArray TEST_FOO
  assertIsArray TEST_BAR
  assertIsArray TEST_BAZ
  assertIsArray TEST_ADDITIONAL_OPTIONS
  assertArrayLengthEquals TEST_FOO 1
  assertArrayLengthEquals TEST_BAR 1
  assertArrayLengthEquals TEST_BAZ 0
  assertArrayLengthEquals TEST_ADDITIONAL_OPTIONS 0
  assertEquals "blah" "${TEST_FOO[0]}"
  assertEquals "baz" "${TEST_BAR[0]}"
}

testReadTwoConfsScalarVariables() {
  file1="$(createConfFile)"
  file2="$(createConfFile)"
  cat <<END >"${file1}"
TEST_FOO="file1-one"
TEST_BAR="file1-two things"
END
  cat <<END >"${file2}"
TEST_FOO="file2-one"
TEST_BAR="file2-two things"
END
  __onms_read_conf "${file1}"
  __onms_read_conf "${file2}"
  assertIsArray TEST_FOO
  assertIsArray TEST_BAR
  assertIsArray TEST_BAZ
  assertIsArray TEST_ADDITIONAL_OPTIONS
  assertArrayLengthEquals TEST_FOO 2
  assertArrayLengthEquals TEST_BAR 4
  assertArrayLengthEquals TEST_BAZ 0
  assertArrayLengthEquals TEST_ADDITIONAL_OPTIONS 0
  assertEquals "file1-one" "${TEST_FOO[0]}"
  assertEquals "file2-one" "${TEST_FOO[1]}"
}

testReadTwoConfsOneScalarOneArray() {
  file1="$(createConfFile)"
  file2="$(createConfFile)"
  cat <<END >"${file1}"
TEST_FOO="file1-one"
TEST_BAR=("file1-two things")
END
  cat <<END >"${file2}"
TEST_FOO="file2-one"
TEST_BAR="file2-two things"
END
  __onms_read_conf "${file1}"
  __onms_read_conf "${file2}"
  assertIsArray TEST_FOO
  assertIsArray TEST_BAR
  assertIsArray TEST_BAZ
  assertIsArray TEST_ADDITIONAL_OPTIONS
  assertArrayLengthEquals TEST_FOO 2
  assertArrayLengthEquals TEST_BAR 3
  assertArrayLengthEquals TEST_BAZ 0
  assertArrayLengthEquals TEST_ADDITIONAL_OPTIONS 0
  assertEquals "file1-one" "${TEST_FOO[0]}"
  assertEquals "file2-one" "${TEST_FOO[1]}"
  assertEquals "file1-two things" "${TEST_BAR[0]}"
  assertEquals "file2-two" "${TEST_BAR[1]}"
}

testReadTwoConfsOneScalarOneArray() {
  file1="$(createConfFile)"
  file2="$(createConfFile)"
  cat <<END >"${file1}"
TEST_FOO="file1-one"
TEST_BAR="file1-two things"
END
  cat <<END >"${file2}"
TEST_FOO=("file2-one")
TEST_BAR=("file2-two things")
END
  __onms_read_conf "${file1}"
  __onms_read_conf "${file2}"
  assertIsArray TEST_FOO
  assertIsArray TEST_BAR
  assertIsArray TEST_BAZ
  assertIsArray TEST_ADDITIONAL_OPTIONS
  assertArrayLengthEquals TEST_FOO 2
  assertArrayLengthEquals TEST_BAR 3
  assertArrayLengthEquals TEST_BAZ 0
  assertArrayLengthEquals TEST_ADDITIONAL_OPTIONS 0
  assertEquals "file1-one" "${TEST_FOO[0]}"
  assertEquals "file2-one" "${TEST_FOO[1]}"
  assertEquals "file1-two" "${TEST_BAR[0]}"
  assertEquals "things" "${TEST_BAR[1]}"
  assertEquals "file2-two things" "${TEST_BAR[2]}"
}

# shellcheck disable=SC1091
. ../shunit2
