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

import java.net.Inet6Address;
import java.net.UnknownHostException;

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.listeners.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramEnrichment;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramVisitor;

import com.google.common.base.Throwables;

import io.netty.buffer.ByteBuf;

public class Inet6Header {

    public final int tos;
    public final int totalLength;
    public final int protocol;

    public final Inet6Address srcAddress;
    public final Inet6Address dstAddress;

    public final Integer srcPort;
    public final Integer dstPort;

    public final Integer tcpFlags;

    public Inet6Header(final ByteBuf buffer) throws InvalidPacketException {
        final long version_tc_fl = BufferUtils.uint32(buffer);
        if (version_tc_fl >> 28 != 0x06) {
            // First four bits must be 0x06
            throw new InvalidPacketException(buffer, "Expected IPv6 Header");
        }

        this.tos = (int) ((version_tc_fl >> 20) & ((1 << 8) -1));
        this.totalLength = BufferUtils.uint16(buffer) + 40;
        this.protocol = BufferUtils.uint8(buffer);
        BufferUtils.skip(buffer, 1); // Hop limit

        try {
            this.srcAddress = (Inet6Address) Inet6Address.getByAddress(BufferUtils.bytes(buffer, 16));
            this.dstAddress = (Inet6Address) Inet6Address.getByAddress(BufferUtils.bytes(buffer, 16));
        } catch (final UnknownHostException e) {
            // This only happens if byte array length is != 16
            throw Throwables.propagate(e);
        }

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

    public Inet6Header(final int tos, final int totalLength, final int protocol, final Inet6Address srcAddress, final Inet6Address dstAddress, final Integer srcPort, final Integer dstPort, final Integer tcpFlags) {
        this.tos = tos;
        this.totalLength = totalLength;
        this.protocol = protocol;
        this.srcAddress = srcAddress;
        this.dstAddress = dstAddress;
        this.srcPort = srcPort;
        this.dstPort = dstPort;
        this.tcpFlags = tcpFlags;
    }

    public Inet6Address getSrcAddress() {
        return srcAddress;
    }

    public Inet6Address getDstAddress() {
        return dstAddress;
    }

    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt32("tos", this.tos);
        bsonWriter.writeInt32("length", this.totalLength);
        bsonWriter.writeInt32("protocol", this.protocol);

        bsonWriter.writeStartDocument("src_ip");
        bsonWriter.writeString("address", this.srcAddress.getHostAddress());
        enr.getHostnameFor(this.srcAddress).ifPresent((hostname) -> bsonWriter.writeString("hostname", hostname));
        bsonWriter.writeEndDocument();

        bsonWriter.writeStartDocument("dst_ip");
        bsonWriter.writeString("address", this.dstAddress.getHostAddress());
        enr.getHostnameFor(this.dstAddress).ifPresent((hostname) -> bsonWriter.writeString("hostname", hostname));
        bsonWriter.writeEndDocument();

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

    public void visit(SampleDatagramVisitor visitor) {
        visitor.accept(this);
    }
}
