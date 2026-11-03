#!/usr/bin/env bash

# shellcheck disable=SC1090,SC1091
. "$SHUNITDIR/init.sh"

TESTDIR="$(get_testdir clean-kar-storage)"

oneTimeSetUp() {
  rm -rf "$TESTDIR"
  mkdir -p "$TESTDIR/bin"
  install -m 755 "${PROJECTDIR}/target/classes/bin/_clean-kar-storage.sh" "$TESTDIR/bin/"
  CLEAN_KAR_STORAGE="$TESTDIR/bin/_clean-kar-storage.sh"
  unset JAVA_OPTS EXTRA_JAVA_OPTS KARAF_OPTS KARAF_DATA KARAF_ETC KARAF_CLEAN_CACHE
}

setUp() {
  KARAF_HOME="$TESTDIR/karaf_home"
  rm -rf "$KARAF_HOME"
  mkdir -p "$KARAF_HOME"/{etc,data/kar/my-plugin,data/cache/bundle1,data/log}
}

# writeCleanCache <value>: the karaf.clean.cache line of etc/system.properties
writeCleanCache() {
  printf '# Karaf system properties\nkaraf.name = test\nkaraf.clean.cache = %s\n' "$1" > "$KARAF_HOME/etc/system.properties"
}

runCleanKarStorage() {
  runCommand clean-kar-storage "$CLEAN_KAR_STORAGE" "$@"
}

testShellcheck() {
  if [ -n "$SHELLCHECK" ] && [ -x "$SHELLCHECK" ]; then
    "$SHELLCHECK" "$CLEAN_KAR_STORAGE"
    assertTrue "shellcheck on bin/_clean-kar-storage.sh should pass" $?
  fi
}

testHelp() {
  output="$(runCleanKarStorage -h)"
  assertTrue "$?"
  assertContains "$output" "usage:"
}

testNoKarafHome() {
  output="$(runCleanKarStorage)"
  assertFalse "$?"
  assertContains "$output" "usage:"
}

testRemovesKarStorageWhenCleanCacheIsTrue() {
  writeCleanCache "true"
  output="$(runCleanKarStorage "$KARAF_HOME")"
  assertTrue "$?"
  assertContains "$output" "removing $KARAF_HOME/data/kar"
  assertFalse "data/kar should be removed" "[ -e '$KARAF_HOME/data/kar' ]"
}

testKeepsTheRestOfData() {
  writeCleanCache "true"
  runCleanKarStorage "$KARAF_HOME" >/dev/null
  assertTrue "data/cache should be kept, Karaf cleans it" "[ -d '$KARAF_HOME/data/cache/bundle1' ]"
  assertTrue "data/log should be kept" "[ -d '$KARAF_HOME/data/log' ]"
}

testKeepsKarStorageWhenCleanCacheIsFalse() {
  writeCleanCache "false"
  output="$(runCleanKarStorage "$KARAF_HOME")"
  assertTrue "$?"
  assertEquals "" "$output"
  assertTrue "data/kar should be kept" "[ -d '$KARAF_HOME/data/kar/my-plugin' ]"
}

testKeepsKarStorageWhenCleanCacheIsNotSet() {
  printf 'karaf.name = test\n' > "$KARAF_HOME/etc/system.properties"
  runCleanKarStorage "$KARAF_HOME" >/dev/null
  assertTrue "$?"
  assertTrue "data/kar should be kept" "[ -d '$KARAF_HOME/data/kar/my-plugin' ]"
}

testKeepsKarStorageWithoutSystemProperties() {
  rm -f "$KARAF_HOME/etc/system.properties"
  runCleanKarStorage "$KARAF_HOME" >/dev/null
  assertTrue "$?"
  assertTrue "data/kar should be kept" "[ -d '$KARAF_HOME/data/kar/my-plugin' ]"
}

testCleanCacheIsCaseInsensitive() {
  writeCleanCache "TRUE"
  runCleanKarStorage "$KARAF_HOME" >/dev/null
  assertFalse "data/kar should be removed" "[ -e '$KARAF_HOME/data/kar' ]"
}

testWithoutSpacesAndLastValueWins() {
  printf 'karaf.clean.cache=false\nkaraf.clean.cache=true\n' > "$KARAF_HOME/etc/system.properties"
  runCleanKarStorage "$KARAF_HOME" >/dev/null
  assertFalse "data/kar should be removed" "[ -e '$KARAF_HOME/data/kar' ]"
}

