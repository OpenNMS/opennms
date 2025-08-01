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
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramVisitor;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

// struct extended_router {
//    next_hop nexthop;            /* IP address of next hop router */
//    unsigned int src_mask_len;   /* Source address prefix mask
//                                    (expressed as number of bits) */
//    unsigned int dst_mask_len;   /* Destination address prefix mask
//                                    (expressed as number of bits) */
// };

public class ExtendedRouter implements FlowData {
    public final NextHop nexthop;
    public final long src_mask_len;
    public final long dst_mask_len;

    public ExtendedRouter(final ByteBuf buffer) throws InvalidPacketException {
        this.nexthop = new NextHop(buffer);
        this.src_mask_len = BufferUtils.uint32(buffer);
        this.dst_mask_len = BufferUtils.uint32(buffer);
    }

    public ExtendedRouter(final NextHop nexthop, final long src_mask_len, final long dst_mask_len) {
        this.nexthop = nexthop;
        this.src_mask_len = src_mask_len;
        this.dst_mask_len = dst_mask_len;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("nexthop", this.nexthop)
                .add("src_mask_len", this.src_mask_len)
                .add("dst_mask_len", this.dst_mask_len)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeName("nexthop");
        this.nexthop.writeBson(bsonWriter, enr);
        bsonWriter.writeInt64("src_mask_len", this.src_mask_len);
        bsonWriter.writeInt64("dst_mask_len", this.dst_mask_len);
        bsonWriter.writeEndDocument();
    }

    @Override
    public void visit(final SampleDatagramVisitor visitor) {
        visitor.accept(this);
        nexthop.visit(visitor);
    }
}
