#!/bin/bash

VERSION_BUILD_COMPILER_SETUP='1.2'
PACKAGES="$PACKAGES COMPILER_SETUP"

for make in gmake make; do
	MAKE=`which $make 2>&1 | grep -v "no $make in" | grep -v "not found"`
	if [ -n "$MAKE" ]; then
		break
	fi
done

for cc in gcc cc CC; do
	CC=`which $cc 2>&1 | grep -v "no $cc in" | grep -v "not found"`
	if [ -n "$CC" ]; then
		break
	fi
done

for dir in /usr/ccs/bin /usr/local/bin /usr/bin /bin; do
	if [ -x "$dir/ld" ]; then
		LD="$dir/ld"
	fi
done
case `uname` in
	Darwin|Linux|SunOS)
		# gcc is often smarter than ld  ;)
		LD="$CC"
		;;
esac

for dir in /usr/local/pgsql/lib /usr/local/lib /usr/lib /sw/lib; do
	if [ -f "$dir/libpq.so" ] || [ -f "$dir/libpq.dylib" ] || [ -f "$dir/libpq.so.2" ]; then
		PG_LIBDIR="$dir"
		break
	fi
done

for dir in /usr/local/pgsql/include /usr/local/include /usr/include /sw/include; do
	if [ -f "$dir/postgres.h" ] || [ -f "$dir/server/postgres.h" ]; then
		PG_INCDIR="$dir"
		break
	elif [ -f "$dir/pgsql/postgres.h" ] || [ -f "$dir/pgsql/server/postgres.h" ]; then
		PG_INCDIR="$dir/pgsql"
		break
	elif [ -f "$dir/postgresql/postgres.h" ] || [ -f "$dir/postgresql/server/postgres.h" ]; then
		PG_INCDIR="$dir/postgresql"
		break
	fi
done

for dir in /usr/local/lib /usr/local/rrdtool-*/lib /usr/lib /sw/lib; do
	if [ -f "$dir/librrd.so" ]; then
		RRD_LIBDIR="$dir"
		break
	fi
	if [ -f "$dir/librrd.a" ]; then
		RRD_LIBDIR="$dir"
		break
	fi
done

for dir in /usr/local/include /usr/local/rrdtool-*/include /usr/include /sw/include; do
	if [ -f "$dir/rrd.h" ]; then
		RRD_INCDIR="$dir"
		break
	fi
done

DEFINES="$DEFINES -Dcompile.make=$MAKE -Dcompile.cc=$CC -Dcompile.ld=$LD \
	-Dcompile.postgresql.lib=$PG_LIBDIR -Dcompile.postgresql.include=$PG_INCDIR \
	-Dcompile.rrdtool.lib=$RRD_LIBDIR -Dcompile.rrdtool.include=$RRD_INCDIR"

