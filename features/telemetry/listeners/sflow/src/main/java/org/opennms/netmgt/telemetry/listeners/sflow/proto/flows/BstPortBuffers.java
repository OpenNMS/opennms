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
import java.util.Optional;

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.Array;

import com.google.common.base.MoreObjects;

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

    public BstPortBuffers(final ByteBuffer buffer) throws InvalidPacketException {
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
    public void writeBson(final BsonWriter bsonWriter) {
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
