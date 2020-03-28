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

package org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.repeatRemaining;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint32;

import java.util.List;
import java.util.Optional;

import org.opennms.netmgt.telemetry.protocols.bmp.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.UpdatePacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerFlags;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerInfo;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

/**
 * From RFC6368:
 *
 * The attribute value consists of a 4-octet "Origin AS" value followed
 * by a variable-length field that conforms to the BGP UPDATE message
 * path attribute encoding rules.  The length of this attribute is 4
 * plus the total length of the encoded attributes.
 *
 *                       +------------------------------+
 *                       | Origin AS (4 octets)         |
 *                       +------------------------------+
 *                       | Path Attributes (variable)   |
 *                       +------------------------------+
 *
 */
public class AttrSet implements Attribute {
    public final long originAs;
    public final List<UpdatePacket.PathAttribute> pathAttributes;

    public AttrSet(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
        this.originAs = uint32(buffer);
        this.pathAttributes = repeatRemaining(buffer, pathAttributeBuffer -> new UpdatePacket.PathAttribute(pathAttributeBuffer, flags, peerInfo));
    }

    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("originAs", this.originAs)
                .add("pathAttributes", this.pathAttributes)
                .toString();
    }
}
