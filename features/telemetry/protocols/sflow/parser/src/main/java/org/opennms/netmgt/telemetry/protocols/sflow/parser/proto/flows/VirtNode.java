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

    public VirtNode(final ByteBuf buffer) throws InvalidPacketException {
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

    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("mhz", this.mhz);
        bsonWriter.writeInt64("cpus", this.cpus);
        bsonWriter.writeInt64("memory", this.memory.longValue());
        bsonWriter.writeInt64("memory_free", this.memory_free.longValue());
        bsonWriter.writeInt64("num_domains", this.num_domains);
        bsonWriter.writeEndDocument();
    }

}
