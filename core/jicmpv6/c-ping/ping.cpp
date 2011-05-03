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

