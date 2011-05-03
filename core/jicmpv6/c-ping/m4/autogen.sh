#!/bin/sh

[ -z "$ACLOCAL"    ] && ACLOCAL=aclocal
[ -z "$AUTOHEADER" ] && AUTOHEADER=autoheader
[ -z "$LIBTOOLIZE" ] && LIBTOOLIZE=glibtoolize
[ -z "$AUTOCONF"   ] && AUTOCONF=autoconf
[ -z "$AUTOMAKE"   ] && AUTOMAKE=automake
[ -z "$AUTORECONF" ] && AUTORECONF=autoreconf

[ -x "`which libtoolize 2>/dev/null`" ] && LIBTOOLIZE=libtoolize

$ACLOCAL --force -I m4
$LIBTOOLIZE --automake --copy --force
$AUTOCONF --force
$AUTOHEADER --force
$AUTOMAKE --add-missing --copy --force-missing
