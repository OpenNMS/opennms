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

    public Processor(final ByteBuf buffer) throws InvalidPacketException {
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
    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeName("cpu_5s");
        this.cpu_5s.writeBson(bsonWriter, enr);
        bsonWriter.writeName("cpu_1m");
        this.cpu_1m.writeBson(bsonWriter, enr);
        bsonWriter.writeName("cpu_5m");
        this.cpu_5m.writeBson(bsonWriter, enr);
        bsonWriter.writeInt64("total_memory", this.total_memory.longValue());
        bsonWriter.writeInt64("free_memory", this.free_memory.longValue());
        bsonWriter.writeEndDocument();
    }
}
