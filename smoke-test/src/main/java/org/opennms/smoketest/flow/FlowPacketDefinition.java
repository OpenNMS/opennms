/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.flow;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Objects;

import com.google.common.io.ByteStreams;

public class FlowPacketDefinition {
    private final InetSocketAddress sendToAddress;
    private final int flowCount;
    private final String resource;

    public FlowPacketDefinition(FlowPacket flowPacket, InetSocketAddress sendToAddress) {
        this(flowPacket.getResource(), flowPacket.getFlowCount(), sendToAddress);
    }

    public FlowPacketDefinition(String resource, int flowCount, InetSocketAddress sendToAddress) {
        this.resource = Objects.requireNonNull(resource);
        this.flowCount = flowCount;
        this.sendToAddress = Objects.requireNonNull(sendToAddress);
    }

    public int getFlowCount() {
        return flowCount;
    }

    public void send() throws IOException {
        sendNetflowPacket(this.sendToAddress, resource);
    }

    private static byte[] getNetflowPacketContent(final String filename) throws IOException {
        try (final InputStream is = FlowPacketDefinition.class.getResourceAsStream(filename)) {
            return ByteStreams.toByteArray(is);
        }
    }

    // Sends a netflow Packet to the given destination address
    private static void sendNetflowPacket(final InetSocketAddress destinationAddress, final String filename) throws IOException {
        final byte[] bytes = getNetflowPacketContent(filename);
        try (DatagramSocket serverSocket = new DatagramSocket(0)) { // opens any free port
            final DatagramPacket sendPacket = new DatagramPacket(bytes, bytes.length, destinationAddress.getAddress(), destinationAddress.getPort());
            serverSocket.send(sendPacket);
        }
    }
}
