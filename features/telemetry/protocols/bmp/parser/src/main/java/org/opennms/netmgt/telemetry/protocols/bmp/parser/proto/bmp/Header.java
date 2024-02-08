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
package org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint32;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint8;

import java.util.Objects;
import java.util.function.Function;

import org.opennms.netmgt.telemetry.protocols.bmp.parser.BmpParser;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.InitiationPacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.PeerDownPacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.PeerUpPacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.RouteMirroringPacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.RouteMonitoringPacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.StatisticsReportPacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.TerminationPacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.UnknownPacket;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

public class Header {
    public static final int SIZE = 1 + 4 + 1;

    public static final int VERSION = 3;

    public final int version; // uint8 - must be 3
    public final int length;  // uint32
    public final Type type;   // uint8

    public Header(final ByteBuf buffer) throws InvalidPacketException {
        this.version = uint8(buffer);
        if (this.version != VERSION) {
            throw new InvalidPacketException(buffer, "Invalid version number: 0x%04X", this.version);
        }

        this.length = (int) uint32(buffer);
        if (this.length == 0) {
            throw new InvalidPacketException(buffer, "Zero length record");
        }

        this.type = Type.from(buffer);
    }

    public int payloadLength() {
        return this.length - Header.SIZE;
    }

    public Packet parsePayload(final ByteBuf buffer, final PeerAccessor peerAccessor) throws InvalidPacketException {
        return this.type.parse(this, buffer, peerAccessor);
    }

    public enum Type {
        ROUTE_MONITORING(RouteMonitoringPacket::new),
        STATISTICS_REPORT(StatisticsReportPacket::new),
        PEER_DOWN_NOTIFICATION(PeerDownPacket::new),
        PEER_UP_NOTIFICATION(PeerUpPacket::new),
        INITIATION_MESSAGE(InitiationPacket::new),
        TERMINATION_MESSAGE(TerminationPacket::new),
        ROUTE_MIRRORING_MESSAGE(RouteMirroringPacket::new),
        UNKNOWN(UnknownPacket::new),
        ;

        private final Packet.Parser parser;

        Type(final Packet.Parser parser) {
            this.parser = Objects.requireNonNull(parser);
        }

        private Packet parse(final Header header, final ByteBuf buffer, final PeerAccessor peerAccessor) throws InvalidPacketException {
            return this.parser.parse(header, buffer, peerAccessor);
        }

        private static Type from(final ByteBuf buffer) throws InvalidPacketException {
            final int type = uint8(buffer);
            switch (type) {
                case 0:
                    return ROUTE_MONITORING;
                case 1:
                    return STATISTICS_REPORT;
                case 2:
                    return PEER_DOWN_NOTIFICATION;
                case 3:
                    return PEER_UP_NOTIFICATION;
                case 4:
                    return INITIATION_MESSAGE;
                case 5:
                    return TERMINATION_MESSAGE;
                case 6:
                    return ROUTE_MIRRORING_MESSAGE;
                default:
                    BmpParser.RATE_LIMITED_LOG.debug("Unknown BMP Packet Type: {}", type);
                    return UNKNOWN;
            }
        }

        public <R> R map(final Function<Type, R> mapper) {
            return mapper.apply(this);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("version", version)
                .add("length", length)
                .add("type", type)
                .toString();
    }
}
