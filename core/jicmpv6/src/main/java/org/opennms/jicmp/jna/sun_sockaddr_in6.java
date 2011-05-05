/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2011 The OpenNMS Group, Inc.  All rights reserved.
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

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.sun.jna.Structure;

public class sun_sockaddr_in6 extends Structure {
    
    public short      sin6_family;
    public byte[]     sin6_port     = new byte[2];   /* Transport layer port # (in_port_t)*/
    public byte[]     sin6_flowinfo = new byte[4];   /* IP6 flow information */
    public byte[]     sin6_addr     = new byte[16];  /* IP6 address */
    public byte[]     sin6_scope_id = new byte[4];   /* scope zone index */
    public byte[]     __sin6_src_id = new byte[4];   /* impl. specific - UDP replies */
    
    public sun_sockaddr_in6(int family, byte[] addr, byte[] port) {
        sin6_family = (short)(0xffff & family);
        assertLen("port", port, 2);
        sin6_port = port;
        sin6_flowinfo = new byte[4];
        assertLen("address", addr, 16);
        sin6_addr = addr;
        sin6_scope_id = new byte[4];
    }
    
    public sun_sockaddr_in6() {
        this((byte)0, new byte[16], new byte[2]);
    }
    
    public sun_sockaddr_in6(InetAddress address, int port) {
        this(NativeDatagramSocket.AF_INET6, 
             address.getAddress(), 
             new byte[] {(byte)(0xff & (port >> 8)), (byte)(0xff & port)});
    }
    
    private void assertLen(String field, byte[] addr, int len) {
        if (addr.length != len) {
            throw new IllegalArgumentException(field+" length must be "+len+" bytes but was " + addr.length + " bytes.");
        }
    }
    
    public InetAddress getAddress() {
        try {
            return InetAddress.getByAddress(sin6_addr);
        } catch (UnknownHostException ex) {
            // this can never happen as sin6_addr is always 16 bytes long.
            return null;
        }
    }
    
    public void setAddress(InetAddress address) {
        byte[] addr = address.getAddress();
        assertLen("address", addr, 16);
        sin6_addr = addr;
    }

    public int getPort() {
        int port = 0;
        for(int i = 0; i < 2; i++) {
            port = ((port << 8) | (sin6_port[i] & 0xff));
        }
        return port;
    }
    
    public void setPort(int port) {
        byte[] p = new byte[] {(byte)(0xff & (port >> 8)), (byte)(0xff & port)};
        assertLen("port", p, 2);
        sin6_port = p;
    }
}