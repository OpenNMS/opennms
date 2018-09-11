/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.headers;

import java.nio.ByteBuffer;

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.common.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;

public class EthernetHeader {

    public final Integer vlan;

    public final Inet4Header inet4Header;
    public final Inet6Header inet6Header;

    public final byte[] rawHeader;

    public EthernetHeader(final ByteBuffer buffer) throws InvalidPacketException {
        BufferUtils.skip(buffer, 6); // dstMAC
        BufferUtils.skip(buffer, 6); // srcMAC

        int type = BufferUtils.uint16(buffer);
        if (type == 0x8100) {
            // 802.1Q (VLAN-Tagging)
            this.vlan = BufferUtils.uint16(buffer) & 0x0fff;
            type = BufferUtils.uint16(buffer);
        } else {
            this.vlan = null;
        }

        switch (type) {
            case 0x0800: // IPv4
                this.inet4Header = new Inet4Header(buffer);
                this.inet6Header = null;
                this.rawHeader = null;
                break;

            case 0x86DD: // IPv6
                this.inet4Header = null;
                this.inet6Header = new Inet6Header(buffer);
                this.rawHeader = null;
                break;

            default:
                this.inet4Header = null;
                this.inet6Header = null;
                this.rawHeader = BufferUtils.bytes(buffer, buffer.remaining());
        }
    }

    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();
        if (this.vlan != null) {
            bsonWriter.writeInt32("vlan", this.vlan);
        }

        bsonWriter.writeEndDocument();
    }
}
