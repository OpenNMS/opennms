/*
 This file is part of the OpenNMS(R) Application.

 OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
 OpenNMS(R) is a derivative work, containing both original code, included code and modified
 code that was published under the GNU General Public License. Copyrights for modified
 and included code are below.

 OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.

 Modifications:

 2004 Oct 27: Handle Darwin 10.2 gracefully.
 2003 Sep 07: More Darwin tweaks.
 2003 Apr 26: Fixes byteswap issues on Solaris x86.
 2003 Mar 25: Used unt64_t instead of unsigned long long.
 2003 Feb 15: Bugfixes for Darwin.
 2003 Feb 11: Bugfixes for Darwin.
 2003 Feb 10: Bugfixes for Darwin.
 2003 Feb 09: ICMP response time on Darwin.
 2003 Feb 02: Initial Darwin port.
 2002 Nov 26: Fixed build issues on Solaris.
 2002 Nov 13: Added response times for ICMP.

 Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

 For more information contact:
      OpenNMS Licensing       <license@opennms.org>
      http://www.opennms.org/
      http://www.opennms.com/


 Tab Size = 8

*/
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/socket.h>
#if defined(__SOLARIS__) || defined (__FreeBSD__)
# include <netinet/in_systm.h>
# endif
#endif
#if defined(__DARWIN__) 
#include <stdint.h>
# include <netinet/in_systm.h>
# include <AvailabilityMacros.h>
# ifndef MAC_OS_X_VERSION_10_3
#  define socklen_t int
# endif
#endif
#include <netinet/in.h>
#include <netinet/ip.h>
#include <netinet/ip_icmp.h> 
#include <netdb.h>
#include <errno.h>

#if defined(__FreeBSD__)
#include <sys/time.h>
#endif

#if defined(__DARWIN__)
#include <sys/time.h>
#include <architecture/byte_order.h>
#endif

#include <jni.h>

#if defined(__DARWIN__) || defined(__SOLARIS__) || defined (__FreeBSD__)
typedef struct ip iphdr_t;
typedef struct icmp icmphdr_t;
#define ihl ip_hl
#else
typedef struct iphdr iphdr_t;
typedef struct icmphdr icmphdr_t;
#endif
#pragma export on
#include "IcmpSocket.h"
#pragma export reset

/**
 * This macro is used to recover the current time
 * in milliseconds.
 */
#ifndef CURRENTTIMEMILLIS
#define CURRENTTIMEMILLIS(_dst_) \
{				\
	struct timeval tv;	\
	gettimeofday(&tv,NULL); \
	_dst_ = (uint64_t)tv.tv_sec * 1000UL + (uint64_t)tv.tv_usec / 1000UL; \
}
#endif

/** 
 * This macro is used to recover the current time
 * in microseconds
 */
#ifndef CURRENTTIMEMICROS
#define CURRENTTIMEMICROS(_dst_) \
{				\
	struct timeval tv;	\
	gettimeofday(&tv,NULL); \
	_dst_ = (uint64_t)tv.tv_sec * 1000000UL + (uint64_t)tv.tv_usec; \
}
#endif

/**
 * converts microseconds to milliseconds
 */
#ifndef MICROS_TO_MILLIS
# define MICROS_TO_MILLIS(_val_) ((_val_) / 1000UL)
#endif

/**
 * convert milliseconds to microseconds.
 */
#ifndef MILLIS_TO_MICROS
# define MILLIS_TO_MICROS(_val_) ((_val_) * 1000UL)
#endif

/**
 * This constant specifies the length of a 
 * time field in the buffer
 */
#ifndef TIME_LENGTH
# define TIME_LENGTH sizeof(uint64_t)
#endif

/**
 * Specifies the header offset and length
 */
#ifndef ICMP_HEADER_OFFSET
# define ICMP_HEADER_OFFSET 0
# define ICMP_HEADER_LENGTH 8
#endif

/** 
 * specifies the offset of the sent time.
 */
#ifndef SENTTIME_OFFSET
# define SENTTIME_OFFSET (ICMP_HEADER_OFFSET + ICMP_HEADER_LENGTH)
#endif

/**
 * Sepcifies the offset of the received time.
 */
