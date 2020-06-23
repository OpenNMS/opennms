#!/bin/bash

find_projectdir() {
	searchdir="$1"
	if [ -z "$searchdir" ]; then
		searchdir=".."
	fi

	if [ -r "$searchdir/pom.xml" ]; then
		(cd "$searchdir" || exit 1; pwd)
	else
		find_projectdir "$searchdir/.."
	fi
}

get_testdir() {
	mkdir -p "$TARGETDIR/shunit/$1"
	echo "$TARGETDIR/shunit/$1"
}

makeFakeJava() {
  if [ -z "$4" ]; then
    echo "usage: makeFakeJava <java_home> <is_openjdk> <version> <build>"
    exit 1
  fi
  mkdir -p "$1"/{bin,include,jre/bin,jre/lib,lib}
  sed -e "s,@fake_java_version@,$3,g" \
    -e "s,@fake_java_build@,$4,g" \
    -e "s,@fake_openjdk@,$2,g" \
    "$SHUNITDIR/fakejava" > "$1/bin/java"
    cp "$1/bin/java" "$1/jre/bin/java"
    chmod 755 "$1/bin/java" "$1/jre/bin/java"
}

# runCommand <project> @cmd
runCommand() {
	__project="$1"; shift
	__outputdir="$(get_testdir "$__project")"

	__tmpfile="$__outputdir/output.$$"
	# shellcheck disable=SC2154
	if [ -n "${_shunit_test_}" ]; then
		__tmpfile="$__outputdir/output.${_shunit_test_}"
	fi
	touch "$__tmpfile.tmp"

	echo "running:" "$@" >>"$__tmpfile"
	HOME="${__outputdir}" "$@" >"$__tmpfile.tmp" 2>&1
	ret=$?
	cat "$__tmpfile.tmp" >> "$__tmpfile"
	cat "$__tmpfile.tmp"
	rm -f "$__tmpfile.tmp"
	return $ret
}


PROJECTDIR="$(find_projectdir)"
TARGETDIR="$PROJECTDIR/target"

export PROJECTDIR TARGETDIR
