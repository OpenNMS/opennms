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
package org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.bytes;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint16;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint8;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import org.opennms.netmgt.telemetry.protocols.bmp.parser.BmpParser;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.KeepalivePacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.NotificationPacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.OpenPacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.UnknownPacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.UpdatePacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerFlags;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerInfo;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

public class Header {
    public static final int SIZE = 16 + 2 + 1;

    private static final byte[] EXPECTED_MARKER = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}; // A 16-byte array with all bits set to one

    public final int length;
    public final Type type;

    public Header(final ByteBuf buffer) throws InvalidPacketException {
        final byte[] marker = bytes(buffer, 16);
        if (!Arrays.equals(marker, EXPECTED_MARKER)) {
            throw new InvalidPacketException(buffer, "Invalid BGP packet marker");
        }

        this.length = uint16(buffer);
        this.type = Type.from(buffer);
    }

    public Packet parsePayload(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
        return this.type.parse(this, buffer, flags, peerInfo);
    }

    public enum Type {
        OPEN(OpenPacket::new),
        UPDATE(UpdatePacket::new),
        NOTIFICATION(NotificationPacket::new),
        KEEPALIVE(KeepalivePacket::new),
        UNKNOWN(UnknownPacket::new),
        ;

        private final Packet.Parser parser;

        Type(final Packet.Parser parser) {
            this.parser = Objects.requireNonNull(parser);
        }

        private static Type from(final ByteBuf buffer) throws InvalidPacketException {
            final int type = uint8(buffer);
            switch (type) {
                case 1:
                    return OPEN;
                case 2:
                    return UPDATE;
                case 3:
                    return NOTIFICATION;
                case 4:
                    return KEEPALIVE;
                default:
                    BmpParser.RATE_LIMITED_LOG.debug("Unknown BGP Packet Type: {}", type);
                    return UNKNOWN;
            }
        }

        private Packet parse(final Header header, final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
            return this.parser.parse(header, buffer, flags, peerInfo);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("length", this.length)
                .add("type", this.type)
                .toString();
    }
}
