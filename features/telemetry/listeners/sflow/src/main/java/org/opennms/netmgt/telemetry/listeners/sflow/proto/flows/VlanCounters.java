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
import com.google.common.primitives.UnsignedLong;

// struct vlan_counters {
//   unsigned int vlan_id;
//   unsigned hyper octets;
//   unsigned int ucastPkts;
//   unsigned int multicastPkts;
//   unsigned int broadcastPkts;
//   unsigned int discards;
// };

public class VlanCounters implements CounterData {
    public final long vlan_id;
    public final UnsignedLong octets;
    public final long ucastPkts;
    public final long multicastPkts;
    public final long broadcastPkts;
    public final long discards;

    public VlanCounters(final ByteBuffer buffer) throws InvalidPacketException {
        this.vlan_id = BufferUtils.uint32(buffer);
        this.octets = BufferUtils.uint64(buffer);
        this.ucastPkts = BufferUtils.uint32(buffer);
        this.multicastPkts = BufferUtils.uint32(buffer);
        this.broadcastPkts = BufferUtils.uint32(buffer);
        this.discards = BufferUtils.uint32(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("vlan_id", this.vlan_id)
                .add("octets", this.octets)
                .add("ucastPkts", this.ucastPkts)
                .add("multicastPkts", this.multicastPkts)
                .add("broadcastPkts", this.broadcastPkts)
                .add("discards", this.discards)
                .toString();
    }

    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("vlan_id", this.vlan_id);
        bsonWriter.writeInt64("octets", this.octets.longValue());
        bsonWriter.writeInt64("ucastPkts", this.ucastPkts);
        bsonWriter.writeInt64("multicastPkts", this.multicastPkts);
        bsonWriter.writeInt64("broadcastPkts", this.broadcastPkts);
        bsonWriter.writeInt64("discards", this.discards);
        bsonWriter.writeEndDocument();
    }

}
