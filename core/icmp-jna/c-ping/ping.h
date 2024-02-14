/**
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
