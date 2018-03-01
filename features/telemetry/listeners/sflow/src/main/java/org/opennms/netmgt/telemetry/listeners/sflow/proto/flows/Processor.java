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
import com.google.common.primitives.UnsignedLong;

// struct processor {
//    percentage cpu_5s;           /* 5 second average CPU utilization */
//    percentage cpu_1m;           /* 1 minute average CPU utilization */
//    percentage cpu_5m;           /* 5 minute average CPU utilization */
//    unsigned hyper total_memory; /* total memory (in bytes) */
//    unsigned hyper free_memory;  /* free memory (in bytes) */
// };

public class Processor implements CounterData {
    public final Percentage cpu_5s;
    public final Percentage cpu_1m;
    public final Percentage cpu_5m;
    public final UnsignedLong total_memory;
    public final UnsignedLong free_memory;

    public Processor(final ByteBuffer buffer) throws InvalidPacketException {
        this.cpu_5s = new Percentage(buffer);
        this.cpu_1m = new Percentage(buffer);
        this.cpu_5m = new Percentage(buffer);
        this.total_memory = BufferUtils.uint64(buffer);
        this.free_memory = BufferUtils.uint64(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("cpu_5s", this.cpu_5s)
                .add("cpu_1m", this.cpu_1m)
                .add("cpu_5m", this.cpu_5m)
                .add("total_memory", this.total_memory)
                .add("free_memory", this.free_memory)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeName("cpu_5s");
        this.cpu_5s.writeBson(bsonWriter);
        bsonWriter.writeName("cpu_1m");
        this.cpu_1m.writeBson(bsonWriter);
        bsonWriter.writeName("cpu_5m");
        this.cpu_5m.writeBson(bsonWriter);
        bsonWriter.writeInt64("total_memory", this.total_memory.longValue());
        bsonWriter.writeInt64("free_memory", this.free_memory.longValue());
        bsonWriter.writeEndDocument();
    }
}
