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

// struct virt_node {
//    unsigned int mhz;           /* expected CPU frequency */
//    unsigned int cpus;          /* the number of active CPUs */
//    unsigned hyper memory;      /* memory size in bytes */
//    unsigned hyper memory_free; /* unassigned memory in bytes */
//    unsigned int num_domains;   /* number of active domains */
// };

public class VirtNode implements CounterData {
    public final long mhz;
    public final long cpus;
    public final UnsignedLong memory;
    public final UnsignedLong memory_free;
    public final long num_domains;

    public VirtNode(final ByteBuffer buffer) throws InvalidPacketException {
        this.mhz = BufferUtils.uint32(buffer);
        this.cpus = BufferUtils.uint32(buffer);
        this.memory = BufferUtils.uint64(buffer);
        this.memory_free = BufferUtils.uint64(buffer);
        this.num_domains = BufferUtils.uint32(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("mhz", this.mhz)
                .add("cpus", this.cpus)
                .add("memory", this.memory)
                .add("memory_free", this.memory_free)
                .add("num_domains", this.num_domains)
                .toString();
    }

    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("mhz", this.mhz);
        bsonWriter.writeInt64("cpus", this.cpus);
        bsonWriter.writeInt64("memory", this.memory.longValue());
        bsonWriter.writeInt64("memory_free", this.memory_free.longValue());
        bsonWriter.writeInt64("num_domains", this.num_domains);
        bsonWriter.writeEndDocument();
    }

}
