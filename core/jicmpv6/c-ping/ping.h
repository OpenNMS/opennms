/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 *     along with OpenNMS(R).  If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information contact: 
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
#ifndef _PING_H
#define _PING_H

#include "stdhdr.h"

#ifdef HAVE_WINSOCK2_H
# ifndef HAVE_STDINT_H
   typedef u_int in_addr_t;
   typedef u_int64 uint64_t;
# endif

# ifdef __MINGW32__
#  ifdef HAVE_STDINT_H
    typedef u_int in_addr_t;
#  endif
# else
   /* Visual Studio */
#  define close closesocket
#  define snprintf _snprintf
#  pragma warning(disable: 4996)
# endif
#endif

/**
 * Macros for doing byte swapping
 */


#if defined(HAVE_STRUCT_IP)
typedef struct ip iphdr_t;
#elif defined(HAVE_STRUCT_IPHDR)
typedef struct iphdr iphdr_t;
#else
# error "not sure how to get an IP header struct on this platform!"
#endif

#if defined(HAVE_STRUCT_ICMP)
typedef struct icmp icmphdr_t;
#elif defined(HAVE_STRUCT_ICMPHDR)
typedef struct icmphdr icmphdr_t;
#else
# error "not sure how to get an ICMP header struct on this platform!"
#endif

/**
 * Winsock uses SOCKET, which is a special kind of Windows
 * HANDLE object, not just an int
 **/

#ifdef __WIN32__
#define onms_socket SOCKET
#else
#define onms_socket int
#define INVALID_SOCKET -1
#define SOCKET_ERROR -1
#endif

#endif // _PING_H
