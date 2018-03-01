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

// struct ethernet_counters {
//    unsigned int dot3StatsAlignmentErrors;
//    unsigned int dot3StatsFCSErrors;
//    unsigned int dot3StatsSingleCollisionFrames;
//    unsigned int dot3StatsMultipleCollisionFrames;
//    unsigned int dot3StatsSQETestErrors;
//    unsigned int dot3StatsDeferredTransmissions;
//    unsigned int dot3StatsLateCollisions;
//    unsigned int dot3StatsExcessiveCollisions;
//    unsigned int dot3StatsInternalMacTransmitErrors;
//    unsigned int dot3StatsCarrierSenseErrors;
//    unsigned int dot3StatsFrameTooLongs;
//    unsigned int dot3StatsInternalMacReceiveErrors;
//    unsigned int dot3StatsSymbolErrors;
// };

public class EthernetCounters implements CounterData {
    public final long dot3StatsAlignmentErrors;
    public final long dot3StatsFCSErrors;
    public final long dot3StatsSingleCollisionFrames;
    public final long dot3StatsMultipleCollisionFrames;
    public final long dot3StatsSQETestErrors;
    public final long dot3StatsDeferredTransmissions;
    public final long dot3StatsLateCollisions;
    public final long dot3StatsExcessiveCollisions;
    public final long dot3StatsInternalMacTransmitErrors;
    public final long dot3StatsCarrierSenseErrors;
    public final long dot3StatsFrameTooLongs;
    public final long dot3StatsInternalMacReceiveErrors;
    public final long dot3StatsSymbolErrors;

    public EthernetCounters(final ByteBuffer buffer) throws InvalidPacketException {
        this.dot3StatsAlignmentErrors = BufferUtils.uint32(buffer);
        this.dot3StatsFCSErrors = BufferUtils.uint32(buffer);
        this.dot3StatsSingleCollisionFrames = BufferUtils.uint32(buffer);
        this.dot3StatsMultipleCollisionFrames = BufferUtils.uint32(buffer);
        this.dot3StatsSQETestErrors = BufferUtils.uint32(buffer);
        this.dot3StatsDeferredTransmissions = BufferUtils.uint32(buffer);
        this.dot3StatsLateCollisions = BufferUtils.uint32(buffer);
        this.dot3StatsExcessiveCollisions = BufferUtils.uint32(buffer);
        this.dot3StatsInternalMacTransmitErrors = BufferUtils.uint32(buffer);
        this.dot3StatsCarrierSenseErrors = BufferUtils.uint32(buffer);
        this.dot3StatsFrameTooLongs = BufferUtils.uint32(buffer);
        this.dot3StatsInternalMacReceiveErrors = BufferUtils.uint32(buffer);
        this.dot3StatsSymbolErrors = BufferUtils.uint32(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("dot3StatsAlignmentErrors", this.dot3StatsAlignmentErrors)
                .add("dot3StatsFCSErrors", this.dot3StatsFCSErrors)
                .add("dot3StatsSingleCollisionFrames", this.dot3StatsSingleCollisionFrames)
                .add("dot3StatsMultipleCollisionFrames", this.dot3StatsMultipleCollisionFrames)
                .add("dot3StatsSQETestErrors", this.dot3StatsSQETestErrors)
                .add("dot3StatsDeferredTransmissions", this.dot3StatsDeferredTransmissions)
                .add("dot3StatsLateCollisions", this.dot3StatsLateCollisions)
                .add("dot3StatsExcessiveCollisions", this.dot3StatsExcessiveCollisions)
                .add("dot3StatsInternalMacTransmitErrors", this.dot3StatsInternalMacTransmitErrors)
                .add("dot3StatsCarrierSenseErrors", this.dot3StatsCarrierSenseErrors)
                .add("dot3StatsFrameTooLongs", this.dot3StatsFrameTooLongs)
                .add("dot3StatsInternalMacReceiveErrors", this.dot3StatsInternalMacReceiveErrors)
                .add("dot3StatsSymbolErrors", this.dot3StatsSymbolErrors)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("dot3StatsAlignmentErrors", this.dot3StatsAlignmentErrors);
        bsonWriter.writeInt64("dot3StatsFCSErrors", this.dot3StatsFCSErrors);
        bsonWriter.writeInt64("dot3StatsSingleCollisionFrames", this.dot3StatsSingleCollisionFrames);
        bsonWriter.writeInt64("dot3StatsMultipleCollisionFrames", this.dot3StatsMultipleCollisionFrames);
        bsonWriter.writeInt64("dot3StatsSQETestErrors", this.dot3StatsSQETestErrors);
        bsonWriter.writeInt64("dot3StatsDeferredTransmissions", this.dot3StatsDeferredTransmissions);
        bsonWriter.writeInt64("dot3StatsLateCollisions", this.dot3StatsLateCollisions);
        bsonWriter.writeInt64("dot3StatsExcessiveCollisions", this.dot3StatsExcessiveCollisions);
        bsonWriter.writeInt64("dot3StatsInternalMacTransmitErrors", this.dot3StatsInternalMacTransmitErrors);
        bsonWriter.writeInt64("dot3StatsCarrierSenseErrors", this.dot3StatsCarrierSenseErrors);
        bsonWriter.writeInt64("dot3StatsFrameTooLongs", this.dot3StatsFrameTooLongs);
        bsonWriter.writeInt64("dot3StatsInternalMacReceiveErrors", this.dot3StatsInternalMacReceiveErrors);
        bsonWriter.writeInt64("dot3StatsSymbolErrors", this.dot3StatsSymbolErrors);
        bsonWriter.writeEndDocument();
    }
}
