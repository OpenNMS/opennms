#!/bin/bash

VERSION_BUILD_BUILD_CLASSPATH='1.1.1.1'
PACKAGES="$PACKAGES BUILD_CLASSPATH"

# build_classpath ()
#   input : a list of targets
#           dir:<directory>
#           jar:<jarname>  (without the .jar)
#           cp:<classpath>
#   output: a classpath

for dir in $OPENNMS_HOME/lib/scripts $PREFIX/tools/infrastructure; do
	if [ -f $dir/find_jarfile.sh ]; then
		. $dir/find_jarfile.sh
	fi
done

build_classpath () {
	local CP

	for target in "$@"; do
		local TYPE=`echo "$target" | awk -F: '{print $1}'`
		local VAR=` echo "$target" | sed -e "s#^${TYPE}:##"`

		if [ -n "$VAR" ]; then
			case "$TYPE" in
				dir)
					CP="$CP:$VAR"
					;;
				jar)
					CP="$CP:`find_jarfile $VAR`"
					;;
				jardir)
					# some shells just put the "*.jar" literally in
					# if there's no files :P
					if [ `ls $VAR/*.jar 2>/dev/null | wc -l` -gt 0 ]; then
						for jar in $VAR/*.jar; do
							CP="$CP:$jar"
						done
					fi
					;;
				cp)
					CP="$CP:$VAR"
					;;
				*)
					echo "build_classpath: unknown type: $TYPE"
					;;
			esac
		fi

	done

	CP=`echo "$CP" | sed -e 's#^:*##'`
	#CP=`echo "$CP" | sed -e "s,${PREFIX}/,./,g"`
	echo "$CP"
	return

}

