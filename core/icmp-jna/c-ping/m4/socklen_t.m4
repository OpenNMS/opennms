dnl ****************************************
dnl **** based on the KDE socklen_t test ***
dnl ****************************************

dnl Check for the type of the third argument of getsockname
AC_DEFUN([ONMS_CHECK_SOCKLEN_T],
[
	AC_CHECK_HEADERS([sys/types.h sys/socket.h winsock2.h ws2tcpip.h])
	AC_MSG_CHECKING(for socklen_t)
	AC_CACHE_VAL(onms_cv_socklen_t,
	[
		onms_cv_socklen_t=no
		AC_TRY_COMPILE([
			#ifdef HAVE_SYS_TYPES_H
			#include <sys/types.h>
			#endif

			#ifdef HAVE_SYS_SOCKET_H
			#include <sys/socket.h>
			#endif

			#ifdef HAVE_WINSOCK2_H
			#include <winsock2.h>
			#endif

			#ifdef HAVE_WS2TCPIP_H
			#include <ws2tcpip.h>
			#endif
		],
		[
			socklen_t len;
			getpeername(0,0,&len);
		],
		[
			onms_cv_socklen_t=yes
			onms_cv_socklen_t_equiv=socklen_t
		])
	])
	AC_MSG_RESULT($onms_cv_socklen_t)
	if test $onms_cv_socklen_t = no; then
		AC_MSG_CHECKING([for socklen_t equivalent for socket functions])
		AC_CACHE_VAL(onms_cv_socklen_t_equiv,
		[
			onms_cv_socklen_t_equiv=int
			for t in int size_t unsigned long "unsigned long"; do
				AC_TRY_COMPILE([
					#ifdef HAVE_SYS_TYPES_H
					#include <sys/types.h>
					#endif
		
					#ifdef HAVE_SYS_SOCKET_H
					#include <sys/socket.h>
					#endif
		
					#ifdef HAVE_WINSOCK2_H
					#include <winsock2.h>
					#endif
		
					#ifdef HAVE_WS2TCPIP_H
					#include <ws2tcpip.h>
					#endif
				],
				[
					$t len;
					getpeername(0,0,&len);
				],
				[
					onms_cv_socklen_t_equiv="$t"
					break
				])
			done
		])
		AC_MSG_RESULT($onms_cv_socklen_t_equiv)
	fi
	AC_DEFINE_UNQUOTED(onms_socklen_t, $onms_cv_socklen_t_equiv, [type to use in place of socklen_t if not defined])
])
