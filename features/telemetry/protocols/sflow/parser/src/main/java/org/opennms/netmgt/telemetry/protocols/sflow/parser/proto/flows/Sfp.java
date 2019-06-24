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

package org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows;

import java.nio.ByteBuffer;
import java.util.Optional;

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.common.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.Array;

import com.google.common.base.MoreObjects;

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

    public Sfp(final ByteBuffer buffer) throws InvalidPacketException {
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

    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("module_id", this.module_id);
        bsonWriter.writeInt64("module_num_lanes", this.module_num_lanes);
        bsonWriter.writeInt64("module_supply_voltage", this.module_supply_voltage);
        bsonWriter.writeInt32("module_temperature", this.module_temperature);
        bsonWriter.writeStartArray("lanes");
        for (final Lane lane : lanes) {
            lane.writeBson(bsonWriter);
        }
        bsonWriter.writeEndArray();
        bsonWriter.writeEndDocument();
    }
}
