#                                               -*- Autoconf -*-
# Process this file with autoconf to produce a configure script.

AC_PREREQ(2.59)
AC_INIT([c-ping], [1.0], [http://www.opennms.org])

RELEASE=1
AC_SUBST([RELEASE])

AC_CONFIG_MACRO_DIR([m4])
AC_CONFIG_HEADER([config.h])
AM_INIT_AUTOMAKE([1.9 foreign])
AC_ENABLE_STATIC([no])
AM_PATH_CPPUNIT(1.10.2)

# Checks for programs.
AC_PROG_CC
AC_PROG_CXX
AC_PROG_LN_S
AC_PROG_LIBTOOL
AC_PROG_INSTALL
AC_C_BIGENDIAN
ONMS_CHECK_SUNCC
ONMS_SET_CC_WARNING_CFLAGS

AC_HEADER_STDC

AC_CHECK_HEADERS([arpa/inet.h netinet/icmp.h])
AC_CHECK_HEADERS([sys/types.h AvailabilityMacros.h architecture/byte_order.h errno.h malloc.h netdb.h stdlib.h sys/byteorder.h sys/time.h], [], [], [
#ifdef HAVE_SYS_TYPES_H
#include <sys/types.h>
#endif
])
AC_SEARCH_LIBS([socket], [socket resolv nsl])
AC_SEARCH_LIBS([sendto], [socket resolv nsl])
AC_SEARCH_LIBS([recvfrom], [socket resolv nsl])
AC_SEARCH_LIBS([close], [socket resolv nsl])
AC_SEARCH_LIBS([inet_aton], [socket resolv nsl])
AC_SEARCH_LIBS([inet_ntoa], [socket resolv nsl])
AC_SEARCH_LIBS([inet_pton], [socket resolv nsl])
AC_SEARCH_LIBS([inet_ntop], [socket resolv nsl])

# check for socklen_t
ONMS_CHECK_SOCKLEN_T

# Checks for typedefs, structures, and compiler characteristics.
AC_C_CONST

ONMS_CHECK_IP_STRUCT([ip])
ONMS_CHECK_IP_STRUCT([iphdr])
ONMS_CHECK_IP_STRUCT([icmp],    [
#ifdef HAVE_NETINET_IP_ICMP_H
# include <netinet/ip_icmp.h>
#endif
])
ONMS_CHECK_IP_STRUCT([icmphdr], [
#ifdef HAVE_NETINET_IP_ICMP_H
# include <netinet/ip_icmp.h>
#endif
])

ONMS_CHECK_IP_STRUCT_ENTRY([HL], [ip_hl])
ONMS_CHECK_IP_STRUCT_ENTRY([HL], [ihl])
ONMS_CHECK_ICMP_STRUCT_ENTRY([TYPE], [icmp_type])
ONMS_CHECK_ICMP_STRUCT_ENTRY([TYPE], [type])
ONMS_CHECK_ICMP_STRUCT_ENTRY([CHECKSUM], [icmp_cksum])
ONMS_CHECK_ICMP_STRUCT_ENTRY([CHECKSUM], [checksum])

AC_CHECK_TYPE([struct timeval],
	[],
	[ AC_MSG_ERROR([cannot find struct timeval]) ],
	[
#ifdef HAVE_SYS_TIME_H
#include <sys/time.h>
#endif

#ifdef HAVE_WINSOCK2_H
#include <winsock2.h>
#endif
])

gl_BYTESWAP

ARCH_PKG="$host_cpu"
AC_SUBST([ARCH_PKG])

UNAME_R="`uname -r`"
AC_SUBST([UNAME_R])
UNAME_S="`uname -s`"
AC_SUBST([UNAME_S])

on_win32=no
case $host_os in
	mingw32*)
		on_win32=yes
		;;
esac

AM_CONDITIONAL([WIN32], [test x$on_win32 = xyes])

AC_CONFIG_FILES([Makefile])
AC_OUTPUT
