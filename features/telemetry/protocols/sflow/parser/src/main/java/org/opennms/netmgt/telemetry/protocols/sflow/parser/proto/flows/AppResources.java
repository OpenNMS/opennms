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

// struct app_resources {
//   unsigned int user_time;      /* time spent executing application user
//                                   instructions (in milliseconds) */
//   unsigned int system_time;    /* time spent in operating system on behalf
//                                   of application (in milliseconds) */
//   unsigned hyper mem_used;     /* memory used in bytes */
//   unsigned hyper mem_max;      /* max. memory in bytes */
//   unsigned int fd_open;        /* number of open file descriptors */
//   unsigned int fd_max;         /* max. number of file descriptors */
//   unsigned int conn_open;      /* number of open network connections */
//   unsigned int conn_max;       /* max. number of network connections */
// };

public class AppResources implements CounterData {
    public final long user_time;
    public final long system_time;
    public final UnsignedLong mem_used;
    public final UnsignedLong mem_max;
    public final long fd_open;
    public final long fd_max;
    public final long conn_open;
    public final long conn_max;

    public AppResources(final ByteBuffer buffer) throws InvalidPacketException {
        this.user_time = BufferUtils.uint32(buffer);
        this.system_time = BufferUtils.uint32(buffer);
        this.mem_used = BufferUtils.uint64(buffer);
        this.mem_max = BufferUtils.uint64(buffer);
        this.fd_open = BufferUtils.uint32(buffer);
        this.fd_max = BufferUtils.uint32(buffer);
        this.conn_open = BufferUtils.uint32(buffer);
        this.conn_max = BufferUtils.uint32(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("user_time", this.user_time)
                .add("system_time", this.system_time)
                .add("mem_used", this.mem_used)
                .add("mem_max", this.mem_max)
                .add("fd_open", this.fd_open)
                .add("fd_max", this.fd_max)
                .add("conn_open", this.conn_open)
                .add("conn_max", this.conn_max)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("user_time", this.user_time);
        bsonWriter.writeInt64("system_time", this.system_time);
        bsonWriter.writeInt64("mem_used", this.mem_used.longValue());
        bsonWriter.writeInt64("mem_max", this.mem_max.longValue());
        bsonWriter.writeInt64("fd_open", this.fd_open);
        bsonWriter.writeInt64("fd_max", this.fd_max);
        bsonWriter.writeInt64("conn_open", this.conn_open);
        bsonWriter.writeInt64("conn_max", this.conn_max);
        bsonWriter.writeEndDocument();
    }
}
