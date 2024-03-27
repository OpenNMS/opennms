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

    public AppResources(final ByteBuf buffer) throws InvalidPacketException {
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
    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
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
