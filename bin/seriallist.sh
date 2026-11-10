#!/bin/sh

MYDIR=`dirname $0`
TOPDIR=`cd $MYDIR; pwd`

find "$TOPDIR" -name \*.java | grep -v /target/ | grep -v src/test/java | grep org/opennms | while read FILE; do
	RELATIVEFILE=`echo "$FILE" | sed -e "s,$TOPDIR/,," -e "s,.*/src/main/java/,," | head -n 1`
	VERSION=`grep "serialVersionUID" "$FILE" | grep long | grep static | grep final | sed -e 's,^.*serialVersionUID[	 ]*=[	 ]*,,' -e 's,L*[	 ]*;.*$,,' | head -n 1`
	if [ -n "$RELATIVEFILE" ] && [ -n "$VERSION" ] && [ x"$RELATIVEFILE" != x"" ]; then
		printf -- "$RELATIVEFILE\t$VERSION\n"
	fi
done | sort -u
