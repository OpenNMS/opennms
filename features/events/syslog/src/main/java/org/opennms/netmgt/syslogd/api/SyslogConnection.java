/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.syslogd.api;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.opennms.core.ipc.sink.api.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyslogConnection implements Message {

    private static final Logger LOG = LoggerFactory.getLogger(SyslogConnection.class);

    /**
     * This size is used as the size of each {@link ByteBuffer} used to capture syslog
     * messages.
     */
    public static final int MAX_PACKET_SIZE = 4096;

    private final InetSocketAddress source;
    private final ByteBuffer buffer;

    public SyslogConnection(DatagramPacket pkt, boolean copy) {
        this.source = new InetSocketAddress(pkt.getAddress(), pkt.getPort());
        final byte[] data = copy ? Arrays.copyOf(pkt.getData(), pkt.getLength()) : pkt.getData();
        this.buffer = ByteBuffer.wrap(data, 0, pkt.getLength());
    }

    public SyslogConnection(InetSocketAddress source, ByteBuffer buffer) {
        this.source = source;
        this.buffer = buffer;
    }

    public InetSocketAddress getSource() {
        return source;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public static DatagramPacket copyPacket(final DatagramPacket packet) {
        byte[] message = new byte[packet.getLength()];
        System.arraycopy(packet.getData(), 0, message, 0, packet.getLength());
        InetAddress addr = null;
        try {
            addr = InetAddress.getByAddress(packet.getAddress().getHostName(), packet.getAddress().getAddress());
            DatagramPacket retPacket = new DatagramPacket(
                message,
                packet.getOffset(),
                packet.getLength(),
                addr,
                packet.getPort()
            );
            return retPacket;
        } catch (UnknownHostException e) {
            LOG.warn("unable to clone InetAddress object for {}", packet.getAddress());
        }
        return null;
    }

    public static DatagramPacket copyPacket(final InetAddress sourceAddress, final int sourcePort, final ByteBuffer buffer) {
        byte[] message = new byte[MAX_PACKET_SIZE];
        int i = 0;
        // Copy the buffer into the byte array
        while (buffer.hasRemaining()) {
            message[i++] = buffer.get();
        }
        return copyPacket(sourceAddress, sourcePort, message, i);
    }

    private static DatagramPacket copyPacket(final InetAddress sourceAddress, final int sourcePort, final byte[] buffer, final int length) {
        DatagramPacket retPacket = new DatagramPacket(
            buffer,
            0,
            length,
            sourceAddress,
            sourcePort
        );
        return retPacket;
    }
}
