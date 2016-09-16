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

package org.opennms.jicmp.standalone;

import java.net.InetAddress;

import org.opennms.jicmp.ipv6.ICMPv6EchoPacket;
import org.opennms.jicmp.ipv6.ICMPv6Packet.Type;
import org.opennms.jicmp.jna.NativeDatagramPacket;
import org.opennms.jicmp.jna.NativeDatagramSocket;

import com.sun.jna.Platform;

/**
 * Dumper
 *
 * @author brozow
 */
public class Dumper {
    
    public void dump() throws Exception {
        NativeDatagramSocket m_pingSocket =  NativeDatagramSocket.create(NativeDatagramSocket.PF_INET6, NativeDatagramSocket.IPPROTO_ICMPV6, 1234);
        
        if (Platform.isWindows()) {
            ICMPv6EchoPacket packet = new ICMPv6EchoPacket(64);
            packet.setCode(0);
            packet.setType(Type.EchoRequest);
            packet.getContentBuffer().putLong(System.nanoTime());
            packet.getContentBuffer().putLong(System.nanoTime());
            m_pingSocket.send(packet.toDatagramPacket(InetAddress.getByName("::1")));
        }

        try {
            NativeDatagramPacket datagram = new NativeDatagramPacket(65535);
            while (true) {
                m_pingSocket.receive(datagram);
                System.err.println(datagram);
            }
    
    
        } catch(Throwable e) {
            e.printStackTrace();
        }
 
    }
    
    public static void main(String[] args) throws Exception {
        new Dumper().dump();
    }

}
