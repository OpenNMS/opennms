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
package org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.repeatRemaining;

import java.util.Objects;
import java.util.Optional;

import org.opennms.netmgt.telemetry.protocols.bmp.parser.BmpParser;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.Header;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.Packet;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerAccessor;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerFlags;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerHeader;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerInfo;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.TLV;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.mirroring.BgpMessage;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.mirroring.Information;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.mirroring.Mirroring;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.mirroring.Unknown;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

public class RouteMirroringPacket implements Packet {

    public final Header header;
    public final PeerHeader peerHeader;
    public final TLV.List<Element, Element.Type, Mirroring> elements;

    public RouteMirroringPacket(final Header header, final ByteBuf buffer, final PeerAccessor peerAccessor) throws InvalidPacketException {
        this.header = Objects.requireNonNull(header);
        this.peerHeader = new PeerHeader(buffer);

        this.elements = TLV.List.wrap(repeatRemaining(buffer, elementBuffer -> new Element(elementBuffer, this.peerHeader.flags, peerAccessor.getPeerInfo(peerHeader))));
    }

    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <R> R map(final Mapper<R> mapper) {
        return mapper.map(this);
    }

    public static class Element extends TLV<Element.Type, Mirroring, PeerFlags> {

        public Element(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
            super(buffer, Element.Type::from, flags, peerInfo);
        }

        public enum Type implements TLV.Type<Mirroring, PeerFlags> {
            BGP_MESSAGE{
                @Override
                public Mirroring parse(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
                    return new BgpMessage(buffer, flags, peerInfo);
                }
            },
            INFORMATION{
                @Override
                public Mirroring parse(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
                    return new Information(buffer, flags, peerInfo);
                }
            },
            UNKNOWN{
                @Override
                public Mirroring parse(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
                    return new Unknown(buffer, flags, peerInfo);
                }
            };

            private static Type from(final int type) {
                switch (type) {
                    case 0: return BGP_MESSAGE;
                    case 1: return INFORMATION;
                    default:
                        BmpParser.RATE_LIMITED_LOG.debug("Unknown Route Mirroring Packet Type: {}", type);
                        return UNKNOWN;
                }
            }
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("header", this.header)
                .add("peerHeader", this.peerHeader)
                .add("elements", this.elements)
                .toString();
    }
}
