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

    public HostDiskIo(final ByteBuf buffer) throws InvalidPacketException {
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
    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("disk_total", this.disk_total.longValue());
        bsonWriter.writeInt64("disk_free", this.disk_free.longValue());
        bsonWriter.writeName("part_max_used");
        this.part_max_used.writeBson(bsonWriter, enr);
        bsonWriter.writeInt64("reads", this.reads);
        bsonWriter.writeInt64("bytes_read", this.bytes_read.longValue());
        bsonWriter.writeInt64("read_time", this.read_time);
        bsonWriter.writeInt64("writes", this.writes);
        bsonWriter.writeInt64("bytes_written", this.bytes_written.longValue());
        bsonWriter.writeInt64("write_time", this.write_time);
        bsonWriter.writeEndDocument();
    }
}
