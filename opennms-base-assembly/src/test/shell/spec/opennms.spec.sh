#!/bin/bash

# shellcheck disable=SC1090,SC1091
. "$SHUNITDIR/init.sh"

oneTimeSetUp() {
	MYUSER="$(id -u -n)"
	TESTDIR="$(get_testdir opennms)"
	INSTPREFIX="$TESTDIR/instprefix"
	rm -rf "$TESTDIR"

	mkdir -p "$INSTPREFIX"/{bin,etc/imports,etc/init.d,log,run,tmp}
}

setUp() {
	cp "$PROJECTDIR/target/classes/bin/_lib.sh" "$INSTPREFIX/bin/"
	for DIR in "$PROJECTDIR/target/classes/bin" "$PROJECTDIR/src/main/filtered/bin" "$PROJECTDIR/target/cli/bin"; do
		for FILE in opennms runjava find-java.sh; do
			if [ -e "$DIR/$FILE" ]; then
				sed -e "s,\${install.dir},${INSTPREFIX},g" \
					-e "s,\${install.pid.file},${INSTPREFIX}/run/opennms.pid,g" \
					-e "s,\${install.package.description},OpenNMS Shell Test,g" \
					-e "s,\${install.postgresql.service},postgresql,g" \
					-e "s,\${install.logs.dir},${INSTPREFIX}/log,g" \
					-e "s,\${install.init.dir},${INSTPREFIX}/etc/init.d,g" \
					-e "s,^RUNAS=.*,RUNAS=$MYUSER,g" \
					-e "s,^OPENNMS_UNIT_TEST=.*,OPENNMS_UNIT_TEST=1,g" \
					"$DIR/$FILE" > "$INSTPREFIX/bin/$FILE"
				chmod 755 "$INSTPREFIX/bin/$FILE"
			fi
		done
	done

	export OPENNMS_HOME="$INSTPREFIX"
	export OPENNMS_UNIT_TEST_STATUS=0

	"$INSTPREFIX/bin/runjava" "-q" "-s"
	assertEquals 'runjava should have succeeded' 0 "$?"

	touch "$INSTPREFIX/etc/configured"
	echo "STATUS_WAIT=1" > "$INSTPREFIX/etc/opennms.conf"
	echo "STOP_TIMEOUT=1" >> "$INSTPREFIX/etc/opennms.conf"
}

runOpennms() {
	runCommand opennms "$INSTPREFIX/bin/opennms" -n "$@"
}

testShellcheck() {
	if [ -n "$SHELLCHECK" ] && [ -x "$SHELLCHECK" ]; then
		"$SHELLCHECK" "$INSTPREFIX/bin/opennms"
		assertTrue "shellcheck on bin/opennms should pass" $?
	fi
}

testSetUp() {
	assertTrue "translated opennms script must exist" "[ -f '$INSTPREFIX/bin/opennms' ]"

	# shellcheck disable=SC2016
	install_count="$(grep -c '${install.*}' "$INSTPREFIX/bin/opennms")"
	assertTrue "there should be no \${install.*} variables" "[ '$install_count' -eq 0 ]";
}

testNoArguments() {
	output="$(runOpennms 2>&1)"
	assertContains "output should contain usage/help text" "$output" "Usage:"
}

testStart() {
	export OPENNMS_UNIT_TEST_STATUS=3
	output="$(runOpennms -f start 2>&1)"
	assertContains "runjava should be skipping execution" "$output" "Skipping execution:"
	assertContains "ADDITIONAL_MANAGER_OPTIONS should include -DisThreadContextMapInheritable=true" "$output" "'-DisThreadContextMapInheritable=true'"
	assertNotContains "HOTSPOT is not enabled by default, no -server" "$output" "-server"
	assertNotContains "Incremental GC is not enabled by default, no -Xincgc" "$output" "-Xincgc"
}

testStop() {
	output="$(runOpennms -f stop 2>&1)"
	assertContains "$output" " 'exit'"
	assertTrue "opennms_bootstrap.jar stop should be run once" "[ $(echo "$output" | grep -c " 'stop'") -eq 1 ]"
	assertTrue "opennms_bootstrap.jar exit should be run once" "[ $(echo "$output" | grep -c " 'exit'") -eq 1 ]"
}

testRestart() {
	export OPENNMS_UNIT_TEST_STATUS=3
	output="$(runOpennms -f restart 2>&1)"
	assertContains "$output" "but it's already stopped"
	assertContains "$output" "Skipping execution"
	assertContains "$output" "opennms_bootstrap.jar' 'start'"
}

testAdditionalManagerOptionsVariableOneArgument() {
	export OPENNMS_UNIT_TEST_STATUS=3
	echo "ADDITIONAL_MANAGER_OPTIONS=\"-DisThreadContextMapInheritable=false\"" >> "$INSTPREFIX/etc/opennms.conf"
	output="$(runOpennms -f start 2>&1)"
	assertContains "$output" "'-DisThreadContextMapInheritable=false'"
	assertNotContains "$output" "'-DisThreadContextMapInheritable=true'"
}

