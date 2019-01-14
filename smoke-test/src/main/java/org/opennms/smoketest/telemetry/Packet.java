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

package org.opennms.smoketest.telemetry;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Objects;

import com.google.common.io.ByteStreams;

public class Packet {

    private final String resource;
    private InetSocketAddress destinationAddress;

    public Packet(String resource) {
        this(resource, null);
    }

    public Packet(String resource, InetSocketAddress destinationAddress) {
        this.resource = Objects.requireNonNull(resource);
        this.destinationAddress = destinationAddress;
    }

    public void setDestinationAddress(InetSocketAddress destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public InetSocketAddress getDestinationAddress() {
        return destinationAddress;
    }

    public String getResource() {
        return resource;
    }

    // Sends a packet to the defined destination address
    public void send() throws IOException {
        Objects.requireNonNull(destinationAddress, "DestinationAddress was not initialized properly");
        final byte[] bytes = getPacketContent(resource);
        try (DatagramSocket serverSocket = new DatagramSocket(0)) { // opens any free port
            final DatagramPacket sendPacket = new DatagramPacket(bytes, bytes.length, destinationAddress.getAddress(), destinationAddress.getPort());
            serverSocket.send(sendPacket);
        }
    }

    private static byte[] getPacketContent(final String filename) throws IOException {
        try (final InputStream is = Packet.class.getResourceAsStream(filename)) {
            return ByteStreams.toByteArray(is);
        }
    }
}
