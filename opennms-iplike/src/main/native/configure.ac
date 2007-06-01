AC_INIT([iplike],[1.0])
AC_CONFIG_HEADERS([config.h])

AM_INIT_AUTOMAKE([1.9 foreign])

AC_ENABLE_SHARED([yes])
AC_ENABLE_STATIC([no])

AC_HEADER_STDC

AC_PROG_CC
AC_PROG_CXX
AC_PROG_LN_S
AC_PROG_LIBTOOL

AC_CANONICAL_HOST

dnl
dnl lovingly stolen from postgis' autoconf macros
dnl

AC_SUBST(PGCONFIG)
PGCONFIG=
AC_ARG_WITH(pgsql, 
[  --with-pgsql[[=ARG]]      build for a specific pgsql version
                          [[ARG=path to pg_config]]],
	if test "$with_pgsql" != "no" -a "$with_pgsql" != "yes"; then
		if test ! -f "$with_pgsql"; then
			AC_MSG_ERROR([Invalid argument to --with-pgsql]);
		fi
		PGCONFIG="$with_pgsql"
	fi
)
if test -z "$PGCONFIG"; then
	AC_PATH_PROG([PGCONFIG], [pg_config])
fi
if test -z "$PGCONFIG"; then
	AC_MSG_ERROR([PostgreSQL is required by PostGIS]);
fi
PGFEINCLUDES="-I"`${PGCONFIG} --includedir`
PGFELIBS="-L'`${PGCONFIG} --libdir`' -lpq "

AC_SUBST(PGFEINCLUDES)
AC_SUBST(PGFELIBS)

AC_CONFIG_FILES([Makefile])

AC_OUTPUT
