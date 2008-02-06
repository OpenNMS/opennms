#!/bin/bash

VERSION_BUILD_CHECK_TOOLS='1.1.1.1'
PACKAGES="$PACKAGES CHECK_TOOLS"

# check_tools
#   input : none
#   output: true if tools are up to date, false if not

check_tools () {
	builddir=`get_property root.build`
	tool_classdir=`PROPERTY_OVERRIDE="$PREFIX/tools/build.properties" get_property opennms.tools.classes`
	tool_classdir=`echo "$tool_classdir" | sed -e s'#${root.build}#'"$builddir"'#'`

	NEW_FILES=0

#	for dir in "ant" "if"; do
	for dir in "if"; do
		for file in `find "${PREFIX}/tools/${dir}" -name \*.java`; do
			file=`echo "$file" | sed -e s"#^${PREFIX}/tools/${dir}/##" -e s'#\.java$##'`
			if [ "${PREFIX}/tools/${dir}/${file}.java" -nt "${tool_classdir}/${file}.class" ]; then
				NEW_FILES=$(($NEW_FILES+1))
			elif [ ! -f "${tool_classdir}/${file}.class" ]; then
				NEW_FILES=$(($NEW_FILES+1))
			fi
		done
	done

	return $NEW_FILES

}

