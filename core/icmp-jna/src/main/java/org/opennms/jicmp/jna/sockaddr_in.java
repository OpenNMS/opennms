/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
