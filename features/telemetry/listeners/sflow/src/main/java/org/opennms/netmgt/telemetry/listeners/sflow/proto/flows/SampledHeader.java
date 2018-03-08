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

package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.nio.ByteBuffer;
import java.util.Optional;

import org.bson.BsonBinary;
import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.Opaque;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.headers.EthernetHeader;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.headers.Inet4Header;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.headers.Inet6Header;

import com.google.common.base.MoreObjects;

// struct sampled_header {
//    header_protocol protocol;       /* Format of sampled header */
//    unsigned int frame_length;      /* Original length of packet before
//                                       sampling.
//                                       Note: For a layer 2 header_protocol,
//                                             length is total number of octets
//                                             of data received on the network 
//                                             (excluding framing bits but
//                                             including FCS octets).
//                                             Hardware limitations may
//                                             prevent an exact reporting
//                                             of the underlying frame length,
//                                             but an agent should attempt to
//                                             be as accurate as possible. Any
//                                             octets added to the frame_length
//                                             to compensate for encapsulations
//                                             removed by the underlying hardware
//                                             must also be added to the stripped
//                                             count. */
//    unsigned int stripped;          /* The number of octets removed from
//                                       the packet before extracting the
//                                       header<> octets. Trailing encapsulation
//                                       data corresponding to any leading
//                                       encapsulations that were stripped must
//                                       also be stripped. Trailing encapsulation
//                                       data for the outermost protocol layer
//                                       included in the sampled header must be
//                                       stripped.
// 
//                                       In the case of a non-encapsulated 802.3
//                                       packet stripped >= 4 since VLAN tag
//                                       information might have been stripped off
//                                       in addition to the FCS.
// 
//                                       Outer encapsulations that are ambiguous,
//                                       or not one of the standard header_protocol
//                                       must be stripped. */
//    opaque header<>;                /* Header bytes */
// };

public class SampledHeader implements FlowData {
    public final HeaderProtocol protocol;
    public final long frame_length;
    public final long stripped;

    public final EthernetHeader ethernetHeader;
    public final Inet4Header inet4Header;
    public final Inet6Header inet6Header;

    public final byte[] rawHeader;

    public SampledHeader(final ByteBuffer buffer) throws InvalidPacketException {
        this.protocol = HeaderProtocol.from(buffer);
        this.frame_length = BufferUtils.uint32(buffer);
        this.stripped = BufferUtils.uint32(buffer);

        switch (this.protocol) {
            case ETHERNET_ISO88023:
                this.ethernetHeader = new Opaque<>(buffer, Optional.empty(), EthernetHeader::new).value;
                this.inet4Header = this.ethernetHeader.inet4Header;
                this.inet6Header = this.ethernetHeader.inet6Header;
                this.rawHeader = this.ethernetHeader.rawHeader;
                break;

            case IPv4:
                this.ethernetHeader = null;
                this.inet4Header = new Opaque<>(buffer, Optional.empty(), Inet4Header::new).value;
                this.inet6Header = null;
                this.rawHeader = null;
                break;

            case IPv6:
                this.ethernetHeader = null;
                this.inet4Header = null;
                this.inet6Header = new Opaque<>(buffer, Optional.empty(), Inet6Header::new).value;
                this.rawHeader = null;
                break;

            default:
                this.ethernetHeader = null;
                this.inet4Header = null;
                this.inet6Header = null;
                this.rawHeader = new Opaque<>(buffer, Optional.empty(), Opaque::parseBytes).value;
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("protocol", this.protocol)
                .add("frame_length", this.frame_length)
                .add("stripped", this.stripped)
                .add("header", this.rawHeader)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeName("protocol");
        this.protocol.writeBson(bsonWriter);
        bsonWriter.writeInt64("frame_length", this.frame_length);
        bsonWriter.writeInt64("stripped", this.stripped);

        if (this.ethernetHeader != null) {
            bsonWriter.writeName("ethernet");
            this.ethernetHeader.writeBson(bsonWriter);
        }

        if (this.inet4Header != null) {
            bsonWriter.writeName("ipv4");
            this.inet4Header.writeBson(bsonWriter);
        }

        if (this.inet6Header != null) {
            bsonWriter.writeName("ipv6");
            this.inet6Header.writeBson(bsonWriter);
        }

        if (this.rawHeader != null) {
            bsonWriter.writeBinaryData("raw", new BsonBinary(this.rawHeader));
        }

        bsonWriter.writeEndDocument();
    }
}
