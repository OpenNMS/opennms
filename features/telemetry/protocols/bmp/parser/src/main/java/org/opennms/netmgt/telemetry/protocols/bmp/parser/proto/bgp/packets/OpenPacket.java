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
package org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.bytes;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.repeatRemaining;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.skip;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.slice;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint16;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint8;

import java.net.InetAddress;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.BmpParser;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.Header;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.Packet;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerFlags;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerInfo;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

public class OpenPacket implements Packet {
    public final Header header;

    public final int version;    // uint8
    public final int as;         // uint16
    public final int holdTime;   // uint16
    public final InetAddress id; // uint32
    public final List<Parameter> parameters;
    public final List<Capability> capabilities;

    public static class Parameter {
        final int type, length;
        final ByteBuf value;

        private Parameter(final ByteBuf buffer) {
            this.type = uint8(buffer);
            this.length = uint8(buffer);
            this.value = slice(buffer, length);
        }

        public int getType() {
            return type;
        }

        public int getLength() {
            return length;
        }

        public ByteBuf getValue() {
            return value;
        }
    }

    public OpenPacket(final Header header, final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) {
        this.header = Objects.requireNonNull(header);

        this.version = uint8(buffer);
        this.as = uint16(buffer);
        this.holdTime = uint16(buffer);
        this.id = InetAddressUtils.getInetAddress(bytes(buffer, 4));

        final int parametersLength = uint8(buffer);

        // see https://tools.ietf.org/html/rfc4271#section-4.2
        this.parameters = repeatRemaining(slice(buffer, parametersLength), Parameter::new);
        // see https://tools.ietf.org/html/rfc3392
        this.capabilities = this.parameters.stream().filter(p -> p.type == 2).flatMap(p -> repeatRemaining(slice(p.value, p.length), Capability::new).stream()).collect(Collectors.toList());
    }

    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    public static Optional<OpenPacket> parse(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
        final Header header = new Header(buffer);
        if (header.type != Header.Type.OPEN) {
            BmpParser.RATE_LIMITED_LOG.debug("Expected Open Message, got: {}", header.type);
            skip(buffer, header.length - Header.SIZE);
            return Optional.empty();
        }

        return Optional.of(new OpenPacket(header, slice(buffer, header.length - Header.SIZE), flags, peerInfo));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("header", this.header)
                .add("version", this.version)
                .add("as", this.as)
                .add("holdTime", this.holdTime)
                .add("id", this.id)
                .toString();
    }
}