testEnvDefaultWhenUnset() {
  # shellcheck disable=SC2016
  writeCleanCache '${env:KARAF_CLEAN_CACHE:-true}'
  runCleanKarStorage "$KARAF_HOME" >/dev/null
  assertFalse "data/kar should be removed" "[ -e '$KARAF_HOME/data/kar' ]"
}

testEnvDefaultWhenEmpty() {
  # shellcheck disable=SC2016
  writeCleanCache '${env:KARAF_CLEAN_CACHE:-true}'
  KARAF_CLEAN_CACHE='' runCleanKarStorage "$KARAF_HOME" >/dev/null
  assertFalse "data/kar should be removed when the variable is empty" "[ -e '$KARAF_HOME/data/kar' ]"
}

testEnvOverridesDefault() {
  # shellcheck disable=SC2016
  writeCleanCache '${env:KARAF_CLEAN_CACHE:-true}'
  KARAF_CLEAN_CACHE=false runCleanKarStorage "$KARAF_HOME" >/dev/null
  assertTrue "data/kar should be kept" "[ -d '$KARAF_HOME/data/kar/my-plugin' ]"
}

testEnvEnablesWhenDefaultIsFalse() {
  # shellcheck disable=SC2016
  writeCleanCache '${env:KARAF_CLEAN_CACHE:-false}'
  KARAF_CLEAN_CACHE=true runCleanKarStorage "$KARAF_HOME" >/dev/null
  assertFalse "data/kar should be removed" "[ -e '$KARAF_HOME/data/kar' ]"
}

testEnvWithoutDefault() {
  # shellcheck disable=SC2016
  writeCleanCache '${env:KARAF_CLEAN_CACHE}'
  runCleanKarStorage "$KARAF_HOME" >/dev/null
  assertTrue "data/kar should be kept while unset" "[ -d '$KARAF_HOME/data/kar/my-plugin' ]"
  KARAF_CLEAN_CACHE=True runCleanKarStorage "$KARAF_HOME" >/dev/null
  assertFalse "data/kar should be removed" "[ -e '$KARAF_HOME/data/kar' ]"
}

testJavaOptionOverridesSystemProperties() {
  writeCleanCache "true"
  EXTRA_JAVA_OPTS="-Xmx1g -Dkaraf.clean.cache=false" runCleanKarStorage "$KARAF_HOME" >/dev/null
  assertTrue "data/kar should be kept" "[ -d '$KARAF_HOME/data/kar/my-plugin' ]"

  writeCleanCache "false"
  JAVA_OPTS="-Dkaraf.clean.cache=true" runCleanKarStorage "$KARAF_HOME" >/dev/null
  assertFalse "data/kar should be removed" "[ -e '$KARAF_HOME/data/kar' ]"
}

testUsesKarafDataAndKarafEtc() {
  other="$TESTDIR/other"
  rm -rf "$other"
  mkdir -p "$other"/{etc,data/kar/my-plugin}
  printf 'karaf.clean.cache = true\n' > "$other/etc/system.properties"
  writeCleanCache "false"
  KARAF_DATA="$other/data" KARAF_ETC="$other/etc" runCleanKarStorage "$KARAF_HOME" >/dev/null
  assertFalse "\$KARAF_DATA/kar should be removed" "[ -e '$other/data/kar' ]"
  assertTrue "\$KARAF_HOME/data/kar should be kept" "[ -d '$KARAF_HOME/data/kar/my-plugin' ]"
}

testNothingToRemove() {
  writeCleanCache "true"
  rm -rf "$KARAF_HOME/data/kar"
  output="$(runCleanKarStorage "$KARAF_HOME")"
  assertTrue "$?"
  assertEquals "" "$output"
}

testRemovesLinkNotTarget() {
  writeCleanCache "true"
  target="$TESTDIR/kar-target"
  rm -rf "$target" "$KARAF_HOME/data/kar"
  mkdir -p "$target/my-plugin"
  ln -s "$target" "$KARAF_HOME/data/kar"
  runCleanKarStorage "$KARAF_HOME" >/dev/null
  assertFalse "the data/kar link should be removed" "[ -L '$KARAF_HOME/data/kar' ]"
  assertTrue "the link target should be kept" "[ -d '$target/my-plugin' ]"
}

