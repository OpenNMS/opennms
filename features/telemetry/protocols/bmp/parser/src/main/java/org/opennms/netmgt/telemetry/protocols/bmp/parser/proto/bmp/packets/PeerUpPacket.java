/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
