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
 

