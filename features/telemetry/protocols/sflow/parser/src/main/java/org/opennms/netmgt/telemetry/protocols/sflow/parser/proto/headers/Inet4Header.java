/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.headers;

import java.net.Inet4Address;
import java.net.UnknownHostException;

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.listeners.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramEnrichment;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramVisitor;

import com.google.common.base.Throwables;

import io.netty.buffer.ByteBuf;

public class Inet4Header {

    public final int tos;
    public final int totalLength;
    public final int protocol;

    public final Inet4Address srcAddress;
    public final Inet4Address dstAddress;

    public final Integer srcPort;
    public final Integer dstPort;

    public final Integer tcpFlags;

    public Inet4Header(final ByteBuf buffer) throws InvalidPacketException {
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
            this.srcAddress = (Inet4Address) Inet4Address.getByAddress(BufferUtils.bytes(buffer, 4));
            this.dstAddress = (Inet4Address) Inet4Address.getByAddress(BufferUtils.bytes(buffer, 4));
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

    public Inet4Header(final int tos, final int totalLength, final int protocol, final Inet4Address srcAddress, final Inet4Address dstAddress, final Integer srcPort, final Integer dstPort, final Integer tcpFlags) {
        this.tos = tos;
        this.totalLength = totalLength;
        this.protocol = protocol;
        this.srcAddress = srcAddress;
        this.dstAddress = dstAddress;
        this.srcPort = srcPort;
        this.dstPort = dstPort;
        this.tcpFlags = tcpFlags;
    }

    public Inet4Address getSrcAddress() {
        return srcAddress;
    }

    public Inet4Address getDstAddress() {
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
