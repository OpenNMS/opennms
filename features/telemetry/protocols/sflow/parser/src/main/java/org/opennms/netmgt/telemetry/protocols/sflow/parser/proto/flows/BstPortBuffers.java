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

import java.util.Optional;

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.listeners.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramEnrichment;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.Array;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

// struct bst_port_buffers {
//   int ingress_uc_pc;         /* ingress unicast buffers utilization */
//   int ingress_mc_pc;         /* ingress multicast buffers utilization */
//   int egress_uc_pc;          /* egress unicast buffers utilization */
//   int egress_mc_pc;          /* egress multicast buffers utilization */
//   int egress_queue_uc_pc<8>; /* per egress queue unicast buffers utilization */
//   int egress_queue_mc_pc<8>; /* per egress queue multicast buffers utilization*/
// };

public class BstPortBuffers implements CounterData {
    public final Integer ingress_uc_pc;
    public final Integer ingress_mc_pc;
    public final Integer egress_uc_pc;
    public final Integer egress_mc_pc;
    public final Array<Integer> egress_queue_uc_pc;
    public final Array<Integer> egress_queue_mc_pc;

    public BstPortBuffers(final ByteBuf buffer) throws InvalidPacketException {
        this.ingress_uc_pc = BufferUtils.sint32(buffer);
        this.ingress_mc_pc = BufferUtils.sint32(buffer);
        this.egress_uc_pc = BufferUtils.sint32(buffer);
        this.egress_mc_pc = BufferUtils.sint32(buffer);
        this.egress_queue_uc_pc = new Array(buffer, Optional.of(8), BufferUtils::sint32);
        this.egress_queue_mc_pc = new Array(buffer, Optional.of(8), BufferUtils::sint32);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("ingress_uc_pc", this.ingress_uc_pc)
                .add("ingress_mc_pc", this.ingress_mc_pc)
                .add("egress_uc_pc", this.egress_uc_pc)
                .add("egress_mc_pc", this.egress_mc_pc)
                .add("egress_queue_uc_pc", this.egress_queue_uc_pc)
                .add("egress_queue_mc_pc", this.egress_queue_mc_pc)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();

        bsonWriter.writeInt32("ingress_uc_pc", this.ingress_uc_pc);
        bsonWriter.writeInt32("ingress_mc_pc", this.ingress_mc_pc);
        bsonWriter.writeInt32("egress_uc_pc", this.egress_uc_pc);
        bsonWriter.writeInt32("egress_mc_pc", this.egress_mc_pc);

        bsonWriter.writeStartArray("egress_queue_uc_pc");
        for (final int i : this.egress_queue_uc_pc) {
            bsonWriter.writeInt32(i);
        }
        bsonWriter.writeEndArray();

        bsonWriter.writeStartArray("egress_queue_mc_pc");
        for (final int i : this.egress_queue_mc_pc) {
            bsonWriter.writeInt32(i);
        }
        bsonWriter.writeEndArray();

        bsonWriter.writeEndDocument();
    }
}
