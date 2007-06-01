#!/bin/sh

[ -z "$ACLOCAL"    ] && ACLOCAL=aclocal
[ -z "$AUTOHEADER" ] && AUTOHEADER=autoheader
[ -z "$LIBTOOLIZE" ] && LIBTOOLIZE=glibtoolize
[ -z "$AUTOCONF"   ] && AUTOCONF=autoconf
[ -z "$AUTOMAKE"   ] && AUTOMAKE=automake

$ACLOCAL
$LIBTOOLIZE --automake --copy --force
$AUTOHEADER --force
$AUTOCONF --force
$AUTOMAKE --add-missing --copy
