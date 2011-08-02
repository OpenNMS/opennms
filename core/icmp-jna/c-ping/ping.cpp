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
#include "ping.h"



int main(int argc, char** argv)
{


  onms_socket sock = socket(PF_INET, SOCK_RAW, IPPROTO_ICMP);
  //onms_socket sock = socket(PF_INET6, SOCK_RAW, IPPROTO_ICMPV6);
  if (sock == INVALID_SOCKET) {
    perror("main: error creating socket");
    exit(1);
  }

  sockaddr_in addr;
  socklen_t len = sizeof(sockaddr_in);
  //sockaddr_in6 addr;
  //socklen_t len = sizeof(sockaddr_in6);

  char buf[1024];
  char addrStr[INET_ADDRSTRLEN];
    // char addrStr[INET6_ADDRSTRLEN]

  int n = recvfrom(sock, buf, 1024, 0, (sockaddr*)(&addr), &len);

  inet_ntop(AF_INET, &(addr.sin_addr), addrStr, INET_ADDRSTRLEN);
  printf("Received %d bytes from %s addr_len = %d\n", n, addrStr, len);

  close(sock);
  exit(0);
}
 
void doSend() {

  const char* localhost = "127.0.0.1";
  sockaddr_in addr;

  addr.sin_family = AF_INET;
  
  if (inet_aton(localhost, &(addr.sin_addr)) < 0) {
    perror("main: error converting localhost to addr");
    exit(2);
  }

  printf("%x\n", addr.sin_addr.s_addr);
  printf("%s\n", inet_ntoa(addr.sin_addr));

  
}