#ifndef RECVTIME_OFFSET
# define RECVTIME_OFFSET (SENTTIME_OFFSET + TIME_LENGTH)
#endif

/**
 * Specifies the offset of the thread identifer
 */
#ifndef THREADID_OFFSET
# define THREADID_OFFSET (RECVTIME_OFFSET + TIME_LENGTH)
#endif

/**
 * Specifies the offset of the round trip time
 */
#ifndef RTT_OFFSET
# define RTT_OFFSET (THREADID_OFFSET + TIME_LENGTH)
#endif

/**
 * specifies the magic tag and the offset/length of
 * the tag in the header.
 */
#ifndef OPENNMS_TAG
# define OPENNMS_TAG "OpenNMS!"
# define OPENNMS_TAG_LEN 8
# define OPENNMS_TAG_OFFSET (RTT_OFFSET + TIME_LENGTH)
#endif

/**
 * Macros for doing byte swapping
 */

#ifndef ntohll
# if defined(__DARWIN__)
#  define ntohll(_x_) NXSwapBigLongLongToHost(_x_)
# elif defined(__SOLARIS__)
#  if defined(_LITTLE_ENDIAN)
#   define ntohll(_x_) ((((uint64_t)ntohl((_x_) >> 32)) & 0xffffffff) | (((uint64_t)ntohl(_x_)) << 32))
#   define htonll(x) ntohll(x)
#  else
#   define ntohll(_x_) (_x_)
#  endif
# elif defined(__FreeBSD__)
#  define  ntohll(_x_) __bswap_64(_x_)
# else
#  define ntohll(_x_) __bswap_64(_x_)
# endif
#endif
#ifndef htonll
# if defined(__DARWIN__)
#  define htonll(_x_) NXSwapHostLongLongToBig(_x_)
# elif defined(__SOLARIS__)
#  if defined(_LITTLE_ENDIAN)
#   define htonll(_x_) ((htonl((_x_ >> 32) & 0xffffffff) | ((uint64_t) (htonl(_x_ & 0xffffffff)) << 32)))
#  else
#   define htonll(_x_) (_x_)
#  endif
# elif defined(__FreeBSD__)
#  define  htonll(_x_) __bswap_64(_x_)
# else
#  define htonll(_x_) __bswap_64(_x_)
# endif
#endif

/**
 * This routine is used to quickly compute the
 * checksum for a particular buffer. The checksum
 * is done with 16-bit quantities and padded with
 * zero if the buffer is not aligned on a 16-bit
 * boundry.
 *
 */
static
unsigned short checksum(register unsigned short *p, register int sz)
{
	register unsigned long sum = 0;	// need a 32-bit quantity

	/*
	 * interate over the 16-bit values and 
	 * accumulate a sum.
	 */
	while(sz > 1)
	{
		sum += *p++;
		sz  -= 2;
	}

	if(sz == 1) /* handle the odd byte out */
	{
		/*
		 * cast the pointer to an unsigned char pointer,
		 * dereference and premote to an unsigned short.
		 * Shift in 8 zero bits and whalla the value
		 * is padded!
		 */
		sum += ((unsigned short) *((unsigned char *)p)) << 8;
	}

	/*
	 * Add back the bits that may have overflowed the 
	 * "16-bit" sum. First add high order 16 to low
	 * order 16, then repeat
	 */
	while(sum >> 16)
		sum = (sum >> 16) + (sum & 0xffffUL);

	sum = ~sum & 0xffffUL; 
	return sum;
}

/**
 * Opens a new raw socket that is set to send
 * and receive the ICMP protocol. The protocol
 * for 'icmp' is looked up using the function
 * getprotobyname() and passed to the newly 
 * constructed socket.
 * 
 * If the socket fails to open then a negative
 * number will be returned to the caller.
 *
 */
static int openIcmpSocket()
{
	int			fd = -1;
	struct protoent * 	proto = getprotobyname("icmp");
	if(proto != (struct protoent *)NULL)
	{
		fd = socket(AF_INET, SOCK_RAW, proto->p_proto);
	}

	return fd;
}

