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

public class sockaddr_in6 extends Structure {
    
    public short      sin6_family;
    public byte[]     sin6_port     = new byte[2];   /* Transport layer port # (in_port_t)*/
    public byte[]     sin6_flowinfo = new byte[4];   /* IP6 flow information */
    public byte[]     sin6_addr     = new byte[16];  /* IP6 address */
    public byte[]     sin6_scope_id = new byte[4];   /* scope zone index */
    
    public sockaddr_in6(int family, byte[] addr, byte[] port) {
        sin6_family = (short)(0xffff & family);
        assertLen("port", port, 2);
        sin6_port = port == null? null : port.clone();
        sin6_flowinfo = new byte[4];
        assertLen("address", addr, 16);
        sin6_addr = addr == null? null : addr.clone();
        sin6_scope_id = new byte[4];
    }
    
    public sockaddr_in6() {
        this((byte)0, new byte[16], new byte[2]);
    }
    
    public sockaddr_in6(InetAddress address, int port) {
        this(NativeDatagramSocket.AF_INET6, 
             address.getAddress(), 
             new byte[] {(byte)(0xff & (port >> 8)), (byte)(0xff & port)});
    }

    public sockaddr_in6(int port) {
        this(NativeDatagramSocket.AF_INET6, 
             new byte[16], 
             new byte[] {(byte)(0xff & (port >> 8)), (byte)(0xff & port)});
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList(new String[] {"sin6_family", "sin6_port", "sin6_flowinfo", "sin6_addr", "sin6_scope_id"});
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
