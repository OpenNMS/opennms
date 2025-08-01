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
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.AsciiString;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

// struct memcache_operation {
//   memcache_protocol protocol;  /* protocol */
//   memcache_cmd cmd;            /* command */
//   string key<255>;             /* key used to store/retrieve data */
//   unsigned int nkeys;          /* number of keys
//                                   (including sampled key) */
//   unsigned int value_bytes;    /* size of the value (in bytes) */
//   unsigned int uS;             /* duration of the operation
//                                   (in microseconds) */
//   memcache_status status;      /* status of command */
// };

public class MemcacheOperation implements CounterData {
    public final MemcacheProtocol protocol;
    public final MemcacheCmd cmd;
    public final AsciiString key;
    public final long nkeys;
    public final long value_bytes;
    public final long uS;
    public final MemcacheStatus status;

    public MemcacheOperation(final ByteBuf buffer) throws InvalidPacketException {
        this.protocol = MemcacheProtocol.from(buffer);
        this.cmd = MemcacheCmd.from(buffer);
        this.key = new AsciiString(buffer);
        this.nkeys = BufferUtils.uint32(buffer);
        this.value_bytes = BufferUtils.uint32(buffer);
        this.uS = BufferUtils.uint32(buffer);
        this.status = MemcacheStatus.from(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("protocol", this.protocol)
                .add("cmd", this.cmd)
                .add("key", this.key)
                .add("nkeys", this.nkeys)
                .add("value_bytes", this.value_bytes)
                .add("uS", this.uS)
                .add("status", this.status)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeName("protocol");
        this.protocol.writeBson(bsonWriter, enr);
        bsonWriter.writeName("cmd");
        this.cmd.writeBson(bsonWriter, enr);
        bsonWriter.writeString("key", this.key.value);
        bsonWriter.writeInt64("nkeys", this.nkeys);
        bsonWriter.writeInt64("value_bytes", this.value_bytes);
        bsonWriter.writeInt64("uS", this.uS);
        bsonWriter.writeName("status");
        this.status.writeBson(bsonWriter, enr);
        bsonWriter.writeEndDocument();
    }
}
