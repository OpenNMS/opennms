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
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.skip;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.slice;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint16;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint32;
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
 * |  Type high    |  Type low(*)  |                               |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+          Value                |
 * |                                                               |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 */
public class ExtendedCommunities implements Attribute {
    public final List<ExtendedCommunity> extendedCommunities;

    public ExtendedCommunities(final ByteBuf buffer, final PeerFlags flags) {
        this.extendedCommunities = BufferUtils.repeatRemaining(buffer, b -> new ExtendedCommunity(b, flags));
    }

    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("extendedCommunities", this.extendedCommunities)
                          .toString();
    }

    public static class ExtendedCommunity {
        public final boolean authoritative;
        public final boolean transitive;
        public final int highType; // uint8
        public final int lowType; // uint8
        public final Value value;

        public ExtendedCommunity(final ByteBuf buffer, final PeerFlags flags) {
            final int highType = uint8(buffer);
            this.authoritative = (highType >> 7 & 0x01) == 1;
            this.transitive = (highType >> 6 & 0x01) == 1;

            this.highType = (highType & 0x3f);
            this.lowType = uint8(buffer);

            this.value = parseValue(this.authoritative,
                                    this.highType,
                                    this.lowType,
                                    slice(buffer, 6));
        }

        private static Value parseValue(final boolean authoritative,
                                        final int highType,
                                        final int lowType,
                                        final ByteBuf buffer) {
            switch (highType | ((authoritative ? 1 : 0) << 7)) {
                case 0x00: // (Non-)Transitive Two-Octet AS-Specific (RFC7153)
                    return parseCommon(buffer, lowType, false, false);
                case 0x01: // (Non-)Transitive IPv4-Address-Specific (RFC7153)
                    return parseCommon(buffer, lowType, true, true);
                case 0x02: // (Non-)Transitive Four-Octet AS-Specific (RFC7153)
                    return parseCommon(buffer, lowType, true, false);

                case 0x03: // Transitive Opaque [RFC7153]
                    return parseOpaque(buffer, lowType);

                case 0x80: // Generic Transitive Experimental Use [RFC7153]
                    return parseGeneric(buffer, lowType, false, false);
                case 0x81: // Generic Transitive Experimental Use Extended Community Part 2 [RFC7674]
                    return parseGeneric(buffer, lowType, true, true);
                case 0x82: // Generic Transitive Experimental Use Extended Community Part 3 [RFC7674]
                    return parseGeneric(buffer, lowType, true, false);

                default:
                    BmpParser.RATE_LIMITED_LOG.debug("Unknown Extended Community Attribute: {}", highType);
                    return null;
            }
        }

        private static Value parseCommon(final ByteBuf buffer,
                                         final int lowType,
                                         final boolean globalLarge,
                                         final boolean globalAddress) {
            final String global = globalLarge
                                  ? globalAddress
                                    ? InetAddressUtils.toIpAddrString(bytes(buffer, 4))
                                    : Long.toString(uint32(buffer))
                                  : Integer.toString(uint16(buffer));
            final String local = globalLarge
                                 ? Integer.toString(uint16(buffer))
                                 : Long.toString(uint32(buffer));

            final String value = String.format("%s:%s", global, local);

            switch (lowType) {
                case 0x02: // Route Target [RFC4360]
                    return new Value("rt", value);
                case 0x03: // Route Origin [RFC4360]
                    return new Value("soo", value);
                case 0x04: // Link Bandwidth Extended Community [draft-ietf-idr-link-bandwidth-00]
                    return new Value("link-bw", value);
                case 0x05: // OSPF Domain Identifier [RFC4577]
                    return new Value("ospf-did", value);
                case 0x07: // OSPF Route ID [RFC4577]
                    return new Value("ospf-rid", value);
                case 0x08: // BGP Data Collection [RFC4384]
                    return new Value("colc", value);
                case 0x09: // Source AS [RFC6514]
                    return new Value("sas", value);
                case 0x0a: // L2VPN Identifier [RFC6074]
                case 0x10: // Cisco VPN-Distinguisher
                    return new Value("vpn-id", value);
                case 0x0b: // VRF Route Import [RFC6514]
                    return new Value("import", value);
                case 0x0c: // Flow-spec Redirect to IPv4 [draft-ietf-idr-flowspec-redirect]
                    return new Value("flowspec-redir", value);
                case 0x12: // Inter-Area P2MP Segmented Next-Hop [RFC7524]
                    return new Value("p2mp-nh", value);
                case 0x13: // Route-Target Record [draft-ietf-bess-service-chaining]
                    return new Value("rtr", value);

                default:
                    BmpParser.RATE_LIMITED_LOG.debug("Unknown Extended Community Attribute: Common: {}", lowType);
                    return null;
            }
        }

        private static Value parseOpaque(final ByteBuf buffer,
                                         final int lowType) {
            switch (lowType) {
                case 0x00: { // BGP Origin Validation State Extended Community [RFC8097]
                    skip(buffer, 5);
                    final int state = uint8(buffer);

                    switch (state) {
                        case 0:
                            return new Value("valid", "valid");
                        case 1:
                            return new Value("valid", "not found");
                        case 2:
                            return new Value("valid", "invalid");

                        default:
                            BmpParser.RATE_LIMITED_LOG.debug("Unknown Extended Community Attribute: Opaque: Origin Validation State: {}", state);
                            return null;
                    }
                }

                case 0x01: { // Cost Community [draft-ietf-idr-custom-decision]
                    final int poi = uint8(buffer); // Point of insertion
                    final int cid = uint8(buffer); // Community ID

                    final long cost = uint32(buffer);

                    final String value;
                    switch (poi) {
                        case 128: // ABSOLUTE_VALUE
                            value = String.format("abs:%s%s", cid, cost);
                            break;
                        case 129: // IGP_COST
                            value = String.format("igp:%s%s", cid, cost);
                            break;
                        case 130: // EXTERNAL_INTERNAL
                            value = String.format("ext:%s%s", cid, cost);
                            break;
                        case 131: // BGP_ID
                            value = String.format("bgp_id:%s%s", cid, cost);
                            break;

                        default:
                            BmpParser.RATE_LIMITED_LOG.debug("Unknown Extended Community Attribute: Opaque: Point of Insertion: {}", poi);
                            return null;
                    }

                    return new Value("cost", value);
                }

                case 0x03: { // CP-ORF [RFC7543]
                    return new Value("cp-orf", "");
                }

                case 0x06: { // OSPF Route Type [RFC4577]
                    return parseOspfRouteType(buffer);
                }

                case 0x0b: { // Color Extended Community [RFC5512]
                    skip(buffer, 2);
                    final long color = uint32(buffer);

                    return new Value("color", Long.toString(color));
                }

                case 0x0c: { // Encapsulation Extended Community [RFC5512]
                    skip(buffer, 4);
                    final int tunnelType = uint16(buffer);

                    return new Value("encap", Integer.toString(tunnelType));
                }

                case 0x0d: { // Default Gateway [Yakov_Rekhter]
                    return new Value("default-gw",  "");
                }

                default:
                    BmpParser.RATE_LIMITED_LOG.debug("Unknown Extended Community Attribute: Opaque: {}", lowType);
                    return null;
            }
        }

        private static Value parseOspfRouteType(final ByteBuf buffer) {
            final long area = uint32(buffer);
            final int type = uint8(buffer);
            final int options = uint8(buffer);

            final String value;
            switch (type) {
                case 1:
                case 2: // Intra-Area routes (LSA)
                    value = String.format("%s:O", area);
                    break;
                case 3: // Intra-Area routes
                    value = String.format("%s:IA", area);
                    break;
                case 5: // External routes
                    value = String.format("0:E:%s", options);
                    break;
                case 7: // NSSA routes
                    value = String.format("%s:N:%s", options);
                    break;

                default:
                    BmpParser.RATE_LIMITED_LOG.debug("Unknown Extended Community Attribute: Opaque: OSPF Route Type: {}", type);
                    return null;
            }

            return new Value("ospf-rt", value);
        }

        private static Value parseGeneric(final ByteBuf buffer,
                                          final int lowType,
                                          final boolean globalLarge,
                                          final boolean globalAddress) {
            switch (lowType) {
                case 0x00: { // OSPF Route Type (deprecated) [RFC4577]
                    return parseOspfRouteType(buffer);
                }

                case 0x01: { // OSPF Router ID (deprecated) [RFC4577]
                    final long id = uint32(buffer);
                    return new Value("ospf-ri", String.format("%s", id));
                }

                case 0x06: { // Flow spec traffic-rate [RFC5575]
                    final int global = uint16(buffer);
                    final long local = uint32(buffer);
                    return new Value("flow-rate", String.format("%s:%s", global, local));
                }

                case 0x07: { // Flow spec traffic-action [RFC5575]
                    skip(buffer, 5);
                    final int actions = uint8(buffer);

                    final StringBuilder s = new StringBuilder();
                    if ((actions & 0x02) != 0) { // Terminal action
                        s.append('S');
                    }
                    if ((actions & 0x01) != 0) { // Sample and Logging
                        s.append('T');
                    }

                    return new Value("flow-act", s.toString());
                }

                case 0x08: { // Flow spec redirect AS-2byte format [RFC5575][RFC7674]
                    final String global = globalLarge
                                          ? globalAddress
                                            ? InetAddressUtils.toIpAddrString(bytes(buffer, 4))
                                            : Long.toString(uint32(buffer))
                                          : Integer.toString(uint16(buffer));
                    final String local = globalLarge
                                         ? Integer.toString(uint16(buffer))
                                         : Long.toString(uint32(buffer));

                    return new Value("flow-redir", String.format("%s:%s", global, local));
                }

                case 0x09: { // Flow spec traffic-remarking [RFC5575]
                    skip(buffer, 5);
                    final int remark = uint8(buffer);
                    return new Value("flow-remark", String.format("%s", remark));
                }

                case 0x0a: { // Layer2 Info Extended Community [RFC4761]
                    final int encap = uint8(buffer);
                    final int flags = uint8(buffer);
                    final int mtu = uint16(buffer);
                    return new Value("l2info", String.format("%s:%s:%s", encap, flags, mtu));
                }

                default:
                    BmpParser.RATE_LIMITED_LOG.debug("Unknown Extended Community Attribute: Generic: {}", lowType);
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