testAdditionalManagerOptionsVariableTwoArguments() {
	export OPENNMS_UNIT_TEST_STATUS=3
	echo "ADDITIONAL_MANAGER_OPTIONS=\"-DisThreadContextMapInheritable=false -Dgroovy.use.classvalue=false\"" >> "$INSTPREFIX/etc/opennms.conf"
	output="$(runOpennms -f start 2>&1)"
	assertContains "$output" "'-DisThreadContextMapInheritable=false'"
	assertNotContains "$output" "'-DisThreadContextMapInheritable=true'"
	assertContains "$output" "'-Dgroovy.use.classvalue=false'"
	assertNotContains "$output" "'-Dgroovy.use.classvalue=true'"
}

testAdditionalManagerOptionsArrayOneArgument() {
	export OPENNMS_UNIT_TEST_STATUS=3
	echo "ADDITIONAL_MANAGER_OPTIONS=(\"-DisThreadContextMapInheritable=false\")" >> "$INSTPREFIX/etc/opennms.conf"
	output="$(runOpennms -f start 2>&1)"
	assertContains "$output" "'-DisThreadContextMapInheritable=false'"
	assertNotContains "$output" "'-DisThreadContextMapInheritable=true'"
}

testAdditionalManagerOptionsArrayTwoArguments() {
	export OPENNMS_UNIT_TEST_STATUS=3
	echo "ADDITIONAL_MANAGER_OPTIONS=(\"-DisThreadContextMapInheritable=false\" \"-Dgroovy.use.classvalue=false\")" >> "$INSTPREFIX/etc/opennms.conf"
	output="$(runOpennms -f start 2>&1)"
	assertContains "$output" "'-DisThreadContextMapInheritable=false'"
	assertNotContains "$output" "'-DisThreadContextMapInheritable=true'"
	assertContains "$output" "'-Dgroovy.use.classvalue=false'"
	assertNotContains "$output" "'-Dgroovy.use.classvalue=true'"
}

testJavaHeapSize() {
	export OPENNMS_UNIT_TEST_STATUS=3
	echo "JAVA_HEAP_SIZE=1234" >> "$INSTPREFIX/etc/opennms.conf"
	output="$(runOpennms -f start 2>&1)"
	assertContains "Java calls should have a modified heap size" "$output" "'-Xmx1234m'"
	assertNotContains "Java calls should not have the default heap size" "$output" "'-Xmx1024m'"
}

testIncrementalGC() {
	export OPENNMS_UNIT_TEST_STATUS=3
	echo "USE_INCGC=true" >> "$INSTPREFIX/etc/opennms.conf"
	output="$(runOpennms -f start 2>&1)"
	assertContains "The -Xincgc flag should be passed" "$output" "'-Xincgc'"
}

testVerboseGC() {
	export OPENNMS_UNIT_TEST_STATUS=3
	echo "VERBOSE_GC=true" >> "$INSTPREFIX/etc/opennms.conf"
	output="$(runOpennms -f start 2>&1)"
	assertContains "The -verbose:gc flag should be passed" "$output" "'-verbose:gc'"
}

testAgalueJavaConf() {
	export OPENNMS_UNIT_TEST_STATUS=3
	cp "opennms.agalue.java.conf" "$INSTPREFIX/etc/opennms.conf"
	output="$(runOpennms -f start 2>&1)"
	assertContains "$output" "'-Xmx8192m'"
	assertContains "$output" "'-d64'"
	assertContains "$output" "'-XX:+StartAttachListener'"
}

testJessieJavaConf() {
	export OPENNMS_UNIT_TEST_STATUS=3
	cp "opennms.jessie.java.conf" "$INSTPREFIX/etc/opennms.conf"
	output="$(runOpennms -f start 2>&1)"
	assertContains "$output" "'-XX:+UseG1GC'"
	assertContains "$output" "'-Xloggc:/opt/opennms/logs/gc.log'"
	assertContains "$output" "'-XX:+UseGCLogFileRotation'"
	assertContains "$output" "'-XX:NumberOfGCLogFiles=4'"
	assertContains "$output" "'-XX:GCLogFileSize=20M'"
	assertContains "$output" "'-Xmx8196m'"
}

testRpmnewFailure() {
	export OPENNMS_UNIT_TEST_STATUS=3
	touch "$INSTPREFIX/etc/foo.rpmnew"
	output="$(runOpennms -f start 2>&1)"
	assertContains "$output" "The format of the original files may have changed since"
	runOpennms -f start >/dev/null 2>&1
	assertEquals '"opennms start" should have failed' 1 "$?"
}

testRpmsaveFailure() {
	export OPENNMS_UNIT_TEST_STATUS=3
	touch "$INSTPREFIX/etc/foo.rpmsave"
	output="$(runOpennms -f start 2>&1)"
	assertContains "$output" "The format of the original files may have changed since"
	runOpennms -f start >/dev/null 2>&1
	assertEquals '"opennms start" should have failed' 1 "$?"
}

testDpkgDistFailure() {
	export OPENNMS_UNIT_TEST_STATUS=3
	touch "$INSTPREFIX/etc/foo.dpkg-dist"
	output="$(runOpennms -f start 2>&1)"
	assertContains "$output" "The format of the original files may have changed since"
	runOpennms -f start >/dev/null 2>&1
	assertEquals '"opennms start" should have failed' 1 "$?"
}

# shellcheck disable=SC1090,SC1091
. "$SHUNITDIR/shunit2"
