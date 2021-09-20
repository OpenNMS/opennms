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

package org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.repeatRemaining;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.skip;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.slice;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint16;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint32;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint8;

import java.net.InetAddress;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.BmpParser;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.Header;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.Packet;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.Aggregator;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.AsPath;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.AsPathLimit;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.AtomicAggregate;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.AttrSet;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.Attribute;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.ClusterList;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.Community;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.Connector;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.ExtendedCommunities;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.ExtendedV6Communities;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.LargeCommunities;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.LocalPref;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.MultiExistDisc;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.MultiprotocolReachableNlri;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.MultiprotocolUnreachableNlri;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.NextHop;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.Origin;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.OriginatorId;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.Unknown;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerFlags;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerInfo;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

public class UpdatePacket implements Packet {
    public final Header header;

    public final List<Prefix> withdrawRoutes;
    public final List<PathAttribute> pathAttributes;
    public final List<Prefix> reachableRoutes;

    public UpdatePacket(final Header header, final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
        this.header = Objects.requireNonNull(header);

        this.withdrawRoutes = repeatRemaining(slice(buffer, uint16(buffer)), prefixBuffer -> new Prefix(prefixBuffer, flags, peerInfo));
        this.pathAttributes = repeatRemaining(slice(buffer, uint16(buffer)), pathAttributeBuffer -> new PathAttribute(pathAttributeBuffer, flags, peerInfo));
        this.reachableRoutes = repeatRemaining(buffer, prefixBuffer -> new Prefix(prefixBuffer, flags, peerInfo));
    }

    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    public static class Prefix {
        private static final int BGP_AFI_IPV4 = 1;
        private static final int BGP_SAFI_UNICAST = 1;
        public int length;         // uint8
        public InetAddress prefix; // byte[length padded to 8 bits]
        public String labels = "";
        public long pathId = 0;

        public Prefix(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) {
            final boolean addPathCapabilityEnabled = peerInfo.map(info -> info.isAddPathEnabled(BGP_AFI_IPV4, BGP_SAFI_UNICAST)).orElse(false);

            if (addPathCapabilityEnabled) {
                this.pathId = uint32(buffer);
            }

            this.length = uint8(buffer);

            // Create a buffer for the address with the size required to hold the full address (depending on the
            // version) but fill ony the first n bytes. Remaining bytes will be initialized all zero.
            final byte[] prefix = new byte[flags.addressVersion.map(v -> {switch (v) {
                case IP_V4: return 4;
                case IP_V6: return 16;
                default: throw new IllegalStateException();
            }})];
            buffer.readBytes(prefix, 0, (this.length + (8-1)) / 8); // Read n bits padded to the next full byte
            this.prefix = InetAddressUtils.getInetAddress(prefix);
        }

        public Prefix() {
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("length", this.length)
                    .add("prefix", this.prefix)
                    .add("pathId", this.pathId)
                    .add("labels", this.labels)
                    .toString();
        }
    }

    public static class PathAttribute {
        public final boolean optional;   // uint8 x0000000
        public final boolean transitive; // ..... 0x000000
        public final boolean partial;    // ..... 00x00000
        public final boolean extended;   // ..... 000x0000

        public final Type type;  // uint8
        public final int length; // uint8 / uint16 (extended)

        public final Attribute attribute;

        public PathAttribute(final ByteBuf buffer, final PeerFlags peerFlags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
            final int flags = uint8(buffer);
            this.optional   = ((flags >> 7 & 0x01) == 1);
            this.transitive = ((flags >> 6 & 0x01) == 1);
            this.partial    = ((flags >> 5 & 0x01) == 1);
            this.extended   = ((flags >> 4 & 0x01) == 1);

            this.type = Type.from(uint8(buffer));

            if (this.extended) {
                this.length = uint16(buffer);
            } else {
                this.length = uint8(buffer);
            }

            this.attribute = this.type.parse(slice(buffer, this.length), peerFlags, peerInfo);
        }

