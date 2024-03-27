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

// struct bst_device_buffers {
//   int uc_pc;  /* unicast buffers percentage utilization */
//   int mc_pc;  /* multicast buffers percentage utilization */
// };

public class BstDeviceBuffers implements CounterData {
    public final Integer uc_pc;
    public final Integer mc_pc;

    public BstDeviceBuffers(final ByteBuf buffer) throws InvalidPacketException {
        this.uc_pc = BufferUtils.sint32(buffer);
        this.mc_pc = BufferUtils.sint32(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("uc_pc", this.uc_pc)
                .add("mc_pc", this.mc_pc)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt32("uc_pc", this.uc_pc);
        bsonWriter.writeInt32("mc_pc", this.mc_pc);
        bsonWriter.writeEndDocument();
    }
}
