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

// struct sfp {
//   unsigned int module_id;
//   unsigned int module_num_lanes;      /* total number of lanes in module */
//   unsigned int module_supply_voltage; /* millivolts */
//   int module_temperature;             /* thousandths of a degree Celsius */
//   lane lanes<>;
// };

public class Sfp {
    public final long module_id;
    public final long module_num_lanes;
    public final long module_supply_voltage;
    public final Integer module_temperature;
    public final Array<Lane> lanes;

    public Sfp(final ByteBuf buffer) throws InvalidPacketException {
        this.module_id = BufferUtils.uint32(buffer);
        this.module_num_lanes = BufferUtils.uint32(buffer);
        this.module_supply_voltage = BufferUtils.uint32(buffer);
        this.module_temperature = BufferUtils.sint32(buffer);
        this.lanes = new Array(buffer, Optional.empty(), Lane::new);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("module_id", this.module_id)
                .add("module_num_lanes", this.module_num_lanes)
                .add("module_supply_voltage", this.module_supply_voltage)
                .add("module_temperature", this.module_temperature)
                .add("lanes", this.lanes)
                .toString();
    }

    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("module_id", this.module_id);
        bsonWriter.writeInt64("module_num_lanes", this.module_num_lanes);
        bsonWriter.writeInt64("module_supply_voltage", this.module_supply_voltage);
        bsonWriter.writeInt32("module_temperature", this.module_temperature);
        bsonWriter.writeStartArray("lanes");
        for (final Lane lane : lanes) {
            lane.writeBson(bsonWriter, enr);
        }
        bsonWriter.writeEndArray();
        bsonWriter.writeEndDocument();
    }
}
