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
package org.opennms.jicmp.ffm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.junit.After;
import org.junit.Assume;
import org.junit.Test;
import org.opennms.jicmp.ip.ICMPEchoPacket;
import org.opennms.jicmp.ip.ICMPPacket;
import org.opennms.jicmp.ip.IPPacket;
import org.opennms.jicmp.ipv6.ICMPv6EchoPacket;
import org.opennms.jicmp.ipv6.ICMPv6Packet;
import org.opennms.jicmp.jna.InvalidSocketException;
import org.opennms.jicmp.jna.NativeDatagramPacket;
import org.opennms.jicmp.jna.NativeDatagramSocket;

/**
 * Sends ICMP echo requests to the loopback addresses through the FFM socket.
 * Skipped when the environment does not allow ICMP sockets for this user
 * (on Linux, see net.ipv4.ping_group_range).
 */
public class FfmNativeDatagramSocketTest {

    private static final int ID = 0x4f4d;
    private final ExecutorService m_reader = Executors.newSingleThreadExecutor(r -> {
        final Thread t = new Thread(r, "ffm-icmp-test-reader");
        t.setDaemon(true);
        return t;
    });
    private NativeDatagramSocket m_socket;

    @After
    public void tearDown() {
        if (m_socket != null) {
            m_socket.close();
        }
        m_reader.shutdownNow();
    }

    private NativeDatagramSocket open(final int family, final int protocol) {
        try {
            return new FfmNativeDatagramSocket(family, NativeDatagramSocket.SOCK_DGRAM, protocol, ID);
        } catch (final Exception e) {
            try {
                return new FfmNativeDatagramSocket(family, NativeDatagramSocket.SOCK_RAW, protocol, ID);
            } catch (final Exception raw) {
                Assume.assumeNoException("ICMP sockets not permitted here: " + e.getMessage(), raw);
                return null;
            }
        }
    }

    /** Receives until a datagram matches, or fails after a few seconds. */
    private NativeDatagramPacket receiveMatching(final Predicate<NativeDatagramPacket> matches) throws Exception {
        final Future<NativeDatagramPacket> f = m_reader.submit(() -> {
            while (true) {
                final NativeDatagramPacket p = new NativeDatagramPacket(65535);
                m_socket.receive(p);
                if (matches.test(p)) {
                    return p;
                }
            }
        });
        return f.get(5, TimeUnit.SECONDS);
    }

    @Test
    public void canPingIpv4Loopback() throws Exception {
        m_socket = open(NativeDatagramSocket.AF_INET, NativeDatagramSocket.IPPROTO_ICMP);
        final InetAddress loopback = InetAddress.getByName("127.0.0.1");
        final long cookie = System.nanoTime();

        final ICMPEchoPacket request = new ICMPEchoPacket(64);
        request.setType(ICMPPacket.Type.EchoRequest);
        request.setCode(0);
        request.setIdentifier(ID);
        request.setSequenceNumber(1);
        request.getContentBuffer().putLong(0, cookie);
        final NativeDatagramPacket out = request.toDatagramPacket(loopback);
        assertEquals(out.getLength(), m_socket.send(out));

        final NativeDatagramPacket reply = receiveMatching(p -> {
            final ICMPPacket icmp = new ICMPPacket(new IPPacket(p.getContent()).getPayload());
            return icmp.getType() == ICMPPacket.Type.EchoReply
                    && new ICMPEchoPacket(icmp).getContentBuffer().getLong(0) == cookie;
        });
        assertEquals(loopback, reply.getAddress());
    }

    @Test
    public void canPingIpv6Loopback() throws Exception {
        m_socket = open(NativeDatagramSocket.AF_INET6, NativeDatagramSocket.IPPROTO_ICMPV6);
        final InetAddress loopback = InetAddress.getByName("::1");
        final long cookie = System.nanoTime();

        final ICMPv6EchoPacket request = new ICMPv6EchoPacket(64);
        request.setType(ICMPv6Packet.Type.EchoRequest);
        request.setCode(0);
        request.setIdentifier(ID);
        request.setSequenceNumber(1);
        request.getContentBuffer().putLong(0, cookie);
        final NativeDatagramPacket out = request.toDatagramPacket(loopback);
        try {
            m_socket.send(out);
        } catch (final NativeSocketException e) {
            Assume.assumeNoException("IPv6 loopback unavailable", e);
        }

        final NativeDatagramPacket reply = receiveMatching(p -> {
            final ICMPv6Packet icmp = new ICMPv6Packet(p.getContent());
            return icmp.getType() == ICMPv6Packet.Type.EchoReply
                    && new ICMPv6EchoPacket(icmp).getContentBuffer().getLong(0) == cookie;
        });
        assertEquals(loopback, reply.getAddress());
    }

    @Test
    public void socketOptionsCanBeSet() throws Exception {
        m_socket = open(NativeDatagramSocket.AF_INET, NativeDatagramSocket.IPPROTO_ICMP);
        m_socket.setTrafficClass(0x28);
        m_socket.allowFragmentation(false);
        m_socket.allowFragmentation(true);
    }

    @Test
    public void closedSocketIsRejected() throws Exception {
        m_socket = open(NativeDatagramSocket.AF_INET, NativeDatagramSocket.IPPROTO_ICMP);
        assertTrue(m_socket.getSock() > 0);
        m_socket.close();
        try {
            m_socket.send(new NativeDatagramPacket(ByteBuffer.allocate(8), InetAddress.getByName("127.0.0.1"), 0));
            fail("expected InvalidSocketException");
        } catch (final InvalidSocketException expected) {
            // ok
        }
        m_socket = null;
    }

    @Test
    public void createSelectsFfmImplementation() throws Exception {
        final String previous = System.getProperty(NativeDatagramSocket.IMPLEMENTATION_PROPERTY);
        try {
            System.setProperty(NativeDatagramSocket.IMPLEMENTATION_PROPERTY, "auto");
            try {
                m_socket = NativeDatagramSocket.create(NativeDatagramSocket.AF_INET, NativeDatagramSocket.IPPROTO_ICMP, ID);
            } catch (final Exception e) {
                Assume.assumeNoException("ICMP sockets not permitted here", e);
            }
            assertEquals(FfmNativeDatagramSocket.class, m_socket.getClass());
        } finally {
            if (previous == null) {
                System.clearProperty(NativeDatagramSocket.IMPLEMENTATION_PROPERTY);
            } else {
                System.setProperty(NativeDatagramSocket.IMPLEMENTATION_PROPERTY, previous);
            }
        }
    }
}
