/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.netutils.internal.service;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.icmp.EchoPacket;
import org.opennms.netmgt.icmp.jna.JnaPingReply;
import org.opennms.netmgt.icmp.jna.JnaPingRequest;
import org.opennms.netmgt.icmp.jni.JniPingRequest;
import org.opennms.netmgt.icmp.jni.JniPingResponse;
import org.opennms.netmgt.icmp.jni6.Jni6PingRequest;
import org.opennms.netmgt.icmp.jni6.Jni6PingResponse;
import org.opennms.protocols.icmp.ICMPEchoPacket;
import org.opennms.protocols.icmp6.ICMPv6EchoPacket;
import org.opennms.protocols.icmp6.ICMPv6EchoReply;
import org.opennms.protocols.icmp6.ICMPv6Packet;

public class PingSequenceTest {

    @Test
    public void testExtractSequenceId() throws UnknownHostException {
        final Inet6Address ip6Address = (Inet6Address) Inet6Address.getByName("0000:0000:0000:0000:0000:0000:0000:0001");
        final InetAddress ipAddress = InetAddress.getByName("127.0.0.1");

        // 1st test replies (ping command created a response)
        EchoPacket response = new JniPingResponse(ipAddress, createEchoPacket((short) 1));
        EchoPacket response2 = new Jni6PingResponse(ip6Address, new ICMPv6EchoReply(createEchoPacket6((short) 2)));
        EchoPacket response3 = new JnaPingReply(ipAddress, new DummyEchoPacket(-1, 3, System.currentTimeMillis() + 100, System.currentTimeMillis()));

        Assert.assertEquals(1, new PingSequence(response).getSequenceNumber());
        Assert.assertEquals(2, new PingSequence(response2).getSequenceNumber());
        Assert.assertEquals(3, new PingSequence(response3).getSequenceNumber());

        // Request (ping command failed, no response was created)
        EchoPacket request = new JniPingRequest(ipAddress, 1, 1, 1, 0, 64, null);
        EchoPacket request2 = new Jni6PingRequest(ip6Address, 1, 2, 1, 0, 64, null);
        EchoPacket request3 = new JnaPingRequest(ipAddress, 1, 3, 1, 0, 64, null);

        Assert.assertEquals(1, new PingSequence(request, true).getSequenceNumber());
        Assert.assertEquals(2, new PingSequence(request2, true).getSequenceNumber());
        Assert.assertEquals(3, new PingSequence(request3, true).getSequenceNumber());
    }

    private ICMPv6EchoPacket createEchoPacket6(short sequenceId) {
        ICMPv6EchoPacket echoPacket = new ICMPv6EchoPacket(new ICMPv6Packet(64));
        echoPacket.setSentTime(System.currentTimeMillis());
        echoPacket.setReceiveTime(echoPacket.getSentTime() + 100);
        echoPacket.setSequenceNumber(sequenceId);
        return echoPacket;
    }

    private static ICMPEchoPacket createEchoPacket(short sequenceId) {
        ICMPEchoPacket icmpEchoPacket = new ICMPEchoPacket(1, 64);
        icmpEchoPacket.setSequenceId(sequenceId);
        icmpEchoPacket.setSentTime();
        icmpEchoPacket.setReceivedTime(icmpEchoPacket.getSentTime() + 100);
        return icmpEchoPacket;
    }
}
