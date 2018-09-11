/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.headers;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.common.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;

import com.google.common.base.Throwables;

public class Inet4Header {

    public final int tos;
    public final int totalLength;
    public final int protocol;

    public final String srcAddress;
    public final String dstAddress;

    public final Integer srcPort;
    public final Integer dstPort;

    public final Integer tcpFlags;

    public Inet4Header(final ByteBuffer buffer) throws InvalidPacketException {
        final int version_ihl = BufferUtils.uint8(buffer);
        if (version_ihl >> 4 != 0x04) {
            // First four bits must be 0x04
            throw new InvalidPacketException(buffer, "Expected IPv4 Header");
        }

        final int ihl = (version_ihl & ((1 << 4) - 1)) * 4;

        this.tos = BufferUtils.uint8(buffer);
        this.totalLength = BufferUtils.uint16(buffer);
        BufferUtils.skip(buffer, 2); // Identifier
        BufferUtils.skip(buffer, 2); // Flags and fragment offset
        BufferUtils.skip(buffer, 1); // TTL
        this.protocol = BufferUtils.uint8(buffer);
        BufferUtils.skip(buffer, 2); // Checksum

        try {
            this.srcAddress = Inet4Address.getByAddress(BufferUtils.bytes(buffer, 4)).getHostAddress();
            this.dstAddress = Inet4Address.getByAddress(BufferUtils.bytes(buffer, 4)).getHostAddress();
        } catch (final UnknownHostException e) {
            // This only happens if byte array length is != 4
            throw Throwables.propagate(e);
        }

        BufferUtils.skip(buffer, ihl - 20); // Padding / Options

        // Almost all protocols have their port fields directly following the IP header
        switch (this.protocol) {
            case 6: // TCP
                this.srcPort = BufferUtils.uint16(buffer);
                this.dstPort = BufferUtils.uint16(buffer);
                BufferUtils.skip(buffer, 8); // SeqNum and AckNum
                this.tcpFlags = BufferUtils.uint16(buffer) & ((1 << 9) - 1);
                break;

            case 17: // UDP
            case 132: // SCTP
                this.srcPort = BufferUtils.uint16(buffer);
                this.dstPort = BufferUtils.uint16(buffer);
                this.tcpFlags = null;
                break;

            case 1: // ICMP
            case 58: // ICMP6
                this.srcPort = 0;
                this.dstPort = BufferUtils.uint16(buffer);
                this.tcpFlags = null;
                break;

            default:
                this.srcPort = null;
                this.dstPort = null;
                this.tcpFlags = null;
        }
    }

    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt32("tos", this.tos);
        bsonWriter.writeInt32("length", this.totalLength);
        bsonWriter.writeInt32("protocol", this.protocol);
        bsonWriter.writeString("src_ip", this.srcAddress);
        bsonWriter.writeString("dst_ip", this.dstAddress);

        if (this.srcPort != null) {
            bsonWriter.writeInt32("src_port", this.srcPort);
        }

        if (this.dstPort != null) {
            bsonWriter.writeInt32("dst_port", this.dstPort);
        }

        if (this.tcpFlags != null) {
            bsonWriter.writeInt32("tcp_flags", this.tcpFlags);
        }

        bsonWriter.writeEndDocument();
    }
}
