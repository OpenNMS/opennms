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
import java.nio.Buffer;
import java.nio.ByteBuffer;

import com.sun.jna.LastErrorException;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * UnixNativeSocketFactory
 *
 * @author brozow
 */
public class BSDV6NativeSocket extends NativeDatagramSocket {

	static {
		Native.register((String)null);
	}

	private static final int IPV6_TCLASS = 36;
	private int m_sock;

	public BSDV6NativeSocket(final int family, final int type, final int protocol, final int listenPort) throws Exception {
		m_sock = socket(family, type, protocol);
                final bsd_sockaddr_in6 in_addr = new bsd_sockaddr_in6(listenPort);
                bind(m_sock, in_addr, in_addr.size());
	}

	public native int bind(int socket, bsd_sockaddr_in6 address, int address_len) throws LastErrorException;
	public native int socket(int family, int type, int protocol) throws LastErrorException;
	public native int setsockopt(int socket, int level, int option_name, Pointer value, int option_len);
	public native int sendto(int socket, Buffer buffer, int buflen, int flags, bsd_sockaddr_in6 dest_addr, int dest_addr_len) throws LastErrorException;
	public native int recvfrom(int socket, Buffer buffer, int buflen, int flags, bsd_sockaddr_in6 in_addr, int[] in_addr_len) throws LastErrorException;
	public native int close(int socket) throws LastErrorException;

	@Override
	public void setTrafficClass(final int tc) throws LastErrorException {
	    final IntByReference tc_ptr = new IntByReference(tc);
	    try {
	        setsockopt(getSock(), IPPROTO_IPV6, IPV6_TCLASS, tc_ptr.getPointer(), Native.POINTER_SIZE);
	    } catch (final LastErrorException e) {
	        throw new RuntimeException("setsockopt: " + strerror(e.getErrorCode()));
	    }
	}

        @Override
        public void allowFragmentation(final boolean frag) throws IOException {
            allowFragmentation(IPPROTO_IPV6, IPV6_DONTFRAG, frag);
        }

	@Override
	public int receive(final NativeDatagramPacket p) {
		final bsd_sockaddr_in6 in_addr = new bsd_sockaddr_in6();
		final int[] szRef = new int[] { in_addr.size() };
		final int socket = getSock();

		final ByteBuffer buf = p.getContent();

		SocketUtils.assertSocketValid(socket);
		final int n = recvfrom(socket, buf, buf.capacity(), 0, in_addr, szRef);
		p.setLength(n);
		p.setAddress(in_addr.getAddress());
		p.setPort(in_addr.getPort());

		return n;
	}

	@Override
	public int send(final NativeDatagramPacket p) {
		final ByteBuffer buf = p.getContent();
		final bsd_sockaddr_in6 destAddr = new bsd_sockaddr_in6(p.getAddress(), p.getPort());
		final int socket = getSock();
		SocketUtils.assertSocketValid(socket);
		return sendto(socket, buf, buf.remaining(), 0, destAddr, destAddr.size());
	}

	@Override
	public void close() {
		close(m_sock);
		m_sock = -1;
	}

	@Override
	public int getSock() {
	    return m_sock;
	}
}
