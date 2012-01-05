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
#include <config.h>
#include "stdhdr.h"

int main(int argc, char** argv)
{
  printf("\n--- socket constants ---\n");
  printf("SOCK_DGRAM = %d\n", SOCK_DGRAM);
  printf("SOCK_RAW = %d\n", SOCK_RAW);

  printf("\n--- ipv4 constants ---\n");
  printf("AF_INET = %d\n", AF_INET);
  printf("PF_INET = %d\n", PF_INET);
  printf("IPPROTO_ICMP = %d\n", IPPROTO_ICMP);
  printf("IPPROTO_UDP = %d\n", IPPROTO_UDP);

  printf("\n--- ipv6 constants ---\n");
  printf("AF_INET6 = %d\n", AF_INET6);
  printf("PF_INET6 = %d\n", PF_INET6);
  printf("IPPROTO_ICMPV6 = %d\n", IPPROTO_ICMPV6);

  printf("\n--- sockaddr sizes ---\n");
  printf("sizeof(sockaddr_in) = %ld\n", sizeof(sockaddr_in));
  printf("sizeof(sockaddr_in6) = %ld\n", sizeof(sockaddr_in6));
  printf("\n");

  return 0;
}
 

