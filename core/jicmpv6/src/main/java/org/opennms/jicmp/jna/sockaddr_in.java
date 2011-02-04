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

public class sockaddr_in extends Structure {
    public short   sin_family;
    /* we  use an array of bytes rather than int16 to avoid jna byte reordering */
    public byte[]  sin_port;
    /* we use an array of bytes rather than the tradition int32 
     * to avoid having jna to byte-order swapping.. They are already in
     * network byte order in java
     */
    public byte[]  sin_addr;
    public byte[]  sin_zero = new byte[8];
    
    public sockaddr_in(int family, byte[] addr, byte[] port) {
        sin_family = (short)(0xffff & family);
        assertLen("port", port, 2);
        sin_port = port;
        assertLen("address", addr, 4);
        sin_addr = addr;
    }
    
    public sockaddr_in() {
        this((byte)0, new byte[4], new byte[2]);
    }
    
    public sockaddr_in(InetAddress address, int port) {
        this(NativeDatagramSocket.AF_INET, 
             address.getAddress(), 
             new byte[] {(byte)(0xff & (port >> 8)), (byte)(0xff & port)});
    }
    
    private void assertLen(String field, byte[] addr, int len) {
        if (addr.length != len) {
            throw new IllegalArgumentException(field+" length must be "+len+" bytes");
        }
    }
    
    public InetAddress getAddress() {
        try {
            return InetAddress.getByAddress(sin_addr);
        } catch (UnknownHostException e) {
            // this can't happen because we ensure the sin_addr always has length 4
            return null;
        }
    }
    
    public void setAddress(InetAddress address) {
        byte[] addr = address.getAddress();
        assertLen("address", addr, 4);
        sin_addr = addr;
    }

    public int getPort() {
        int portH = (int)sin_port[0];
        int portL = (int)sin_port[1];
        int port = (portH << 8) | portL;
        return port;
    }
    
    public void setPort(int port) {
        byte[] p = new byte[] {(byte)(0xff & (port >> 8)), (byte)(0xff & port)};
        assertLen("port", p, 2);
        sin_port = p;
    }
}