        public enum Type {
            ORIGIN {
                @Override
                public Attribute parse(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
                    return new Origin(buffer, flags);
                }
            },
            AS_PATH {
                @Override
                public Attribute parse(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
                    return new AsPath(buffer, flags);
                }
            },
            NEXT_HOP {
                @Override
                public Attribute parse(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
                    return new NextHop(buffer, flags);
                }
            },
            MULTI_EXIT_DISC {
                @Override
                public Attribute parse(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
                    return new MultiExistDisc(buffer, flags);
                }
            },
            LOCAL_PREF {
                @Override
                public Attribute parse(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
                    return new LocalPref(buffer, flags);
                }
            },
            ATOMIC_AGGREGATE {
                @Override
                public Attribute parse(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
                    return new AtomicAggregate(buffer, flags);
                }
            },
            AGGREGATOR {
                @Override
                public Attribute parse(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
                    return new Aggregator(buffer, flags);
                }
            },
            COMMUNITY {
                @Override
                public Attribute parse(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
                    return new Community(buffer, flags);
                }
            },
            ORIGINATOR_ID {
                @Override
                public Attribute parse(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
                    return new OriginatorId(buffer, flags);
                }
            },
            CLUSTER_LIST {
                @Override
                public Attribute parse(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
                    return new ClusterList(buffer, flags);
                }
            },
            EXTENDED_COMMUNITIES {
                @Override
                public Attribute parse(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
                    return new ExtendedCommunities(buffer, flags);
                }
            },
            CONNECTOR_ATTRIBUTE {
                @Override
                public Attribute parse(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
                    return new Connector(buffer, flags);
                }
            },
            AS_PATH_LIMIT {
                @Override
                public Attribute parse(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
                    return new AsPathLimit(buffer, flags);
                }
            },
            EXTENDED_V6_COMMUNITIES {
                @Override
                public Attribute parse(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
                    return new ExtendedV6Communities(buffer, flags);
                }
            },
            LARGE_COMMUNITIES {
                @Override
                public Attribute parse(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
                    return new LargeCommunities(buffer, flags);
                }
            },
            ATTR_SET {
                @Override
                public Attribute parse(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
                    return new AttrSet(buffer, flags, peerInfo);
                }
            },
            UNKNOWN {
                @Override
                public Attribute parse(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
                    return new Unknown(buffer, flags);
                }
            },

            MP_REACH_NLRI {
                @Override
                public Attribute parse(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
                    return new MultiprotocolReachableNlri(buffer, flags, peerInfo);
                }
            },
            MP_UNREACH_NLRI {
                @Override
                public Attribute parse(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
                    return new MultiprotocolUnreachableNlri(buffer, flags, peerInfo);
                }
            };

            public abstract Attribute parse(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException;

            private static Type from(final int type) {
                // See https://www.iana.org/assignments/bgp-parameters/bgp-parameters.xhtml for type mappings
                switch (type) {
                    case 1: return ORIGIN;
                    case 2: return AS_PATH;
                    case 3: return NEXT_HOP;
                    case 4: return MULTI_EXIT_DISC;
                    case 5: return LOCAL_PREF;
                    case 6: return ATOMIC_AGGREGATE;
                    case 7: return AGGREGATOR;
                    case 8: return COMMUNITY; // See RFC1997
                    case 9: return ORIGINATOR_ID; // See RFC4456
                    case 10: return CLUSTER_LIST; // See RFC4456
                    case 14: return MP_REACH_NLRI; // See RFC4760
                    case 15: return MP_UNREACH_NLRI; // See RFC4760
                    case 16: return EXTENDED_COMMUNITIES; // See RFC4360
                    case 20: return CONNECTOR_ATTRIBUTE; // See RFC6037
                    case 21: return AS_PATH_LIMIT; // See [draft-ietf-idr-as-pathlimit]
                    case 25: return EXTENDED_V6_COMMUNITIES; // See RFC5701
                    case 32: return LARGE_COMMUNITIES; // See RFC8092
                    case 128: return ATTR_SET; // See RFC6368
                    default:
                        BmpParser.RATE_LIMITED_LOG.debug("Unknown Update Packet Type: {}", type);
                        return UNKNOWN;
                }
            }
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("optional", this.optional)
                    .add("transitive", this.transitive)
                    .add("partial", this.partial)
                    .add("extended", this.extended)
                    .add("type", this.type)
                    .add("length", this.length)
                    .add("attribute", this.attribute)
                    .toString();
        }
    }

    public static Optional<UpdatePacket> parse(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
        final Header header = new Header(buffer);
        if (header.type != Header.Type.UPDATE) {
            BmpParser.RATE_LIMITED_LOG.debug("Expected Update Message, got: {}", header.type);
            skip(buffer, header.length - Header.SIZE);
            return Optional.empty();
        }

        return Optional.of(new UpdatePacket(header, slice(buffer, header.length - Header.SIZE), flags, peerInfo));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("header", this.header)
                .add("withdrawRoutes", this.withdrawRoutes)
                .add("pathAttributes", this.pathAttributes)
                .add("reachableRoutes", this.reachableRoutes)
                .toString();
    }
}
