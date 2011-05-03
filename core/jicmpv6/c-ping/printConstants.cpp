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
 

