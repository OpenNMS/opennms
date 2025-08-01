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
package org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows;

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.listeners.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramEnrichment;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

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

    public EthernetCounters(final ByteBuf buffer) throws InvalidPacketException {
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
    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
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