testOptionalFileOverridesSystemProperties() {
  # shellcheck disable=SC2016
  printf '${optionals} = custom.system.properties\nkaraf.clean.cache = true\n' > "$KARAF_HOME/etc/system.properties"
  printf 'karaf.clean.cache = false\n' > "$KARAF_HOME/etc/custom.system.properties"
  runCleanKarStorage "$KARAF_HOME" >/dev/null
  assertTrue "data/kar should be kept" "[ -d '$KARAF_HOME/data/kar/my-plugin' ]"

  # shellcheck disable=SC2016
  printf '${optionals} = custom.system.properties\nkaraf.clean.cache = false\n' > "$KARAF_HOME/etc/system.properties"
  printf 'karaf.clean.cache = true\n' > "$KARAF_HOME/etc/custom.system.properties"
  runCleanKarStorage "$KARAF_HOME" >/dev/null
  assertFalse "data/kar should be removed" "[ -e '$KARAF_HOME/data/kar' ]"
}

testOptionalsOverrideIncludes() {
  # shellcheck disable=SC2016
  printf '${includes} = included.properties\n${optionals} = "optional.properties"\nkaraf.clean.cache = false\n' > "$KARAF_HOME/etc/system.properties"
  printf 'karaf.clean.cache = false\n' > "$KARAF_HOME/etc/included.properties"
  printf 'karaf.clean.cache = true\n' > "$KARAF_HOME/etc/optional.properties"
  runCleanKarStorage "$KARAF_HOME" >/dev/null
  assertFalse "data/kar should be removed" "[ -e '$KARAF_HOME/data/kar' ]"
}

testMissingOptionalFile() {
  # shellcheck disable=SC2016
  printf '${optionals} = custom.system.properties\nkaraf.clean.cache = true\n' > "$KARAF_HOME/etc/system.properties"
  output="$(runCleanKarStorage "$KARAF_HOME")"
  assertTrue "$?"
  assertFalse "data/kar should be removed" "[ -e '$KARAF_HOME/data/kar' ]"
}

testOverridePropertyWinsOverJavaOption() {
  printf 'karaf.override.karaf.clean.cache = false\nkaraf.clean.cache = true\n' > "$KARAF_HOME/etc/system.properties"
  JAVA_OPTS="-Dkaraf.clean.cache=true" runCleanKarStorage "$KARAF_HOME" >/dev/null
  assertTrue "data/kar should be kept" "[ -d '$KARAF_HOME/data/kar/my-plugin' ]"
}

testLastJavaOptionWins() {
  writeCleanCache "false"
  JAVA_OPTS="-Dkaraf.clean.cache=false" KARAF_SYSTEM_OPTS="-Dkaraf.clean.cache=true" runCleanKarStorage "$KARAF_HOME" >/dev/null
  assertFalse "data/kar should be removed" "[ -e '$KARAF_HOME/data/kar' ]"
}

testWhitespaceSeparator() {
  printf 'karaf.clean.cache true\n' > "$KARAF_HOME/etc/system.properties"
  runCleanKarStorage "$KARAF_HOME" >/dev/null
  assertFalse "data/kar should be removed" "[ -e '$KARAF_HOME/data/kar' ]"
}

testUnresolvedPlaceholderKeepsKarStorage() {
  # shellcheck disable=SC2016
  writeCleanCache '${karaf.clean.default}'
  runCleanKarStorage "$KARAF_HOME" >/dev/null
  assertTrue "data/kar should be kept" "[ -d '$KARAF_HOME/data/kar/my-plugin' ]"
}

testWarnsWhenRemovalFails() {
  if [ "$(id -u)" -eq 0 ]; then
    startSkipping
  fi
  writeCleanCache "true"
  chmod 555 "$KARAF_HOME/data"
  output="$(runCleanKarStorage "$KARAF_HOME" 2>&1)"
  ret=$?
  chmod 755 "$KARAF_HOME/data"
  assertEquals 1 "$ret"
  assertContains "$output" "WARNING: failed to remove $KARAF_HOME/data/kar"
}

# shellcheck disable=SC1090,SC1091
. "$SHUNITDIR/shunit2"
