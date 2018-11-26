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

    public VirtDiskIo(final ByteBuffer buffer) throws InvalidPacketException {
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

    public void writeBson(final BsonWriter bsonWriter) {
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
