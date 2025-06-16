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

    public VlanCounters(final ByteBuf buffer) throws InvalidPacketException {
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

    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
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