/**
 * This method is used to lookup the instances java.io.FileDescriptor
 * object and it's internal integer descriptor. This hidden integer
 * is used to store the opened ICMP socket handle that was 
 * allocated by the operating system.
 *
 * If the descriptor could not be recovered or has not been
 * set then a negative value is returned.
 *
 */
static jint getIcmpFd(JNIEnv *env, jobject instance)
{
	jclass	thisClass = NULL;
	jclass	fdClass   = NULL;

	jfieldID thisFdField    = NULL;
	jobject  thisFdInstance = NULL;

	jfieldID fdFdField = NULL;
	jint	 fd_value  = -1;

	/**
	 * Find the class that describes ourself.
	 */
	thisClass = (*env)->GetObjectClass(env, instance);
	if(thisClass == NULL)
		goto end_getfd;

	/**
	 * Find the java.io.FileDescriptor class
	 */
	thisFdField = (*env)->GetFieldID(env, thisClass, "m_rawFd", "Ljava/io/FileDescriptor;");
	if(thisFdField == NULL || (*env)->ExceptionOccurred(env) != NULL)
		goto end_getfd;

	(*env)->DeleteLocalRef(env, thisClass);
	thisClass = NULL;

	/**
	 * Get the instance of the FileDescriptor class from
	 * the instance of ourself
	 */
	thisFdInstance = (*env)->GetObjectField(env, instance, thisFdField);
	if(thisFdInstance == NULL || (*env)->ExceptionOccurred(env) != NULL)
		goto end_getfd;

	/**
	 * Get the class object for the java.io.FileDescriptor
	 */
	fdClass = (*env)->GetObjectClass(env, thisFdInstance);
	if(fdClass == NULL || (*env)->ExceptionOccurred(env) != NULL)
		goto end_getfd;

	/**
	 * Get the field identifier for the primitive integer
	 * that is part of the FileDescriptor class.
	 */
	fdFdField = (*env)->GetFieldID(env, fdClass, "fd", "I");
	if(fdFdField == NULL || (*env)->ExceptionOccurred(env) != NULL)
		goto end_getfd;

	(*env)->DeleteLocalRef(env, fdClass);
	fdClass = NULL;

	/**
	 * Recover the value
	 */
	fd_value = (*env)->GetIntField(env, thisFdInstance, fdFdField);

	(*env)->DeleteLocalRef(env, thisFdInstance);

end_getfd:
	/**
	 * method complete, value is -1 unless the 
	 * entire method is successful.
	 */
	return fd_value;
}

static void setIcmpFd(JNIEnv *env, jobject instance, jint fd_value)
{
	jclass	thisClass = NULL;
	jclass	fdClass   = NULL;

	jfieldID thisFdField    = NULL;
	jobject  thisFdInstance = NULL;

	jfieldID fdFdField = NULL;

	/**
	 * Find the class that describes ourself.
	 */
	thisClass = (*env)->GetObjectClass(env, instance);
	if(thisClass == NULL)
		goto end_setfd;

	/**
	 * Find the java.io.FileDescriptor class
	 */
	thisFdField = (*env)->GetFieldID(env, thisClass, "m_rawFd", "Ljava/io/FileDescriptor;");
	if(thisFdField == NULL || (*env)->ExceptionOccurred(env) != NULL)
		goto end_setfd;

	(*env)->DeleteLocalRef(env, thisClass);
	thisClass = NULL;

	/**
	 * Get the instance of the FileDescriptor class from
	 * the instance of ourself
	 */
	thisFdInstance = (*env)->GetObjectField(env, instance, thisFdField);
	if(thisFdInstance == NULL || (*env)->ExceptionOccurred(env) != NULL)
		goto end_setfd;

	/**
	 * Get the class object for the java.io.FileDescriptor
	 */
	fdClass = (*env)->GetObjectClass(env, thisFdInstance);
	if(fdClass == NULL || (*env)->ExceptionOccurred(env) != NULL)
		goto end_setfd;

	/**
	 * Get the field identifier for the primitive integer
	 * that is part of the FileDescriptor class.
	 */
	fdFdField = (*env)->GetFieldID(env, fdClass, "fd", "I");
	if(fdFdField == NULL || (*env)->ExceptionOccurred(env) != NULL)
		goto end_setfd;

	(*env)->DeleteLocalRef(env, fdClass);
	fdClass = NULL;

	/**
	 * Set the value
	 */
	(*env)->SetIntField(env, thisFdInstance, fdFdField, fd_value);
	(*env)->DeleteLocalRef(env, thisFdInstance);

end_setfd:
	/**
	 * method complete, value is -1 unless the 
	 * entire method is successful.
	 */
	return;
}

