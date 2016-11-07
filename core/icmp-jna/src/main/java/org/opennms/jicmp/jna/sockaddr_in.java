/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.jicmp.jna;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

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
        sin_port = port == null? null : port.clone();
        assertLen("address", addr, 4);
        sin_addr = addr == null? null : addr.clone();
    }
    
    public sockaddr_in() {
        this((byte)0, new byte[4], new byte[2]);
    }
    
    public sockaddr_in(InetAddress address, int port) {
        this(NativeDatagramSocket.AF_INET, 
             address.getAddress(), 
             new byte[] {(byte)(0xff & (port >> 8)), (byte)(0xff & port)});
    }
    
    public sockaddr_in(final int port) {
        this(NativeDatagramSocket.AF_INET,
             new byte[4],
             new byte[] {(byte)(0xff & (port >> 8)), (byte)(0xff & port)});
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList(new String[] {"sin_family", "sin_port", "sin_addr", "sin_zero"});
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
        int port = 0;
        for(int i = 0; i < 2; i++) {
            port = ((port << 8) | (sin_port[i] & 0xff));
        }
        return port;
    }
    
    public void setPort(int port) {
        byte[] p = new byte[] {(byte)(0xff & (port >> 8)), (byte)(0xff & port)};
        assertLen("port", p, 2);
        sin_port = p;
    }
}
