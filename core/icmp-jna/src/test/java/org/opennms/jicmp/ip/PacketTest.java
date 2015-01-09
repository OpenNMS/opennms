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

package org.opennms.jicmp.ip;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.net.InetAddress;
import java.nio.ByteBuffer;

import org.junit.Test;
import org.opennms.jicmp.ip.ICMPPacket.Type;
import org.opennms.jicmp.ip.IPPacket.Protocol;
import org.opennms.jicmp.jna.NativeDatagramPacket;


/**
 * IPPacketTest
 *
 * @author brozow
 */
public class PacketTest {

    byte[] ip = new byte[] {
        (byte)0x45, (byte)0x00, (byte)0x40, (byte)0x00,
        (byte)0x1e, (byte)0x92, (byte)0x00, (byte)0x00,
        (byte)0x80, (byte)0x01, (byte)0x00, (byte)0x00,
        (byte)0x7f, (byte)0x00, (byte)0x00, (byte)0x01,
        (byte)0x7f, (byte)0x00, (byte)0x00, (byte)0x01,
        (byte)0x00, (byte)0x00, (byte)0x5e, (byte)0xf6,
        (byte)0xa8, (byte)0xfc, (byte)0x00, (byte)0xfa,
        (byte)0x00, (byte)0x01, (byte)0x02, (byte)0x03,
        (byte)0x04, (byte)0x05, (byte)0x06, (byte)0x07,
        (byte)0x08, (byte)0x09, (byte)0x0a, (byte)0x0b,
        (byte)0x0c, (byte)0x0d, (byte)0x0e, (byte)0x0f,
        (byte)0x10, (byte)0x11, (byte)0x12, (byte)0x13,
        (byte)0x14, (byte)0x15, (byte)0x16, (byte)0x17,
        (byte)0x18, (byte)0x19, (byte)0x1a, (byte)0x1b,
        (byte)0x1c, (byte)0x1d, (byte)0x1e, (byte)0x1f,
        (byte)0x20, (byte)0x21, (byte)0x22, (byte)0x23,
        (byte)0x24, (byte)0x25, (byte)0x26, (byte)0x27,
        (byte)0x28, (byte)0x29, (byte)0x2a, (byte)0x2b,
        (byte)0x2c, (byte)0x2d, (byte)0x2e, (byte)0x2f,
        (byte)0x30, (byte)0x31, (byte)0x32, (byte)0x33,   
        (byte)0x34, (byte)0x35, (byte)0x36, (byte)0x37,
    };
    
    @Test
    public void testIPHeaderGets() throws Exception {
        
        IPPacket pkt = new IPPacket(ip, 0, ip.length);
        
        // test header data
        assertThat(pkt.getVersion(), is(equalTo(4)));
        assertThat(pkt.getHeaderLength(), is(equalTo(20)));
        assertThat(pkt.getTimeToLive(), is(equalTo(128)));
        assertThat(pkt.getProtocol(), is(equalTo(Protocol.ICMP)));
        assertThat(pkt.getSourceAddress(), is(equalTo(InetAddress.getByName("127.0.0.1"))));
        assertThat(pkt.getDestinationAddress(), is(equalTo(InetAddress.getByName("127.0.0.1"))));
        
        // payload 64 bytes: ICMP header (8 bytes) followed by byte values 1, 2, 3, ... 55, 56 
        ByteBuffer payload = pkt.getPayload();
        assertThat(payload.remaining(), is(equalTo(64)));
        assertThat(payload.capacity(), is(equalTo(64)));
        assertThat(payload.position(), is(equalTo(0)));
        assertThat(payload.get(8), is(equalTo((byte)0)));
        assertThat(payload.get(16), is(equalTo((byte)8)));
        assertThat(payload.get(24), is(equalTo((byte)16)));
    }
    
    @Test
    public void testICMPPacketGets() throws Exception {
        
        IPPacket pkt = new IPPacket(ip, 0, ip.length);
        assertThat(pkt.getProtocol(), is(equalTo(Protocol.ICMP)));
        
        // payload 64 bytes: ICMP header (8 bytes) followed by byte values 1, 2, 3, ... 55, 56 
        ByteBuffer payload = pkt.getPayload();
        ICMPPacket icmpPacket = new ICMPPacket(payload);
        assertThat(icmpPacket.getType(), is(equalTo(Type.EchoReply)));
        assertThat(icmpPacket.getCode(), is(equalTo(0)));
        assertThat(icmpPacket.getChecksum(), is(equalTo(24310)));
        assertThat(icmpPacket.getChecksum(), is(equalTo(icmpPacket.computeChecksum())));
        
        ICMPEchoPacket echoReply = new ICMPEchoPacket(icmpPacket);
        assertThat(echoReply.getIdentifier(), is(equalTo(43260)));
        assertThat(echoReply.getSequenceNumber(), is(equalTo(250)));
        
        ByteBuffer content = echoReply.getContentBuffer();
        for(int i = 0; i < 56; i++) {
            assertThat(content.get(i), is(equalTo((byte)i)));
        }
        
    }
    
    @Test
    public void testICMPPacketSets() throws Exception {
        
        // payload 64 bytes: ICMP header (8 bytes) followed by byte values 0, 1, 2, 3, ... 54, 55
        byte[] bytes = new byte[64];
        ByteBuffer buf = ByteBuffer.wrap(bytes, 0, 64);
        ICMPPacket icmpPacket = new ICMPPacket(buf);
        ICMPEchoPacket echoRequest = new ICMPEchoPacket(icmpPacket);
        echoRequest.setType(Type.EchoRequest);
        echoRequest.setCode(0);
        echoRequest.setIdentifier(0x1234);
        echoRequest.setSequenceNumber(0x5678);
        
        ByteBuffer content = echoRequest.getContentBuffer();
        for(int i = 0; i < 56; i++) {
            content.put((byte)i);
        }
        
        echoRequest.setChecksum(); // check sum is 0x9840
        
        assertThat(bytes[0], is(equalTo((byte)8))); // icmp type
        assertThat(bytes[1], is(equalTo((byte)0))); // icmp code (zero for echo pkts)
        assertThat(bytes[2], is(equalTo((byte)0x98))); // checksum hi
        assertThat(bytes[3], is(equalTo((byte)0x40))); // checksum lo
        assertThat(bytes[4], is(equalTo((byte)0x12))); // id hi
        assertThat(bytes[5], is(equalTo((byte)0x34))); // id lo
        assertThat(bytes[6], is(equalTo((byte)0x56))); // seq_num hi
        assertThat(bytes[7], is(equalTo((byte)0x78))); // seq_num lo

        for(int i = 0; i < 56; i++) {
            assertThat(bytes[8+i], is(equalTo((byte)i)));
        }
        
        NativeDatagramPacket pkt = echoRequest.toDatagramPacket(InetAddress.getByName("127.0.0.1"));
        
        assertThat(pkt.getAddress(), is(equalTo(InetAddress.getByName("127.0.0.1"))));
        assertThat(pkt.getPort(), is(equalTo(0)));
        
        ByteBuffer data = pkt.getContent();
        assertThat(data.position(), is(equalTo(0)));
        assertThat(data.limit(), is(equalTo(64)));
        assertThat(data.remaining(), is(equalTo(64)));
        assertThat(data.hasArray(), is(true));
        assertThat(data.array(), is(sameInstance(bytes)));
        assertThat(data.arrayOffset(), is(equalTo(0)));
        
    }
    
}
