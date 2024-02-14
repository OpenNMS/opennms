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
package org.opennms.netmgt.icmp.jna;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;

import org.opennms.core.logging.Logging;
import org.opennms.jicmp.ipv6.ICMPv6EchoPacket;
import org.opennms.jicmp.ipv6.ICMPv6Packet;
import org.opennms.jicmp.ipv6.ICMPv6Packet.Type;
import org.opennms.jicmp.jna.NativeDatagramPacket;
import org.opennms.jicmp.jna.NativeDatagramSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Platform;

/**
 * PingListener
 *
 * @author brozow
 */
public class V6Pinger extends AbstractPinger<Inet6Address> {
	

	private static final Logger LOG = LoggerFactory.getLogger(V6Pinger.class);

    public V6Pinger(final int pingerId) throws Exception {
        super(pingerId, NativeDatagramSocket.create(NativeDatagramSocket.PF_INET6, NativeDatagramSocket.IPPROTO_ICMPV6, pingerId));
        
        // Windows requires at least one packet sent before a receive call can be made without error
        // so we send a packet here to make sure...  This one should not match the normal ping requests
        // since it does not contain the cookie so it won't interface.
        if (Platform.isWindows()) {
            final ICMPv6EchoPacket packet = new ICMPv6EchoPacket(64);
            packet.setCode(0);
            packet.setType(Type.EchoRequest);
            packet.getContentBuffer().putLong(System.nanoTime());
            packet.getContentBuffer().putLong(System.nanoTime());
            getPingSocket().send(packet.toDatagramPacket(InetAddress.getByName("::1")));
        }
    }
    
    
    
//    @Override
//    public void start() {
//        throw new UnsupportedOperationException("Put socket initialization here rather than the constructor");
//    }



    @Override
    public void run() {
        Logging.putPrefix("icmp");
        try {
            final int pingerId = getPingerId();
            while (!isFinished()) {
                final NativeDatagramPacket datagram = new NativeDatagramPacket(65535);
                getPingSocket().receive(datagram);
                final long received = System.nanoTime();
    
                final ICMPv6Packet icmpPacket = new ICMPv6Packet(getIPPayload(datagram));
                final V6PingReply echoReply = icmpPacket.getType() == Type.EchoReply ? new V6PingReply(icmpPacket, received) : null;
            
                if (echoReply != null && echoReply.getIdentifier() == pingerId && echoReply.isValid()) {
                    notifyPingListeners(datagram.getAddress(), echoReply);
                }
            }
        } catch(final Throwable t) {
            setThrowable(t);
            LOG.debug("Error caught while processing ping packets: {}", t.getMessage(), t);
        }
    }

    private ByteBuffer getIPPayload(final NativeDatagramPacket datagram) {
        return datagram.getContent();
    }
    
    @Override
    public void ping(final Inet6Address addr, final int identifier, final int sequenceNumber, final long threadId, final long count, final long interval, final int packetSize) throws InterruptedException {
        final NativeDatagramSocket socket = getPingSocket();
        for(int i = sequenceNumber; i < sequenceNumber + count; i++) {
            final V6PingRequest request = new V6PingRequest(identifier, i, threadId,packetSize);
            request.send(socket, addr);
            Thread.sleep(interval);
        }
    }
}
