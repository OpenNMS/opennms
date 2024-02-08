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
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint16;

import java.net.InetAddress;
import java.util.Objects;
import java.util.Optional;

import org.opennms.netmgt.telemetry.protocols.bmp.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.OpenPacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.Header;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.InformationElement;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.Packet;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerAccessor;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerHeader;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.TLV;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

public class PeerUpPacket implements Packet {
    public final Header header;
    public final PeerHeader peerHeader;

    public final InetAddress localAddress;
    public final int localPort;  // uint16
    public final int remotePort; // uint16

    public final Optional<OpenPacket> sendOpenMessage;
    public final Optional<OpenPacket> recvOpenMessage;

    public final TLV.List<InformationElement, InformationElement.Type, String> information;

    public PeerUpPacket(final Header header, final ByteBuf buffer, final PeerAccessor peerAccessor) throws InvalidPacketException {
        this.header = Objects.requireNonNull(header);
        this.peerHeader = new PeerHeader(buffer);

        this.localAddress = this.peerHeader.flags.parsePaddedAddress(buffer);
        this.localPort = uint16(buffer);
        this.remotePort = uint16(buffer);

        this.sendOpenMessage = OpenPacket.parse(buffer, this.peerHeader.flags, peerAccessor.getPeerInfo(peerHeader));
        this.recvOpenMessage = OpenPacket.parse(buffer, this.peerHeader.flags, peerAccessor.getPeerInfo(peerHeader));

        this.information = TLV.List.wrap(repeatRemaining(buffer, b -> new InformationElement(b, peerAccessor.getPeerInfo(peerHeader))));
    }

    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <R> R map(final Mapper<R> mapper) {
        return mapper.map(this);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("header", this.header)
                .add("peerHeader", this.peerHeader)
                .add("localAddress", this.localAddress)
                .add("localPort", this.localPort)
                .add("remotePort", this.remotePort)
                .add("sendOpenMessage", this.sendOpenMessage)
                .add("recvOpenMessage", this.recvOpenMessage)
                .add("information", this.information)
                .toString();
    }
}
