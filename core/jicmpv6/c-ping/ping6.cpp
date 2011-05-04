#include <config.h>
#include "ping.h"

#define PKT_LEN 80

void doReceive(onms_socket sock) {
  sockaddr_in6 addr;
  socklen_t len = sizeof(sockaddr_in6);
  //socklen_t len = 28;

  char buf[1024];
  char addrStr[INET6_ADDRSTRLEN];

  int n = recvfrom(sock, buf, 1024, 0, (sockaddr*)(&addr), &len);

  icmp6_hdr* pkt = (icmp6_hdr*)buf;

  inet_ntop(AF_INET6, &(addr.sin6_addr), addrStr, INET6_ADDRSTRLEN);
  printf("%d bytes from %s: icmp6_id=%d icmp6_seq=%d\n", n, addrStr, pkt->icmp6_id, pkt->icmp6_seq);
  

  close(sock);
  exit(0);
}
 
void doSend(onms_socket sock, const char* dest) {

  sockaddr_in6 addr;
  char buf[PKT_LEN];
  icmp6_hdr* pkt = (icmp6_hdr*)buf;
  //int len = sizeof(sockaddr_in6);
  int len = 28;

  for(int i = 0; i < PKT_LEN; i++) {
    buf[i] = i;
  }

  addr.sin6_family = AF_INET6;
  
  if (inet_pton(AF_INET6, dest, &(addr.sin6_addr)) < 0) {
    perror("main: error converting localhost to addr");
    exit(2);
  }

  pkt->icmp6_type = ICMP6_ECHO_REQUEST;
  pkt->icmp6_code = 0;
  pkt->icmp6_cksum = 0; /* let the OS compute is */
  pkt->icmp6_id = 2345;
  pkt->icmp6_seq = 1;

  sendto(sock, buf, PKT_LEN, 0, (sockaddr *)&addr, len);

  for(int i = 0; i < 16; i++) {
    if (i > 0 && 0 == i % 2) {
      printf(":");
    }
    printf("%02x", addr.sin6_addr.s6_addr[i]);
  }
  printf("\n");
  
}


int main(int argc, char** argv)
{

  if (argc < 2) {
    printf("Usage: %s <ipv6 addr>\n", argv[0]);
    exit(1);
  }

  onms_socket sock = socket(PF_INET6, SOCK_RAW, IPPROTO_ICMPV6);
  if (sock == INVALID_SOCKET) {
    perror("main: error creating socket");
    exit(1);
  }

  doSend(sock, argv[1]);
  doReceive(sock);
}
  

