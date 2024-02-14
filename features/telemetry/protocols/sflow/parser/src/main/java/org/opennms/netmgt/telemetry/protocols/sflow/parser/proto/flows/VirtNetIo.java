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

// struct virt_net_io {
//    unsigned hyper rx_bytes;  /* total bytes received */
//    unsigned int rx_packets;  /* total packets received */
//    unsigned int rx_errs;     /* total receive errors */
//    unsigned int rx_drop;     /* total receive drops */
//    unsigned hyper tx_bytes;  /* total bytes transmitted */
//    unsigned int tx_packets;  /* total packets transmitted */
//    unsigned int tx_errs;     /* total transmit errors */
//    unsigned int tx_drop;     /* total transmit drops */
// };

public class VirtNetIo implements CounterData {
    public final UnsignedLong rx_bytes;
    public final long rx_packets;
    public final long rx_errs;
    public final long rx_drop;
    public final UnsignedLong tx_bytes;
    public final long tx_packets;
    public final long tx_errs;
    public final long tx_drop;

    public VirtNetIo(final ByteBuf buffer) throws InvalidPacketException {
        this.rx_bytes = BufferUtils.uint64(buffer);
        this.rx_packets = BufferUtils.uint32(buffer);
        this.rx_errs = BufferUtils.uint32(buffer);
        this.rx_drop = BufferUtils.uint32(buffer);
        this.tx_bytes = BufferUtils.uint64(buffer);
        this.tx_packets = BufferUtils.uint32(buffer);
        this.tx_errs = BufferUtils.uint32(buffer);
        this.tx_drop = BufferUtils.uint32(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("rx_bytes", this.rx_bytes)
                .add("rx_packets", this.rx_packets)
                .add("rx_errs", this.rx_errs)
                .add("rx_drop", this.rx_drop)
                .add("tx_bytes", this.tx_bytes)
                .add("tx_packets", this.tx_packets)
                .add("tx_errs", this.tx_errs)
                .add("tx_drop", this.tx_drop)
                .toString();
    }

    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("rx_bytes", this.rx_bytes.longValue());
        bsonWriter.writeInt64("rx_packets", this.rx_packets);
        bsonWriter.writeInt64("rx_errs", this.rx_errs);
        bsonWriter.writeInt64("rx_drop", this.rx_drop);
        bsonWriter.writeInt64("tx_bytes", this.tx_bytes.longValue());
        bsonWriter.writeInt64("tx_packets", this.tx_packets);
        bsonWriter.writeInt64("tx_errs", this.tx_errs);
        bsonWriter.writeInt64("tx_drop", this.tx_drop);
        bsonWriter.writeEndDocument();
    }

}
