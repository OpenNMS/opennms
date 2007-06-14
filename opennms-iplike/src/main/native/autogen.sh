#!/bin/sh

[ -z "$ACLOCAL"    ] && ACLOCAL=aclocal
[ -z "$AUTOHEADER" ] && AUTOHEADER=autoheader
[ -z "$LIBTOOLIZE" ] && LIBTOOLIZE=glibtoolize
[ -z "$AUTOCONF"   ] && AUTOCONF=autoconf
[ -z "$AUTOMAKE"   ] && AUTOMAKE=automake

[ -x "`which libtoolize 2>/dev/null`" ] && LIBTOOLIZE=libtoolize

$ACLOCAL
$LIBTOOLIZE --automake --copy --force
$AUTOHEADER --force
$AUTOCONF --force
$AUTOMAKE --add-missing --copy
