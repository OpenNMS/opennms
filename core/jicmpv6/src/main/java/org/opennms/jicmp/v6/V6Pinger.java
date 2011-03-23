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
package org.opennms.jicmp.v6;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import org.opennms.jicmp.Pinger;
import org.opennms.jicmp.ipv6.ICMPv6EchoPacket;
import org.opennms.jicmp.ipv6.ICMPv6Packet;
import org.opennms.jicmp.ipv6.ICMPv6Packet.Type;
import org.opennms.jicmp.jna.NativeDatagramPacket;
import org.opennms.jicmp.jna.NativeDatagramSocket;

import com.sun.jna.Platform;

/**
 * PingListener
 *
 * @author brozow
 */
public class V6Pinger extends Pinger {

    public V6Pinger() throws Exception {
        super(NativeDatagramSocket.create(NativeDatagramSocket.PF_INET6, Platform.isMac() ? NativeDatagramSocket.SOCK_DGRAM : NativeDatagramSocket.SOCK_RAW, NativeDatagramSocket.IPPROTO_ICMPV6));
        
        // Windows requires at least one packet sent before a receive call can be made without error
        // so we send a packet here to make sure...  This one should not match the normal ping requests
        // since it does not contain the cookie so it won't interface.
        if (Platform.isWindows()) {
            ICMPv6EchoPacket packet = new ICMPv6EchoPacket(64);
            packet.setCode(0);
            packet.setType(Type.EchoRequest);
            packet.getContentBuffer().putLong(System.nanoTime());
            packet.getContentBuffer().putLong(System.nanoTime());
            getPingSocket().send(packet.toDatagramPacket(InetAddress.getByName("::1")));
        }
    }
    
    public void run() {
        try {
            NativeDatagramPacket datagram = new NativeDatagramPacket(65535);
            boolean first = true;
            while (!isFinished()) {
                getPingSocket().receive(datagram);
                long received = System.nanoTime();
    
                ICMPv6Packet icmpPacket = new ICMPv6Packet(getIPPayload(datagram));
                PingReply echoReply = icmpPacket.getType() == Type.EchoReply ? new PingReply(icmpPacket, received) : null;
            
                if (echoReply != null && echoReply.isValid()) {
                    // skip the first one since it includes class loading time etc.
                    if (first) {
                        first = false;
                    } else {
                        m_metric.update(echoReply.elapsedTimeNanos());
                        // 64 bytes from 127.0.0.1: icmp_seq=0 time=0.069 ms
                        printf("%d bytes from %s: icmp_seq=%d time=%.3f ms\n", 
                                echoReply.getPacketLength(),
                                datagram.getAddress().getHostAddress(),
                                echoReply.getSequenceNumber(),
                                echoReply.elapsedTimeNanos()/1000000.0
                        );
                    }
                }
            }
    
    
        } catch(Exception e) {
            m_exception.set(e);
            e.printStackTrace();
        }
    }

    private ByteBuffer getIPPayload(NativeDatagramPacket datagram) {
        return datagram.getContent();
    }
    
    public void ping(InetAddress addr, int id, int count, int interval) throws InterruptedException {
        NativeDatagramSocket socket = getPingSocket();
        for(int i = 0; i <= count; i++) {
            PingRequest request = new PingRequest(id, i);
            request.send(socket, addr);
            Thread.sleep(interval);
        }
        
        //round-trip cnt/min/avg/max/stddev = 10/0.053/0.137/0.233/0.038 ms
        printf("round-trip %s ms\n", m_metric.getSummary(TimeUnit.MILLISECONDS));

    }

}
