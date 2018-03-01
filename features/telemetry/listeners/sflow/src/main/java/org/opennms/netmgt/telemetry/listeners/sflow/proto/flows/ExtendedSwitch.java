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

package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.nio.ByteBuffer;

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;

import com.google.common.base.MoreObjects;

// struct extended_switch {
//    unsigned int src_vlan;     /* The 802.1Q VLAN id of incoming frame */
//    unsigned int src_priority; /* The 802.1p priority of incoming frame */
//    unsigned int dst_vlan;     /* The 802.1Q VLAN id of outgoing frame */
//    unsigned int dst_priority; /* The 802.1p priority of outgoing frame */
// };

public class ExtendedSwitch implements FlowData {
    public final long src_vlan;
    public final long src_priority;
    public final long dst_vlan;
    public final long dst_priority;

    public ExtendedSwitch(final ByteBuffer buffer) throws InvalidPacketException {
        this.src_vlan = BufferUtils.uint32(buffer);
        this.src_priority = BufferUtils.uint32(buffer);
        this.dst_vlan = BufferUtils.uint32(buffer);
        this.dst_priority = BufferUtils.uint32(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("src_vlan", this.src_vlan)
                .add("src_priority", this.src_priority)
                .add("dst_vlan", this.dst_vlan)
                .add("dst_priority", this.dst_priority)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("src_vlan", this.src_vlan);
        bsonWriter.writeInt64("src_priority", this.src_priority);
        bsonWriter.writeInt64("dst_vlan", this.dst_vlan);
        bsonWriter.writeInt64("dst_priority", this.dst_priority);
        bsonWriter.writeEndDocument();
    }
}