static jobject newInetAddress(JNIEnv *env, unsigned long addr)
{
	char 		buf[32];
	jclass		addrClass;
	jmethodID	addrByNameMethodID;
	jobject 	addrInstance = NULL;
	jstring		addrString = NULL;

#define BYTE_OF(_x, _i) ((_x >> (_i * 8)) & 0xff)
	sprintf(buf,
		"%d.%d.%d.%d",
		BYTE_OF(addr, 3),
		BYTE_OF(addr, 2),
		BYTE_OF(addr, 1),
		BYTE_OF(addr, 0));
#undef BYTE_OF

	/**
	 * create the string
	 */
	addrString = (*env)->NewStringUTF(env, (const char *)buf);
	if(addrString == NULL || (*env)->ExceptionOccurred(env))
		goto end_inet;

	/**
	 * load the class
	 */
	addrClass = (*env)->FindClass(env, "java/net/InetAddress");
	if(addrClass == NULL || (*env)->ExceptionOccurred(env) != NULL)
		goto end_inet;

	/**
	 * Find the static method
	 */
	addrByNameMethodID = (*env)->GetStaticMethodID(env,
						       addrClass,
						       "getByName",
						       "(Ljava/lang/String;)Ljava/net/InetAddress;");
	if(addrByNameMethodID == NULL || (*env)->ExceptionOccurred(env) != NULL)
		goto end_inet;

	/*
	 * Invoke it!
	 */
	addrInstance = (*env)->CallStaticObjectMethod(env,
						      addrClass,
						      addrByNameMethodID,
						      addrString);

	(*env)->DeleteLocalRef(env, addrClass);
	(*env)->DeleteLocalRef(env, addrString);
end_inet:

	return addrInstance;
}

static unsigned long getInetAddress(JNIEnv *env, jobject instance)
{
	jclass		addrClass = NULL;
	jmethodID	addrArrayMethodID = NULL;
	jbyteArray	addrData = NULL;

	unsigned long	retAddr = 0UL;

	/**
	 * load the class
	 */
	addrClass = (*env)->GetObjectClass(env, instance);
	if(addrClass == NULL || (*env)->ExceptionOccurred(env) != NULL)
		goto end_inet;

	/**
	 * Find the method
	 */
	addrArrayMethodID = (*env)->GetMethodID(env,
						addrClass,
						"getAddress",
						"()[B");
	if(addrArrayMethodID == NULL || (*env)->ExceptionOccurred(env) != NULL)
		goto end_inet;

	addrData = (*env)->CallObjectMethod(env,instance,addrArrayMethodID);
	if(addrData == NULL || (*env)->ExceptionOccurred(env) != NULL)
		goto end_inet;

	(*env)->GetByteArrayRegion(env,
				   addrData,
				   0,
				   4,
				   (jbyte *)&retAddr);

	(*env)->DeleteLocalRef(env, addrClass);
	(*env)->DeleteLocalRef(env, addrData);
end_inet:

	return retAddr;
}

/*
 * Class:     org_opennms_protocols_icmp_IcmpSocket
 * Method:    initSocket
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_org_opennms_protocols_icmp_IcmpSocket_initSocket (JNIEnv *env, jobject instance)
{
	int icmp_fd = openIcmpSocket();
	if(icmp_fd < 0)
	{
		char	errBuf[128];	/* for exceptions */
		int	savedErrno  = errno;
		jclass  ioException = (*env)->FindClass(env, "java/net/SocketException");
		if(ioException != NULL)
		{
			sprintf(errBuf, "System error creating ICMP socket (%d, %s)", savedErrno, strerror(savedErrno));
			(*env)->ThrowNew(env, ioException, (char *)errBuf);
		}
	}
	else
	{
		setIcmpFd(env, instance, icmp_fd);
	}
	return;
}
	

