/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.bytes;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.slice;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint16;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint8;

import java.util.List;
import java.util.Objects;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.telemetry.listeners.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.BmpParser;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerFlags;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

/**
 *  0                   1                   2                   3
 *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * | 0x00 or 0x40  |    Sub-Type   |    Global Administrator       |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |          Global Administrator (cont.)                         |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |          Global Administrator (cont.)                         |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |          Global Administrator (cont.)                         |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * | Global Administrator (cont.)  |    Local Administrator        |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 */
public class ExtendedV6Communities implements Attribute {
    public final List<ExtendedV6Community> extendedCommunities;

    public ExtendedV6Communities(final ByteBuf buffer, final PeerFlags flags) {
        this.extendedCommunities = BufferUtils.repeatRemaining(buffer, b -> new ExtendedV6Community(b, flags));
    }

    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("extendedV6Communities", this.extendedCommunities)
                          .toString();
    }

    public static class ExtendedV6Community {
        public final boolean authoritative;
        public final boolean transitive;
        public final int highType; // uint8
        public final int lowType; // uint8
        public final Value value;

        public ExtendedV6Community(final ByteBuf buffer, final PeerFlags flags) {
            final int highType = uint8(buffer);
            this.authoritative = (highType >> 7 & 0x01) == 1;
            this.transitive = (highType >> 6 & 0x01) == 1;

            this.highType = (highType & 0x3f);
            this.lowType = uint8(buffer);

            this.value = parseValue(this.authoritative,
                                    this.highType,
                                    this.lowType,
                                    slice(buffer, 18));
        }

        private static Value parseValue(final boolean authoritative,
                                        final int highType,
                                        final int lowType,
                                        final ByteBuf buffer) {
            switch (highType | ((authoritative ? 1 : 0) << 7)) {
                case 0x00:
                    return parseSpecific(buffer, lowType);

                default:
                    BmpParser.RATE_LIMITED_LOG.debug("Unknown Extended IPv6 Community Attribute: {}", highType);
                    return null;
            }
        }

        private static Value parseSpecific(final ByteBuf buffer,
                                           final int lowType) {


            final String global = InetAddressUtils.toIpAddrString(bytes(buffer, 16));
            final String local = Integer.toString(uint16(buffer));

            final String value = String.format("%s:%s", global, local);

            switch (lowType) {
                case 0x02: // Route Target [RFC5701]
                    return new Value("rt", value);
                case 0x03: // Route Origin [RFC5701]
                    return new Value("soo", value);
                case 0x0b: // VRF Route Import [RFC6515][RFC6514]
                    return new Value("import", value);
                case 0x0c: // Flow-spec Redirect to IPv6 [draft-ietf-idr-flowspec-redirect]
                    return new Value("flowspec-redir", value);
                case 0x10: // Cisco VPN-Distinguisher
                    return new Value("vpn-id", value);
                case 0x12: // Inter-Area P2MP Segmented Next-Hop [RFC7524]
                    return new Value("p2mp-nh", value);

                default:
                    BmpParser.RATE_LIMITED_LOG.debug("Unknown Extended Community Attribute (IPv4): Common: {}", lowType);
                    return null;
            }
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                              .add("authoritative", this.authoritative)
                              .add("transitive", this.transitive)
                              .add("highType", this.highType)
                              .add("lowType", this.lowType)
                              .add("value", this.value)
                              .toString();
        }

        public static class Value {
            public final String type;
            public final String value;

            public Value(final String type, final String value) {
                this.type = Objects.requireNonNull(type);
                this.value = Objects.requireNonNull(value);
            }
        }
    }
}
