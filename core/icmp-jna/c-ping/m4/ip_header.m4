AC_DEFUN([_ONMS_FIND_IP_HEADERS], [
	if test -z "$_ONMS_TESTED_IP_HEADERS"; then
		AC_CHECK_HEADERS([sys/types.h netinet/in.h netinet/in_systm.h netinet/ip.h netinet/ip_icmp.h netinet/icmp6.h winsock2.h ws2tcpip.h win32/icmp.h], [], [], [
			#ifdef HAVE_SYS_TYPES_H
			#include <sys/types.h>
			#endif

			#ifdef HAVE_WINSOCK2_H
			#include <winsock2.h>
			#endif

			#ifdef HAVE_WINSOCK2_H
			#include <winsock2.h>
			#endif

			#ifdef HAVE_WS2TCPIP_H
			#include <ws2tcpip.h>
			#endif

			#ifdef HAVE_NETINET_IN_H
			#include <netinet/in.h>
			#endif

			#ifdef HAVE_NETINET_IN_SYSTM_H
			#include <netinet/in_systm.h>
			#endif

			#ifdef HAVE_NETINET_IP_H
			#include <netinet/ip.h>
			#endif

			#ifdef HAVE_NETINET_IP_ICMP_H
			#include <netinet/ip_icmp.h>
			#endif
])
		_ONMS_TESTED_IP_HEADERS=yes
	fi
])

AC_DEFUN([_ONMS_TRY_COMPILE], [
		_ONMS_FIND_IP_HEADERS
		AC_TRY_COMPILE(
			[
				#ifdef HAVE_SYS_TYPES_H
				#include <sys/types.h>
				#endif

				#ifdef HAVE_NETINET_IN_H
				#include <netinet/in.h>
				#endif

				#ifdef HAVE_NETINET_IN_SYSTM_H
				#include <netinet/in_systm.h>
				#endif

				#ifdef HAVE_NETINET_IP_H
				#include <netinet/ip.h>
				#endif

				#ifdef HAVE_NETINET_IP_ICMP_H
				#include <netinet/ip_icmp.h>
				#endif

				#ifdef HAVE_WINSOCK2_H
				#include <winsock2.h>
				#endif

				#ifdef HAVE_WS2TCPIP_H
				#include <ws2tcpip.h>
				#endif

				#ifdef __WIN32__
				#ifdef HAVE_WIN32_ICMP_H
				#include "win32/icmp.h"
				#endif
				#endif

				$1
			],
			[ $2 ],
			[ $3 ],
			[ $4 ]
		)
	]
)

dnl check for a struct based on the first argument
AC_DEFUN([ONMS_CHECK_IP_STRUCT],
	[
		_ONMS_FIND_IP_HEADERS
		AC_CHECK_TYPE(
			[struct $1],
			[
				AC_DEFINE(
					AS_TR_CPP([HAVE_STRUCT_$1]),
					[1],
					[struct $1 needed for IP headers]
				)
			],
			[],
			[
				#ifdef HAVE_SYS_TYPES_H
				#include <sys/types.h>
				#endif

				#ifdef HAVE_NETINET_IN_H
				#include <netinet/in.h>
				#endif

				#ifdef HAVE_NETINET_IN_SYSTM_H
				#include <netinet/in_systm.h>
				#endif

				#ifdef HAVE_NETINET_IP_H
				#include <netinet/ip.h>
				#endif

				#ifdef HAVE_NETINET_IP_ICMP_H
				#include <netinet/ip_icmp.h>
				#endif

				#ifdef HAVE_WINSOCK2_H
				#include <winsock2.h>
				#endif

				#ifdef HAVE_WS2TCPIP_H
				#include <ws2tcpip.h>
				#endif

				#ifdef __WIN32__
				#ifdef HAVE_WIN32_ICMP_H
				#include "win32/icmp.h"
				#endif
				#endif

				$2
			]
		)
	]
)

dnl check for an entry in the IP struct
AC_DEFUN([ONMS_CHECK_IP_STRUCT_ENTRY],
	[
		AC_MSG_CHECKING([for ip->$2])
		_ONMS_TRY_COMPILE(
			[],
			[
				#if defined(HAVE_STRUCT_IP)
				struct ip ip;
				#elif defined(HAVE_STRUCT_IPHDR)
				struct iphdr ip;
				#endif
			
				ip.$2 = 0;
			],
			[
				AC_DEFINE(
					AS_TR_CPP([ONMS_IP_$1]),
					[$2],
					[the $2 entry in the IP struct]
				)
				AC_MSG_RESULT(yes)
			],
			AC_MSG_RESULT(no)
		)
	]
)

dnl check for an entry in the ICMP struct
AC_DEFUN([ONMS_CHECK_ICMP_STRUCT_ENTRY],
	[
		AC_MSG_CHECKING([for icmp->$2])
		_ONMS_TRY_COMPILE(
			[],
			[
				#if defined(HAVE_STRUCT_ICMP)
				struct icmp icmp;
				#elif defined(HAVE_STRUCT_ICMPHDR)
				struct icmphdr icmp;
				#endif
			
				icmp.$2 = 0;
			],
			[
				AC_DEFINE(
					AS_TR_CPP([ICMP_$1]),
					[$2],
					[the $2 entry in the ICMP struct]
				)
				AC_MSG_RESULT(yes)
			],
			AC_MSG_RESULT(no)
		)
	]
)
