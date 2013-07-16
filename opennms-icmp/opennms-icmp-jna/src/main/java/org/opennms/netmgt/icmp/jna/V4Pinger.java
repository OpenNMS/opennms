/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.icmp.jna;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;

import org.opennms.core.logging.Logging;
import org.opennms.jicmp.ip.ICMPEchoPacket;
import org.opennms.jicmp.ip.ICMPPacket;
import org.opennms.jicmp.ip.IPPacket;
import org.opennms.jicmp.ip.ICMPPacket.Type;
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
public class V4Pinger extends AbstractPinger<Inet4Address> {
    private static final Logger LOG = LoggerFactory.getLogger(V4Pinger.class);
    

    public V4Pinger(final int pingerId) throws Exception {
        super(pingerId, NativeDatagramSocket.create(NativeDatagramSocket.PF_INET, Platform.isMac() ? NativeDatagramSocket.SOCK_DGRAM : NativeDatagramSocket.SOCK_RAW, NativeDatagramSocket.IPPROTO_ICMP));
        
        // Windows requires at least one packet sent before a receive call can be made without error
        // so we send a packet here to make sure...  This one should not match the normal ping requests
        // since it does not contain the cookie so it won't interface.
        if (Platform.isWindows()) {
            final ICMPEchoPacket packet = new ICMPEchoPacket(64);
            packet.setCode(0);
            packet.setType(Type.EchoRequest);
            packet.getContentBuffer().putLong(System.nanoTime());
            packet.getContentBuffer().putLong(System.nanoTime());
            getPingSocket().send(packet.toDatagramPacket(InetAddress.getLocalHost()));
        }
    }
    
    @Override
    public void run() {
        Logging.putPrefix("icmp");
        try {
            final int pingerId = getPingerId();
            final NativeDatagramPacket datagram = new NativeDatagramPacket(65535);
            while (!isFinished()) {
                getPingSocket().receive(datagram);
                final long received = System.nanoTime();
    
                final ICMPPacket icmpPacket = new ICMPPacket(getIPPayload(datagram));
                final V4PingReply echoReply = icmpPacket.getType() == Type.EchoReply ? new V4PingReply(icmpPacket, received) : null;
            
                if (echoReply != null && echoReply.getIdentifier() ==  pingerId && echoReply.isValid()) {
                    notifyPingListeners(datagram.getAddress(), echoReply);
                }
            }
        } catch(final Throwable t) {
            setThrowable(t);
            LOG.debug("Error caught while processing ping packets: {}", t.getMessage(), t);
        }
    }

    private ByteBuffer getIPPayload(final NativeDatagramPacket datagram) {
        return new IPPacket(datagram.getContent()).getPayload();
    }
    
    @Override
    public void ping(final Inet4Address addr, final int identifier, final int sequenceNumber, final long threadId, final long count, final long interval, final int packetSize) throws InterruptedException {
        final NativeDatagramSocket socket = getPingSocket();
        for(int i = sequenceNumber; i < sequenceNumber + count; i++) {
            final V4PingRequest request = new V4PingRequest(identifier, i, threadId,packetSize);
            request.send(socket, addr);
            Thread.sleep(interval);
        }
    }
}