/*
 * Class:     org_opennms_protocols_icmp_IcmpSocket
 * Method:    receive
 * Signature: ()Ljava/net/DatagramPacket;
 */
JNIEXPORT jobject JNICALL
Java_org_opennms_protocols_icmp_IcmpSocket_receive (JNIEnv *env, jobject instance)
{
	struct sockaddr_in	inAddr;
	socklen_t		inAddrLen;
	int			iRC;
	void *			inBuf = NULL;
	iphdr_t *		ipHdr = NULL;
	icmphdr_t *		icmpHdr = NULL;

	jbyteArray		byteArray 	= NULL;
	jobject			addrInstance 	= NULL;
	jobject			datagramInstance = NULL;
	jclass			datagramClass 	= NULL;
	jmethodID		datagramCtorID 	= NULL;


	/**
	 * Get the current descriptor's value
	 */
	jint fd_value = getIcmpFd(env, instance);
	if((*env)->ExceptionOccurred(env) != NULL)
	{
		goto end_recv; /* jump to end if necessary */
	}
	else if(fd_value < 0)
	{
		jclass ioEx = (*env)->FindClass(env, "java/io/IOException");
		(*env)->ThrowNew(env, ioEx, "Invalid Socket Descriptor");
		goto end_recv;
	}
	
	/**
	 * Allocate a buffer to receive data if necessary.
	 * This is probably more than necessary, but we don't
	 * want to lose messages if we don't need to. This also
	 * must be dynamic for MT-Safe reasons and avoids blowing
	 * up the stack.
	 */
	inBuf = malloc(512);
	if(inBuf == NULL)
	{
		jclass noMem = (*env)->FindClass(env, "java/lang/OutOfMemoryError");
		(*env)->ThrowNew(env, noMem, "Failed to allocate memory to receive icmp datagram");
		goto end_recv;
	}

	/**
	 * Clear out the address structures where the
	 * operating system will store the to/from address
	 * information.
	 */
	memset((void *)&inAddr, 0, sizeof(inAddr));
	inAddrLen = sizeof(inAddr);

	/**
	 * Receive the data from the operating system. This
	 * will also include the IP header that preceeds
	 * the ICMP data, we'll strip that off later.
	 */
	iRC = recvfrom((int)fd_value,
		       inBuf,
		       512,
		       0,
		       (struct sockaddr *)&inAddr,
		       &inAddrLen);
	if(iRC < 0)
	{
		/*
		 * Error reading the information from the socket
		 */
		char errBuf[256];
		int savedErrno = errno;
		jclass ioEx = (*env)->FindClass(env, "java/io/IOException");

		sprintf(errBuf, "Error reading data from the socket descriptor (%d, %s)", savedErrno, strerror(savedErrno));
		(*env)->ThrowNew(env, ioEx, (char *)errBuf);
		goto end_recv;
	}
	else if(iRC == 0)
	{
		/*
		 * Error reading the information from the socket
		 */
		jclass ioEx = (*env)->FindClass(env, "java/io/EOFException");
		(*env)->ThrowNew(env, ioEx, "End-Of-File returned from socket descriptor");
		goto end_recv;
	}

	/**
	 * update the length by removing the IP
	 * header from the message. Don't forget to decrement
	 * the bytes received by the size of the IP header.
	 *
	 * NOTE: The ihl field of the IP header is the number
	 * of 4 byte values in the header. Thus the ihl must
	 * be multiplied by 4 (or shifted 2 bits).
	 */
	ipHdr = (iphdr_t *)inBuf;
	iRC -= ipHdr->ihl << 2;
	if(iRC <= 0)
	{
		jclass ioEx = (*env)->FindClass(env, "java/io/IOException");
		(*env)->ThrowNew(env, ioEx, "Malformed ICMP datagram received");
		goto end_recv;
	}
	icmpHdr = (icmphdr_t *)((char *)inBuf + (ipHdr->ihl << 2));

	/**
	 * Check the ICMP header for type equal 0, which is ECHO_REPLY, and
	 * then check the payload for the 'OpenNMS!' marker. If it's one
	 * we sent out then fix the recv time!
	 *
	 * Don't forget to check for a buffer overflow!
	 */
#if defined(__SOLARIS__) || defined(__DARWIN__) || defined(__FreeBSD__)
	if(iRC >= (OPENNMS_TAG_OFFSET + OPENNMS_TAG_LEN)
	   && icmpHdr->icmp_type == 0
	   && memcmp((char *)icmpHdr + OPENNMS_TAG_OFFSET, OPENNMS_TAG, OPENNMS_TAG_LEN) == 0)
#else

	if(iRC >= (OPENNMS_TAG_OFFSET + OPENNMS_TAG_LEN)
	   && icmpHdr->type == 0
	   && memcmp((char *)icmpHdr + OPENNMS_TAG_OFFSET, OPENNMS_TAG, OPENNMS_TAG_LEN) == 0)
#endif
	{
		uint64_t now;
		uint64_t sent;
		uint64_t diff;

		/**
		 * get the current time in microseconds and then
		 * compute the difference
		 */
		CURRENTTIMEMICROS(now);
		memcpy((char *)&sent, (char *)icmpHdr + SENTTIME_OFFSET, TIME_LENGTH);
		sent = ntohll(sent);
		diff = now - sent;

		/*
		 * Now fill in the sent, received, and diff
		 */
		sent = MICROS_TO_MILLIS(sent);
		sent = htonll(sent);
		memcpy((char *)icmpHdr + SENTTIME_OFFSET, (char *)&sent, TIME_LENGTH);

		now  = MICROS_TO_MILLIS(now);
		now  = htonll(now);
		memcpy((char *)icmpHdr + RECVTIME_OFFSET, (char *)&now, TIME_LENGTH);

		diff = htonll(diff);
		memcpy((char *)icmpHdr + RTT_OFFSET, (char *)&diff, TIME_LENGTH);

		/* no need to recompute checksum on this on
		 * since we don't actually check it upon receipt
		 */
	}

	/**
	 * Now construct a new java.net.InetAddress object from
	 * the recipt information. The network address must
	 * be passed in network byte order!
	 */
	addrInstance = newInetAddress(env, (unsigned long)ntohl(inAddr.sin_addr.s_addr));
	if(addrInstance == NULL || (*env)->ExceptionOccurred(env) != NULL)
		goto end_recv;

	/**
	 * Get the byte array needed to setup
	 * the datagram constructor.
	 */
	byteArray = (*env)->NewByteArray(env, (jsize)iRC);
	if(byteArray != NULL && (*env)->ExceptionOccurred(env) == NULL)
	{
		(*env)->SetByteArrayRegion(env,
					   byteArray,
					   0,
					   (jsize)iRC,
					   (jbyte *)icmpHdr);
	}
	if((*env)->ExceptionOccurred(env) != NULL)
		goto end_recv;

	/**
	 * get the datagram class
	 */
	datagramClass = (*env)->FindClass(env, "java/net/DatagramPacket");
	if(datagramClass == NULL || (*env)->ExceptionOccurred(env) != NULL)
		goto end_recv;

	/**
	 * datagram constructor identifier
	 */
	datagramCtorID = (*env)->GetMethodID(env,
					     datagramClass,
					     "<init>",
					     "([BILjava/net/InetAddress;I)V");
	if(datagramCtorID == NULL || (*env)->ExceptionOccurred(env) != NULL)
		goto end_recv;

	/*
	 * new one!
	 */
	datagramInstance = (*env)->NewObject(env,
					     datagramClass,
					     datagramCtorID,
					     byteArray,
					     (jint)iRC,
					     addrInstance,
					     (jint)0);

	/**
	 * they will be deleted anyway, 
	 * but we're just speeding up the process.
	 */
	(*env)->DeleteLocalRef(env, addrInstance);
	(*env)->DeleteLocalRef(env, byteArray);
	(*env)->DeleteLocalRef(env, datagramClass);

end_recv:
	if(inBuf != NULL)
		free(inBuf);

	return datagramInstance;
}

