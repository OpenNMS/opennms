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

import java.nio.ByteBuffer;
import java.util.Objects;

import org.opennms.netmgt.telemetry.protocols.bmp.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.UpdatePacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.Header;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.Packet;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerHeader;

import com.google.common.base.MoreObjects;

public class RouteMonitoringPacket implements Packet {
    public final Header header;
    public final PeerHeader peerHeader;

    public final UpdatePacket updateMessage;

    public RouteMonitoringPacket(final Header header, final ByteBuffer buffer) throws InvalidPacketException {
        this.header = Objects.requireNonNull(header);
        this.peerHeader = new PeerHeader(buffer);

        this.updateMessage = UpdatePacket.parse(buffer, this.peerHeader.flags);
    }

    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("header", this.header)
                .add("peerHeader", this.peerHeader)
                .add("updateMessage", this.updateMessage)
                .toString();
    }
}
