#!/bin/bash

VERSION_BUILD_HANDLE_PROPERTIES='1.2'
PACKAGES="$PACKAGES HANDLE_PROPERTIES"

# find_property_file()
#   input : none
#   output: the property file to use for overrides

find_property_file () {

	if [ -n "$PROPERTY_OVERRIDE" ] && [ -f "$PROPERTY_OVERRIDE" ]; then
		echo "$PROPERTY_OVERRIDE"
		return
	elif [ -f "~/.opennms-global.properties" ]; then
		echo "~/.opennms-global.properties"
		return
	elif [ -f "~/.bb-global.properties" ]; then
		echo "~/.bb-global.properties"
		return
	else
		return 1
	fi

}
 
list_properties() {
	for prop in `cat build.properties | grep -v -E '^#' | grep -v -E '^$' | awk '{print $1}'`; do
		echo -e "$prop \c"
	done
	echo ""
}

# get_relative_property()
#   input : the property to get
#   output: it's value, stripped of the base directory

get_relative_property() {
	local PROPERTY="$1"
	local RETPROP=`get_property "$PROPERTY"`

	RETPROP=`echo "$RETPROP" | sed -e s'#'$PREFIX'/##'`
	echo "$RETPROP"
}

# get_property()
#   input : the property to get
#   output: it's value

get_property () {
	local PROPERTY="$1"
	local RECURSE="$2"

	for file in $PROPERTY_OVERRIDE ~/.opennms-global.properties ~/.bb-global.properties ${PREFIX}/build.properties; do
		if [ -f "$file" ]; then
			MATCH=`cat $file | grep "^${PROPERTY}[[:space:]]*="`
			if [ -n "$MATCH" ]; then
				break
			fi
		fi
	done

	if [ -n "$MATCH" ]; then
		if parse_property "$MATCH" >/dev/null 2>&1; then
			VALUE=`parse_property "$MATCH"`
			VALUE=`echo "$VALUE" | sed -e s'#${root.source}#'"$PREFIX"'#g'`
			if [ -z "$RECURSE" ]; then
				BUILD=`PROPERTY_OVERRIDE="" get_property root.build 1`
				VALUE=`echo "$VALUE" | sed -e s'#${root.build}#'"$BUILD"'#g'`
				INSTALL=`PROPERTY_OVERRIDE="" get_property root.install 1`
				VALUE=`echo "$VALUE" | sed -e s'#${root.install}#'"$INSTALL"'#g'`
			fi

			#PROPERTY=`echo $PROPERTY | sed -e 's#[^[:alnum:]]#_#g'`
			#eval "'ANT_${PROPERTY}'=$VALUE"
			echo "$VALUE"
			return
		else
			return 1
		fi
	fi

	return 1
}

# parse_property()
#   input : a line of text from a property file
#   output: the value of the property in that line

parse_property () {
	local LINE="$1"

	KEY=`echo "$LINE" | cut -d= -f1`
	VALUE=`echo "$LINE" | cut -d= -f2-`

	if [ -z "$VALUE" ] || [ "$KEY" = "$VALUE" ]; then
		return 1
	else
		VALUE=`echo "$VALUE" | sed -e 's#^[[:space:]]*##' -e 's#[[:space:]]*$##'`
		echo "$VALUE"
		return
	fi
}

