//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//  
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software 
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
// 
// For more information contact: 
//	Brian Weaver	<weave@opennms.org>
//	http://www.opennms.org/
//
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netinet/ip.h>
#include <netinet/udp.h>
#include <netdb.h>
#include <arpa/inet.h>
#include <getopt.h>
#include <string.h>

/**
 * This defines the basic packet that is sent out
 * in our spoofed packet
 */
struct packet
{
	struct iphdr	m_ip;
	struct udphdr	m_udp;
#ifndef ZERO_LENGTH_PACKET
	unsigned char	m_data[4];
#endif
};
typedef struct packet packet_t;

#if 0
/**
 * This function is used to perfrom the one's compliment
 * checksum for basic data verification.
 * 
 * @param p	The poitner to the buffer
 * @parma x	The number of bytes in the buffer
 * @return	The one's compliment checksum
 *
 */
unsigned short cksum(unsigned short *p, int x)
{
	unsigned short ans;
	long sum = 0;

	while(x > 0)
	{
		sum += *p++;
		x -= 2;
	}
	if(x == 1)
		sum += ((unsigned short) *((unsigned char *)p)) << 8;

	sum  = (sum >> 16) + (sum & 0xffff);
	sum += (sum >> 16);

	return ~((unsigned short)sum);
}
#endif

packet_t * build_packet(struct sockaddr_in *srcip, struct sockaddr_in *dstip, packet_t * pkt)
{
	pkt->m_ip.version = IPVERSION;
	pkt->m_ip.ihl	  = sizeof(pkt->m_ip) / 4;
	pkt->m_ip.tos	  = 0;
	/*
	 * The packet length is filled in by the sendto() method
	 *
	pkt->m_ip.tot_len = htons(sizeof(pkt));
	 */
	pkt->m_ip.id	  = htons((unsigned short)0x0bad);
	pkt->m_ip.frag_off= 0;
	pkt->m_ip.ttl	  = 8;
	pkt->m_ip.protocol= IPPROTO_UDP;
	pkt->m_ip.check   = 0;
	pkt->m_ip.saddr   = srcip->sin_addr.s_addr; // already converted
	pkt->m_ip.daddr   = dstip->sin_addr.s_addr; // already converted

	pkt->m_udp.source = srcip->sin_port;	// already converted
	pkt->m_udp.dest   = dstip->sin_port; 	// already converted
#ifndef ZERO_LENGTH_PACKET
	pkt->m_udp.len    = htons(sizeof(pkt->m_udp) + sizeof(pkt->m_data));
#else
	pkt->m_udp.len	  = htons(sizeof(pkt->m_udp));
#endif
	pkt->m_udp.check  = 0;

#ifndef ZERO_LENGTH_PACKET
	pkt->m_data[0] = 0; /* cause it's a magic number! */
	pkt->m_data[1] = 0;
	pkt->m_data[2] = 7;
	pkt->m_data[3] = 0;
#endif

	/*
	 * The check sums are computed by the IP stack for the IP
	 * header. The UDP header doesn't need one.
	 */
	/*pkt->m_ip.check = cksum((unsigned short *) &(pkt->m_ip), sizeof(pkt->m_ip));*/

	return pkt;
}

/*
 * Options
 * 	-s <host>:<port>
 * 	-d <host>:<port>
 *
 */
int main(int argc, char **argv)
{
	int 			iRC;
	struct sockaddr_in	addr[2];
	char *			s;
	packet_t		pkt;
	int 			err = 0;
	int			sock= 0;

	/**
	 * clear out the address structure
	 */
	memset(&addr, 0, sizeof(addr));
	addr[0].sin_family = AF_INET;
	addr[1].sin_family = AF_INET;

	/**
	 * Read the options
	 */
	while((iRC = getopt(argc, argv, "s:d:")) != -1)
	{
		switch(iRC)
		{
		case 's':
			s = strtok(optarg, ":");
			if(s != NULL)
			{
				inet_aton(s, &(addr[0].sin_addr));
				s = strtok(NULL, ":");
				if(s != NULL)
				{
					addr[0].sin_port = (unsigned short)strtoul(s, NULL, 10);
				}
				/* addr[0].sin_addr.s_addr = htonl(addr[0].sin_addr.s_addr); */
				addr[0].sin_port = htons(addr[0].sin_port);
			}
			break;

		case 'd':
			s = strtok(optarg, ":");
			if(s != NULL)
			{
				inet_aton(s, &(addr[1].sin_addr));
				s = strtok(NULL, ":");
				if(s != NULL)
				{
					addr[1].sin_port = (unsigned short)strtoul(s, NULL, 10);
				}
				/* addr[1].sin_addr.s_addr = htonl(addr[1].sin_addr.s_addr); */
				addr[1].sin_port = htons(addr[1].sin_port);
			}
			break;

		case '?':
			fprintf(stderr, "Invalid option %c\n", optopt);
			++err;
			break;

		case ':':
			fprintf(stderr, "Missing argument for option %c\n", optopt);
			++err;
			break;

		default:
			fprintf(stderr, "Huh?\n");
			++err;
			break;
		}
	}

	/**
	 * Check for a vaild source address
	 */
	if(addr[0].sin_addr.s_addr == 0 || addr[0].sin_addr.s_addr == (unsigned long)-1)
	{
		fprintf(stderr, "Source address is not set\n");
		++err;
	}

	/**
	 * Check for a valid destination address
	 */
	if(addr[1].sin_addr.s_addr == 0 || addr[1].sin_addr.s_addr == (unsigned long)-1)
	{
		fprintf(stderr, "Destination address is not set\n");
		++err;
	}

	/**
	 * Any errors, exit
	 */
	if(err != 0)
		exit(err);


	/**
	 * Allocate a raw UDP socket
	 */
	sock = socket(AF_INET, SOCK_RAW, IPPROTO_UDP);
	if(sock < 0)
	{
		perror("socket (raw)");
		exit(1);
	}
	
	/**
	 * Set the 'header included' option for the socket
	 */
	iRC = 1;
	iRC = setsockopt(sock, SOL_IP, IP_HDRINCL, &iRC, sizeof(iRC));
	if(iRC < 0)
	{
		perror("setsockopt (HDRINCL)");
		exit(1);
	}

	/**
	 * Build the packet
	 */
	build_packet(&addr[0], &addr[1], &pkt);

	/**
	 * Send the packet, this really only works on the local
	 * host. For some reason it's never really pushed onto
	 * the network (well my tests never had it happen).
	 */
	iRC = sendto(sock,
		     (const void *)&pkt,
		     sizeof(pkt),
		     0,
		     (struct sockaddr *)&addr[1],
		     sizeof(addr[1]));
	if(iRC < 0)
	{
		perror("sendto");
	}
	close(sock);
	return (iRC == sizeof(pkt));
}

