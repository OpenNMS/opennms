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

package org.opennms.jicmp.standalone;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import org.opennms.jicmp.ip.ICMPEchoPacket;
import org.opennms.jicmp.ip.ICMPPacket;
import org.opennms.jicmp.ip.IPPacket;
import org.opennms.jicmp.ip.ICMPPacket.Type;
import org.opennms.jicmp.jna.NativeDatagramPacket;
import org.opennms.jicmp.jna.NativeDatagramSocket;

import com.sun.jna.Platform;

/**
 * PingListener
 *
 * @author brozow
 */
public class V4Pinger extends AbstractPinger<Inet4Address> {

    public V4Pinger() throws Exception {
        super(NativeDatagramSocket.create(NativeDatagramSocket.PF_INET, Platform.isMac() ? NativeDatagramSocket.SOCK_DGRAM : NativeDatagramSocket.SOCK_RAW, NativeDatagramSocket.IPPROTO_ICMP));
        
        // Windows requires at least one packet sent before a receive call can be made without error
        // so we send a packet here to make sure...  This one should not match the normal ping requests
        // since it does not contain the cookie so it won't interface.
        if (Platform.isWindows()) {
            ICMPEchoPacket packet = new ICMPEchoPacket(64);
            packet.setCode(0);
            packet.setType(Type.EchoRequest);
            packet.getContentBuffer().putLong(System.nanoTime());
            packet.getContentBuffer().putLong(System.nanoTime());
            getPingSocket().send(packet.toDatagramPacket(InetAddress.getLocalHost()));
        }
    }
    
    @Override
    public void run() {
        try {
            NativeDatagramPacket datagram = new NativeDatagramPacket(65535);
            while (!isFinished()) {
                getPingSocket().receive(datagram);
                long received = System.nanoTime();
    
                ICMPPacket icmpPacket = new ICMPPacket(getIPPayload(datagram));
                V4PingReply echoReply = icmpPacket.getType() == Type.EchoReply ? new V4PingReply(icmpPacket, received) : null;
            
                if (echoReply != null && echoReply.isValid()) {
                    // 64 bytes from 127.0.0.1: icmp_seq=0 time=0.069 ms
                    System.out.printf("%d bytes from %s: tid=%d icmp_seq=%d time=%.3f ms\n", 
                        echoReply.getPacketLength(),
                        datagram.getAddress().getHostAddress(),
                        echoReply.getIdentifier(),
                        echoReply.getSequenceNumber(),
                        echoReply.elapsedTime(TimeUnit.MILLISECONDS)
                    );
                    for (PingReplyListener listener : getListeners()) {
                        listener.onPingReply(datagram.getAddress(), echoReply);
                    }
                }
            }
        } catch(Throwable e) {
            m_throwable.set(e);
            e.printStackTrace();
        }
    }

    private ByteBuffer getIPPayload(NativeDatagramPacket datagram) {
        return new IPPacket(datagram.getContent()).getPayload();
    }
    
    @Override
    public PingReplyMetric ping(Inet4Address addr, int id, int sequenceNumber, int count, long interval) throws InterruptedException {
        PingReplyMetric metric = new PingReplyMetric(count, interval);
        addPingReplyListener(metric);
        NativeDatagramSocket socket = getPingSocket();
        for(int i = sequenceNumber; i < sequenceNumber + count; i++) {
            V4PingRequest request = new V4PingRequest(id, i);
            request.send(socket, addr);
            Thread.sleep(interval);
        }
        return metric;
    }

}