/*
 * Class:     org_opennms_protocols_icmp_IcmpSocket
 * Method:    send
 * Signature: (Ljava/net/DatagramPacket;)V
 */
JNIEXPORT void JNICALL
Java_org_opennms_protocols_icmp_IcmpSocket_send (JNIEnv *env, jobject instance, jobject packet)
{
	jclass		dgramClass;
	jmethodID	dgramGetDataID;
	jmethodID	dgramGetAddrID;

	jobject		addrInstance;
	jbyteArray	icmpDataArray;
	unsigned long	netAddr   = 0UL;
	char *		outBuffer = NULL;
	jsize		bufferLen = 0;
	int		iRC;
	struct sockaddr_in Addr;


	/**
	 * Recover the operating system file descriptor
	 * so that we can use it in the sendto function.
	 */
	jint icmpfd = getIcmpFd(env, instance);

	/**
	 * Check for exception
	 */
	if((*env)->ExceptionOccurred(env) != NULL)
		goto end_send;

	/**
	 * check the descriptor
	 */
	if(icmpfd < 0)
	{
		jclass ioEx = (*env)->FindClass(env, "java/io/IOException");
		(*env)->ThrowNew(env, ioEx, "Invalid File Descriptor");
		goto end_send;
	}

	/**
	 * get the DatagramPacket class information
	 */
	dgramClass = (*env)->GetObjectClass(env, packet);
	if(dgramClass == NULL || (*env)->ExceptionOccurred(env) != NULL)
		goto end_send;

	/**
	 * Get the identifiers for the getData() and getAddress()
	 * methods that are part of the DatagramPacket class.
	 */
	dgramGetDataID = (*env)->GetMethodID(env, dgramClass, "getData", "()[B");
	if(dgramGetDataID == NULL || (*env)->ExceptionOccurred(env) != NULL)
		goto end_send;

	dgramGetAddrID = (*env)->GetMethodID(env, dgramClass, "getAddress", "()Ljava/net/InetAddress;");
	if(dgramGetAddrID == NULL || (*env)->ExceptionOccurred(env) != NULL)
		goto end_send;

	(*env)->DeleteLocalRef(env, dgramClass);
	dgramClass = NULL;

	/**
	 * Get the address information from the DatagramPacket
	 * so that a useable Operating System address can
	 * be constructed.
	 */
	addrInstance = (*env)->CallObjectMethod(env, packet, dgramGetAddrID);
	if(addrInstance == NULL || (*env)->ExceptionOccurred(env) != NULL)
		goto end_send;

	netAddr = getInetAddress(env, addrInstance);
	if((*env)->ExceptionOccurred(env) != NULL)
		goto end_send;

	/**
	 * Remove local references that are no longer needed
	 */
	(*env)->DeleteLocalRef(env, addrInstance);
	addrInstance = NULL;

	/**
	 * Get the byte[] data from the DatagramPacket
	 * and then free up the local reference to the
	 * method id of the getData() method.
	 */
	icmpDataArray = (*env)->CallObjectMethod(env, packet, dgramGetDataID);
	if(icmpDataArray == NULL || (*env)->ExceptionOccurred(env) != NULL)
		goto end_send;

	/**
	 * Get the length of the buffer so that
	 * a suitable 'char *' buffer can be allocated
	 * and used with the sendto() function.
	 */
	bufferLen = (*env)->GetArrayLength(env, icmpDataArray);
	if(bufferLen <= 0)
	{
		jclass ioEx = (*env)->FindClass(env, "java/io/IOException");
		(*env)->ThrowNew(env, ioEx, "Insufficent data");
		goto end_send;
	}

	/**
	 * Allocate the buffer where the java byte[] information
	 * is to be transfered to.
	 */
	outBuffer = malloc((size_t)bufferLen);
	if(outBuffer == NULL)
	{
		char buf[128]; /* error condition: java.lang.OutOfMemoryError! */
		int serror = errno;
		jclass memEx = (*env)->FindClass(env, "java/lang/OutOfMemoryError");
		
		sprintf(buf, "Insufficent Memory (%d, %s)", serror, strerror(serror));
		(*env)->ThrowNew(env, memEx, (const char *)buf);
		goto end_send;
	}

	/**
	 * Copy the contents of the packet's byte[] array
	 * into the newly allocated buffer.
	 */
	(*env)->GetByteArrayRegion(env,
				   icmpDataArray,
				   0,
				   bufferLen,
				   (jbyte *)outBuffer);
	if((*env)->ExceptionOccurred(env) != NULL)
		goto end_send;

	(*env)->DeleteLocalRef(env, icmpDataArray);

	/**
	 * Check for 'OpenNMS!' at byte offset 32. If
	 * it's found then we need to modify the time
	 * and checksum for transmission. ICMP type
	 * must equal 8 for ECHO_REQUEST
	 *
	 * Don't forget to check for a potential buffer
	 * overflow!
	 */
#if defined(__SOLARIS__) || defined(__DARWIN__) || defined(__FreeBSD__)
	if(bufferLen >= (OPENNMS_TAG_OFFSET + OPENNMS_TAG_LEN)
	   && ((icmphdr_t *)outBuffer)->icmp_type == 0x08
	   && memcmp((char *)outBuffer + OPENNMS_TAG_OFFSET, OPENNMS_TAG, OPENNMS_TAG_LEN) == 0)
#else
	if(bufferLen >= (OPENNMS_TAG_OFFSET + OPENNMS_TAG_LEN)
	   && ((icmphdr_t *)outBuffer)->type == 0x08
	   && memcmp((char *)outBuffer + OPENNMS_TAG_OFFSET, OPENNMS_TAG, OPENNMS_TAG_LEN) == 0)
#endif
	{
		uint64_t now = 0;

		memcpy((char *)outBuffer + RECVTIME_OFFSET, (char *)&now, TIME_LENGTH);
		memcpy((char *)outBuffer + RTT_OFFSET, (char *)&now, TIME_LENGTH);

		CURRENTTIMEMICROS(now);
		now = htonll(now);
		memcpy((char *)outBuffer + SENTTIME_OFFSET, (char *)&now, TIME_LENGTH);

		/* recompute the checksum */
#if defined(__SOLARIS__) || defined(__DARWIN__) || defined(__FreeBSD__)
		((icmphdr_t *)outBuffer)->icmp_cksum = 0;
		((icmphdr_t *)outBuffer)->icmp_cksum = checksum((unsigned short *)outBuffer, bufferLen);
#else
		((icmphdr_t *)outBuffer)->checksum = 0;
		((icmphdr_t *)outBuffer)->checksum = checksum((unsigned short *)outBuffer, bufferLen);
#endif
	}

	/**
	 * Now send the damn data before Jeff drives me nuts!
	 */
	memset(&Addr, 0, sizeof(Addr));
	Addr.sin_family = AF_INET;
	Addr.sin_port   = 0;
	Addr.sin_addr.s_addr = netAddr;

	iRC = sendto((int)icmpfd,
		     (void *)outBuffer,
		     (int)bufferLen,
		     0,
		     (struct sockaddr *)&Addr,
		     sizeof(Addr));

	if(iRC == -1 && errno == EACCES)
	{
		jclass ioEx = (*env)->FindClass(env, "java/net/NoRouteToHostException");
		(*env)->ThrowNew(env, ioEx, "cannot send to broadcast address");
	}
	else if(iRC != bufferLen)
	{
		char buf[128];
		int serror = errno;
		jclass ioEx = (*env)->FindClass(env, "java/io/IOException");
		
		sprintf(buf, "sendto error (%d, %s)", serror, strerror(serror));
		(*env)->ThrowNew(env, ioEx, (const char *)buf);
	}
	

end_send:
	if(outBuffer != NULL)
		free(outBuffer);

	return;
}

/*
 * Class:     org_opennms_protocols_icmp_IcmpSocket
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void
JNICALL Java_org_opennms_protocols_icmp_IcmpSocket_close (JNIEnv *env, jobject instance)
{
	jint fd_value = getIcmpFd(env, instance);
	if(fd_value >= (jint)0 && (*env)->ExceptionOccurred(env) == NULL)
	{
		close((int)fd_value);
		setIcmpFd(env, instance, (jint)-1);
	}
	return;
}
