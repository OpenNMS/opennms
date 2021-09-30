/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.icmp.jna;

import java.net.InetAddress;
import java.nio.ByteBuffer;

import org.opennms.jicmp.ipv6.ICMPv6EchoPacket;
import org.opennms.jicmp.jna.NativeDatagramPacket;
import org.opennms.jicmp.jna.NativeDatagramSocket;

class V6PingRequest extends ICMPv6EchoPacket {
    
    public static final int PACKET_LENGTH = 64;
    public static final long COOKIE = 0x4F70656E4E4D5321L;
    public static final int OFFSET_COOKIE = 0;
    public static final int OFFSET_TIMESTAMP = 8;
    public static final int OFFSET_THREAD_ID = 16;
    public static final int DATA_LENGTH = 8*3;
    
    public V6PingRequest() {
        super(PACKET_LENGTH);
        setType(Type.EchoRequest);
        setCode(0);
    }

    public V6PingRequest(int id, int seqNum, long threadId, int packetsize) {
        super(packetsize);
        // header fields
        setType(Type.EchoRequest);
        setCode(0);
        setIdentifier(id);
        setSequenceNumber(seqNum);
        
        // data fields
        setThreadId(threadId);
        setCookie();
        // timestamp is set later

        // fill buffer with 'interesting' data
        ByteBuffer buf = getContentBuffer();
        for(int b = DATA_LENGTH; b < buf.limit(); b++) {
            buf.put(b, (byte)b);
        }
    }

    public V6PingRequest(int id, int seqNum, long threadId) {
        super(PACKET_LENGTH);
        // header fields
        setType(Type.EchoRequest);
        setCode(0);
        setIdentifier(id);
        setSequenceNumber(seqNum);
        
        // data fields
        setThreadId(threadId);
        setCookie();
        // timestamp is set later

        // fill buffer with 'interesting' data
        ByteBuffer buf = getContentBuffer();
        for(int b = DATA_LENGTH; b < buf.limit(); b++) {
            buf.put(b, (byte)b);
        }
    }
    
    public long getThreadId() {
        return getContentBuffer().getLong(OFFSET_THREAD_ID);
    }
    
    public void setThreadId(long threadId) {
        getContentBuffer().putLong(OFFSET_THREAD_ID, threadId);
    }

    private void setCookie() {
        getContentBuffer().putLong(OFFSET_COOKIE, COOKIE);
    }

    @Override
    public NativeDatagramPacket toDatagramPacket(InetAddress destinationAddress) {
        getContentBuffer().putLong(OFFSET_TIMESTAMP, System.nanoTime());
        return super.toDatagramPacket(destinationAddress);
    }

    public void send(NativeDatagramSocket socket, InetAddress addr) {
        socket.send(toDatagramPacket(addr));
    }
}
