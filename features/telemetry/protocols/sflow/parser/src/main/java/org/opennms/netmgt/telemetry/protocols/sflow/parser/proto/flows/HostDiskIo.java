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

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.common.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;

import com.google.common.base.MoreObjects;
import com.google.common.primitives.UnsignedLong;

// struct host_disk_io {
//    unsigned hyper disk_total;    /* total disk size in bytes */
//    unsigned hyper disk_free;     /* total disk free in bytes */
//    percentage  part_max_used;    /* utilization of most utilized partition */
//    unsigned int reads;           /* reads issued */
//    unsigned hyper bytes_read;    /* bytes read */
//    unsigned int read_time;       /* read time (ms) */
//    unsigned int writes;          /* writes completed */
//    unsigned hyper bytes_written; /* bytes written */
//    unsigned int write_time;      /* write time (ms) */
// };

public class HostDiskIo implements CounterData {
    public final UnsignedLong disk_total;
    public final UnsignedLong disk_free;
    public final Percentage part_max_used;
    public final long reads;
    public final UnsignedLong bytes_read;
    public final long read_time;
    public final long writes;
    public final UnsignedLong bytes_written;
    public final long write_time;

    public HostDiskIo(final ByteBuffer buffer) throws InvalidPacketException {
        this.disk_total = BufferUtils.uint64(buffer);
        this.disk_free = BufferUtils.uint64(buffer);
        this.part_max_used = new Percentage(buffer);
        this.reads = BufferUtils.uint32(buffer);
        this.bytes_read = BufferUtils.uint64(buffer);
        this.read_time = BufferUtils.uint32(buffer);
        this.writes = BufferUtils.uint32(buffer);
        this.bytes_written = BufferUtils.uint64(buffer);
        this.write_time = BufferUtils.uint32(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("disk_total", this.disk_total)
                .add("disk_free", this.disk_free)
                .add("part_max_used", this.part_max_used)
                .add("reads", this.reads)
                .add("bytes_read", this.bytes_read)
                .add("read_time", this.read_time)
                .add("writes", this.writes)
                .add("bytes_written", this.bytes_written)
                .add("write_time", this.write_time)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("disk_total", this.disk_total.longValue());
        bsonWriter.writeInt64("disk_free", this.disk_free.longValue());
        bsonWriter.writeName("part_max_used");
        this.part_max_used.writeBson(bsonWriter);
        bsonWriter.writeInt64("reads", this.reads);
        bsonWriter.writeInt64("bytes_read", this.bytes_read.longValue());
        bsonWriter.writeInt64("read_time", this.read_time);
        bsonWriter.writeInt64("writes", this.writes);
        bsonWriter.writeInt64("bytes_written", this.bytes_written.longValue());
        bsonWriter.writeInt64("write_time", this.write_time);
        bsonWriter.writeEndDocument();
    }
}
