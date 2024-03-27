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
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramVisitor;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

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

    public ExtendedSwitch(final ByteBuf buffer) throws InvalidPacketException {
        this.src_vlan = BufferUtils.uint32(buffer);
        this.src_priority = BufferUtils.uint32(buffer);
        this.dst_vlan = BufferUtils.uint32(buffer);
        this.dst_priority = BufferUtils.uint32(buffer);
    }

    public ExtendedSwitch(final long src_vlan, final long src_priority, final long dst_vlan, final long dst_priority) {
        this.src_vlan = src_vlan;
        this.src_priority = src_priority;
        this.dst_vlan = dst_vlan;
        this.dst_priority = dst_priority;
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
    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("src_vlan", this.src_vlan);
        bsonWriter.writeInt64("src_priority", this.src_priority);
        bsonWriter.writeInt64("dst_vlan", this.dst_vlan);
        bsonWriter.writeInt64("dst_priority", this.dst_priority);
        bsonWriter.writeEndDocument();
    }

    @Override
    public void visit(final SampleDatagramVisitor visitor) {
        visitor.accept(this);
    }
}
