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

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import com.sun.jna.LastErrorException;
import com.sun.jna.Native;

/**
 * UnixNativeSocketFactory
 *
 * @author brozow
 */
public class Win32V6NativeSocket extends NativeDatagramSocket {
    
    static {
        Native.register((String)null);
    }

    private final int m_sock;

    public Win32V6NativeSocket(int family, int type, int protocol, final int listenPort) throws Exception {
        m_sock = socket(family, type, protocol);
        final sockaddr_in6 addr_in = new sockaddr_in6(listenPort);
        bind(m_sock, addr_in, addr_in.size());
    }

    public native int bind(int socket, sockaddr_in6 address, int address_len) throws LastErrorException;

    public native int socket(int domain, int type, int protocol) throws LastErrorException;

    public native int sendto(int socket, Buffer buffer, int buflen, int flags, sockaddr_in6 dest_addr, int dest_addr_len) throws LastErrorException;

    public native int recvfrom(int socket, Buffer buffer, int buflen, int flags, sockaddr_in6 in_addr, int[] in_addr_len) throws LastErrorException;

    public native int closesocket(int socket) throws LastErrorException;

    @Override
    public void setTrafficClass(final int tc) throws LastErrorException {
        // it appears that IP_TOS and IPV6_TCLASS do not exist in Win32 anymore
    }

    @Override
    public void allowFragmentation(final boolean frag) throws IOException {
        allowFragmentation(IPPROTO_IPV6, IPV6_DONTFRAG, frag);
    }

    @Override
    public int receive(NativeDatagramPacket p) throws UnknownHostException {
        sockaddr_in6 in_addr = new sockaddr_in6();
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
        sockaddr_in6 destAddr = new sockaddr_in6(p.getAddress(), p.getPort());
        return sendto(getSock(), buf, buf.remaining(), 0, destAddr, destAddr.size());
    }

    @Override
    public void close() {
        closesocket(getSock());
    }

    @Override
    public int getSock() {
        return m_sock;
    }

}
