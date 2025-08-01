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
import com.google.common.primitives.UnsignedLong;

import io.netty.buffer.ByteBuf;

// struct vg_counters {
//   unsigned int dot12InHighPriorityFrames;
//   unsigned hyper dot12InHighPriorityOctets;
//   unsigned int dot12InNormPriorityFrames;
//   unsigned hyper dot12InNormPriorityOctets;
//   unsigned int dot12InIPMErrors;
//   unsigned int dot12InOversizeFrameErrors;
//   unsigned int dot12InDataErrors;
//   unsigned int dot12InNullAddressedFrames;
//   unsigned int dot12OutHighPriorityFrames;
//   unsigned hyper dot12OutHighPriorityOctets;
//   unsigned int dot12TransitionIntoTrainings;
//   unsigned hyper dot12HCInHighPriorityOctets;
//   unsigned hyper dot12HCInNormPriorityOctets;
//   unsigned hyper dot12HCOutHighPriorityOctets;
// };

public class VgCounters implements CounterData {
    public final long dot12InHighPriorityFrames;
    public final UnsignedLong dot12InHighPriorityOctets;
    public final long dot12InNormPriorityFrames;
    public final UnsignedLong dot12InNormPriorityOctets;
    public final long dot12InIPMErrors;
    public final long dot12InOversizeFrameErrors;
    public final long dot12InDataErrors;
    public final long dot12InNullAddressedFrames;
    public final long dot12OutHighPriorityFrames;
    public final UnsignedLong dot12OutHighPriorityOctets;
    public final long dot12TransitionIntoTrainings;
    public final UnsignedLong dot12HCInHighPriorityOctets;
    public final UnsignedLong dot12HCInNormPriorityOctets;
    public final UnsignedLong dot12HCOutHighPriorityOctets;

    public VgCounters(final ByteBuf buffer) throws InvalidPacketException {
        this.dot12InHighPriorityFrames = BufferUtils.uint32(buffer);
        this.dot12InHighPriorityOctets = BufferUtils.uint64(buffer);
        this.dot12InNormPriorityFrames = BufferUtils.uint32(buffer);
        this.dot12InNormPriorityOctets = BufferUtils.uint64(buffer);
        this.dot12InIPMErrors = BufferUtils.uint32(buffer);
        this.dot12InOversizeFrameErrors = BufferUtils.uint32(buffer);
        this.dot12InDataErrors = BufferUtils.uint32(buffer);
        this.dot12InNullAddressedFrames = BufferUtils.uint32(buffer);
        this.dot12OutHighPriorityFrames = BufferUtils.uint32(buffer);
        this.dot12OutHighPriorityOctets = BufferUtils.uint64(buffer);
        this.dot12TransitionIntoTrainings = BufferUtils.uint32(buffer);
        this.dot12HCInHighPriorityOctets = BufferUtils.uint64(buffer);
        this.dot12HCInNormPriorityOctets = BufferUtils.uint64(buffer);
        this.dot12HCOutHighPriorityOctets = BufferUtils.uint64(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("dot12InHighPriorityFrames", this.dot12InHighPriorityFrames)
                .add("dot12InHighPriorityOctets", this.dot12InHighPriorityOctets)
                .add("dot12InNormPriorityFrames", this.dot12InNormPriorityFrames)
                .add("dot12InNormPriorityOctets", this.dot12InNormPriorityOctets)
                .add("dot12InIPMErrors", this.dot12InIPMErrors)
                .add("dot12InOversizeFrameErrors", this.dot12InOversizeFrameErrors)
                .add("dot12InDataErrors", this.dot12InDataErrors)
                .add("dot12InNullAddressedFrames", this.dot12InNullAddressedFrames)
                .add("dot12OutHighPriorityFrames", this.dot12OutHighPriorityFrames)
                .add("dot12OutHighPriorityOctets", this.dot12OutHighPriorityOctets)
                .add("dot12TransitionIntoTrainings", this.dot12TransitionIntoTrainings)
                .add("dot12HCInHighPriorityOctets", this.dot12HCInHighPriorityOctets)
                .add("dot12HCInNormPriorityOctets", this.dot12HCInNormPriorityOctets)
                .add("dot12HCOutHighPriorityOctets", this.dot12HCOutHighPriorityOctets)
                .toString();
    }

    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("dot12InHighPriorityFrames", this.dot12InHighPriorityFrames);
        bsonWriter.writeInt64("dot12InHighPriorityOctets", this.dot12InHighPriorityOctets.longValue());
        bsonWriter.writeInt64("dot12InNormPriorityFrames", this.dot12InNormPriorityFrames);
        bsonWriter.writeInt64("dot12InNormPriorityOctets", this.dot12InNormPriorityOctets.longValue());
        bsonWriter.writeInt64("dot12InIPMErrors", this.dot12InIPMErrors);
        bsonWriter.writeInt64("dot12InOversizeFrameErrors", this.dot12InOversizeFrameErrors);
        bsonWriter.writeInt64("dot12InDataErrors", this.dot12InDataErrors);
        bsonWriter.writeInt64("dot12InNullAddressedFrames", this.dot12InNullAddressedFrames);
        bsonWriter.writeInt64("dot12OutHighPriorityFrames", this.dot12OutHighPriorityFrames);
        bsonWriter.writeInt64("dot12OutHighPriorityOctets", this.dot12OutHighPriorityOctets.longValue());
        bsonWriter.writeInt64("dot12TransitionIntoTrainings", this.dot12TransitionIntoTrainings);
        bsonWriter.writeInt64("dot12HCInHighPriorityOctets", this.dot12HCInHighPriorityOctets.longValue());
        bsonWriter.writeInt64("dot12HCInNormPriorityOctets", this.dot12HCInNormPriorityOctets.longValue());
        bsonWriter.writeInt64("dot12HCOutHighPriorityOctets", this.dot12HCOutHighPriorityOctets.longValue());
        bsonWriter.writeEndDocument();
    }
}
