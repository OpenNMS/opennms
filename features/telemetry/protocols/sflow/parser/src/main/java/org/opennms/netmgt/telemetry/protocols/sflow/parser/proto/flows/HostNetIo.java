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

// struct host_net_io {
//    unsigned hyper bytes_in;  /* total bytes in */
//    unsigned int pkts_in;     /* total packets in */
//    unsigned int errs_in;     /* total errors in */
//    unsigned int drops_in;    /* total drops in */
//    unsigned hyper bytes_out; /* total bytes out */
//    unsigned int packets_out; /* total packets out */
//    unsigned int errs_out;    /* total errors out */
//    unsigned int drops_out;   /* total drops out */
// };

public class HostNetIo implements CounterData {
    public final UnsignedLong bytes_in;
    public final long pkts_in;
    public final long errs_in;
    public final long drops_in;
    public final UnsignedLong bytes_out;
    public final long packets_out;
    public final long errs_out;
    public final long drops_out;

    public HostNetIo(final ByteBuf buffer) throws InvalidPacketException {
        this.bytes_in = BufferUtils.uint64(buffer);
        this.pkts_in = BufferUtils.uint32(buffer);
        this.errs_in = BufferUtils.uint32(buffer);
        this.drops_in = BufferUtils.uint32(buffer);
        this.bytes_out = BufferUtils.uint64(buffer);
        this.packets_out = BufferUtils.uint32(buffer);
        this.errs_out = BufferUtils.uint32(buffer);
        this.drops_out = BufferUtils.uint32(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("bytes_in", this.bytes_in)
                .add("pkts_in", this.pkts_in)
                .add("errs_in", this.errs_in)
                .add("drops_in", this.drops_in)
                .add("bytes_out", this.bytes_out)
                .add("packets_out", this.packets_out)
                .add("errs_out", this.errs_out)
                .add("drops_out", this.drops_out)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("bytes_in", this.bytes_in.longValue());
        bsonWriter.writeInt64("pkts_in", this.pkts_in);
        bsonWriter.writeInt64("errs_in", this.errs_in);
        bsonWriter.writeInt64("drops_in", this.drops_in);
        bsonWriter.writeInt64("bytes_out", this.bytes_out.longValue());
        bsonWriter.writeInt64("packets_out", this.packets_out);
        bsonWriter.writeInt64("errs_out", this.errs_out);
        bsonWriter.writeInt64("drops_out", this.drops_out);
        bsonWriter.writeEndDocument();
    }
}
