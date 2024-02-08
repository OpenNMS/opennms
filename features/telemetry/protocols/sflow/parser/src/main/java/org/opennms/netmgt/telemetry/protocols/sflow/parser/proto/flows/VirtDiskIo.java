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

// struct virt_disk_io {
//    unsigned hyper capacity;   /* logical size in bytes */
//    unsigned hyper allocation; /* current allocation in bytes */
//    unsigned hyper available;  /* remaining free bytes */
//    unsigned int rd_req;       /* number of read requests */
//    unsigned hyper rd_bytes;   /* number of read bytes */
//    unsigned int wr_req;       /* number of write requests */
//    unsigned hyper wr_bytes;   /* number of  written bytes */
//    unsigned int errs;         /* read/write errors */
// };

public class VirtDiskIo implements CounterData {
    public final UnsignedLong capacity;
    public final UnsignedLong allocation;
    public final UnsignedLong available;
    public final long rd_req;
    public final UnsignedLong rd_bytes;
    public final long wr_req;
    public final UnsignedLong wr_bytes;
    public final long errs;

    public VirtDiskIo(final ByteBuf buffer) throws InvalidPacketException {
        this.capacity = BufferUtils.uint64(buffer);
        this.allocation = BufferUtils.uint64(buffer);
        this.available = BufferUtils.uint64(buffer);
        this.rd_req = BufferUtils.uint32(buffer);
        this.rd_bytes = BufferUtils.uint64(buffer);
        this.wr_req = BufferUtils.uint32(buffer);
        this.wr_bytes = BufferUtils.uint64(buffer);
        this.errs = BufferUtils.uint32(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("capacity", this.capacity)
                .add("allocation", this.allocation)
                .add("available", this.available)
                .add("rd_req", this.rd_req)
                .add("rd_bytes", this.rd_bytes)
                .add("wr_req", this.wr_req)
                .add("wr_bytes", this.wr_bytes)
                .add("errs", this.errs)
                .toString();
    }

    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("capacity", this.capacity.longValue());
        bsonWriter.writeInt64("allocation", this.allocation.longValue());
        bsonWriter.writeInt64("available", this.available.longValue());
        bsonWriter.writeInt64("rd_req", this.rd_req);
        bsonWriter.writeInt64("rd_bytes", this.rd_bytes.longValue());
        bsonWriter.writeInt64("wr_req", this.wr_req);
        bsonWriter.writeInt64("wr_bytes", this.wr_bytes.longValue());
        bsonWriter.writeInt64("errs", this.errs);
        bsonWriter.writeEndDocument();
    }

}
