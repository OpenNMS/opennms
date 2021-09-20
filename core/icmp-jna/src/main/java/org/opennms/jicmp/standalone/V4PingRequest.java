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

package org.opennms.jicmp.standalone;

import java.net.InetAddress;
import java.nio.ByteBuffer;

import org.opennms.jicmp.ip.ICMPEchoPacket;
import org.opennms.jicmp.jna.NativeDatagramPacket;
import org.opennms.jicmp.jna.NativeDatagramSocket;

class V4PingRequest extends ICMPEchoPacket {
    
    public V4PingRequest() {
        super(64);
        setType(Type.EchoRequest);
        setCode(0);
    }
    
    public V4PingRequest(int id, int seqNum) {
        super(64);
        setType(Type.EchoRequest);
        setCode(0);
        setIdentifier(id);
        setSequenceNumber(seqNum);
        ByteBuffer buf = getContentBuffer();
        for(int b = 0; b < 56; b++) {
            buf.put((byte)b);
        }
    }

    @Override
    public NativeDatagramPacket toDatagramPacket(InetAddress destinationAddress) {
        ByteBuffer contentBuffer = getContentBuffer();
        contentBuffer.putLong(V4PingReply.COOKIE);
        contentBuffer.putLong(System.nanoTime());
        return super.toDatagramPacket(destinationAddress);
    }

    public void send(NativeDatagramSocket socket, InetAddress addr) {
        socket.send(toDatagramPacket(addr));
    }
}
