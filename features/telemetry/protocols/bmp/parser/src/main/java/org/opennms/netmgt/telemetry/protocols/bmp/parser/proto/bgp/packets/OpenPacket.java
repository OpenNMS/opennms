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

package org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets;

import static org.opennms.netmgt.telemetry.common.utils.BufferUtils.skip;
import static org.opennms.netmgt.telemetry.common.utils.BufferUtils.slice;
import static org.opennms.netmgt.telemetry.common.utils.BufferUtils.uint16;
import static org.opennms.netmgt.telemetry.common.utils.BufferUtils.uint32;
import static org.opennms.netmgt.telemetry.common.utils.BufferUtils.uint8;

import java.nio.ByteBuffer;
import java.util.Objects;

import org.opennms.netmgt.telemetry.protocols.bmp.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.Header;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.Packet;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerFlags;

import com.google.common.base.MoreObjects;

public class OpenPacket implements Packet {
    public final Header header;

    public final int version;  // uint8
    public final int as;       // uint16
    public final int holdTime; // uint16
    public final long id;      // uint32

    public OpenPacket(final Header header, final ByteBuffer buffer, final PeerFlags flags) {
        this.header = Objects.requireNonNull(header);

        this.version = uint8(buffer);
        this.as = uint16(buffer);
        this.holdTime = uint16(buffer);
        this.id = uint32(buffer);

        final int parametersLength = uint8(buffer);

        // Skip the parameters (see https://tools.ietf.org/html/rfc4271#section-4.2)
        skip(buffer, parametersLength);
    }

    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    public static OpenPacket parse(final ByteBuffer buffer, final PeerFlags flags) throws InvalidPacketException {
        final Header header = new Header(buffer);
        if (header.type != Header.Type.OPEN) {
            throw new InvalidPacketException(buffer, "Expected Open Message, got: {}", header.type);
        }

        return new OpenPacket(header, slice(buffer, header.length - Header.SIZE), flags);
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
