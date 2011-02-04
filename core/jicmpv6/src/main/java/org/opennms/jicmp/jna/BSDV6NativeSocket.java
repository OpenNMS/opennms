/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.jicmp.jna;

import java.nio.Buffer;
import java.nio.ByteBuffer;


import com.sun.jna.LastErrorException;
import com.sun.jna.Native;

/**
 * UnixNativeSocketFactory
 *
 * @author brozow
 */
public class BSDV6NativeSocket extends NativeDatagramSocket {
    
    static {
        Native.register((String)null);
    }

    private int m_sock;
    
    public BSDV6NativeSocket(int family, int type, int protocol) throws Exception {
        m_sock = socket(family, type, protocol);
    }
    
    public native int socket(int family, int type, int protocol) throws LastErrorException;

    public native int sendto(int socket, Buffer buffer, int buflen, int flags, bsd_sockaddr_in6 dest_addr, int dest_addr_len) throws LastErrorException;
    
    public native int recvfrom(int socket, Buffer buffer, int buflen, int flags, bsd_sockaddr_in6 in_addr, int[] in_addr_len) throws LastErrorException;

    public native int close(int socket) throws LastErrorException;

    @Override
    public int receive(NativeDatagramPacket p) {
        bsd_sockaddr_in6 in_addr = new bsd_sockaddr_in6();
        int[] szRef = new int[] { in_addr.size() };
        
        ByteBuffer buf = p.getContent();
        
        int n = recvfrom(getSock(), buf, buf.capacity(), 0, in_addr, szRef);
        p.setLength(n);
        p.setAddress(in_addr.getAddress());
        p.setPort(in_addr.getPort());
        
        return n;
    }

    @Override
    public int send(NativeDatagramPacket p) {
        ByteBuffer buf = p.getContent();
        bsd_sockaddr_in6 destAddr = new bsd_sockaddr_in6(p.getAddress(), p.getPort());
        return sendto(getSock(), buf, buf.remaining(), 0, destAddr, destAddr.size());
    }

    @Override
    public int close() {
        return close(getSock());
    }

    private int getSock() {
        return m_sock;
    }

}
