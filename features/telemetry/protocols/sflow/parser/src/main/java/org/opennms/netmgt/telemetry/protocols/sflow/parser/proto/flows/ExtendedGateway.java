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

import java.util.Optional;

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.listeners.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramEnrichment;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramVisitor;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.Array;

import com.google.common.base.MoreObjects;
import com.google.common.primitives.UnsignedInteger;

import io.netty.buffer.ByteBuf;

// struct extended_gateway {
//    next_hop nexthop;           /* Address of the border router that should
//                                   be used for the destination network */
//    unsigned int as;            /* Autonomous system number of router */
//    unsigned int src_as;        /* Autonomous system number of source */
//    unsigned int src_peer_as;   /* Autonomous system number of source peer */
//    as_path_type dst_as_path<>; /* Autonomous system path to the destination */
//    unsigned int communities<>; /* Communities associated with this route */
//    unsigned int localpref;     /* LocalPref associated with this route */
// };

public class ExtendedGateway implements FlowData {
    public final NextHop nexthop;
    public final long as;
    public final long src_as;
    public final long src_peer_as;
    public final Array<AsPathType> dst_as_path;
    public final Array<UnsignedInteger> communities;
    public final long localpref;

    public ExtendedGateway(final ByteBuf buffer) throws InvalidPacketException {
        this.nexthop = new NextHop(buffer);
        this.as = BufferUtils.uint32(buffer);
        this.src_as = BufferUtils.uint32(buffer);
        this.src_peer_as = BufferUtils.uint32(buffer);
        this.dst_as_path = new Array(buffer, Optional.empty(), AsPathType::new);
        this.communities = new Array(buffer, Optional.empty(), BufferUtils::uint32);
        this.localpref = BufferUtils.uint32(buffer);
    }

    public ExtendedGateway(final NextHop nexthop, final long as, final long src_as, final long src_peer_as, final Array<AsPathType> dst_as_path, final Array<UnsignedInteger> communities, final long localpref) {
        this.nexthop = nexthop;
        this.as = as;
        this.src_as = src_as;
        this.src_peer_as = src_peer_as;
        this.dst_as_path = dst_as_path;
        this.communities = communities;
        this.localpref = localpref;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("nexthop", this.nexthop)
                .add("as", this.as)
                .add("src_as", this.src_as)
                .add("src_peer_as", this.src_peer_as)
                .add("dst_as_path", this.dst_as_path)
                .add("communities", this.communities)
                .add("localpref", this.localpref)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeName("nexthop");
        this.nexthop.writeBson(bsonWriter, enr);
        bsonWriter.writeInt64("as", this.as);
        bsonWriter.writeInt64("src_as", this.src_as);
        bsonWriter.writeInt64("src_peer_as", this.src_peer_as);

        bsonWriter.writeStartArray("dst_as_path");
        for (final AsPathType asPathType : this.dst_as_path) {
            asPathType.writeBson(bsonWriter, enr);
        }
        bsonWriter.writeEndArray();

        bsonWriter.writeStartArray("communities");
        for (final UnsignedInteger unsignedInteger : this.communities) {
            bsonWriter.writeInt64(unsignedInteger.longValue());
        }
        bsonWriter.writeEndArray();

        bsonWriter.writeInt64("localpref", this.localpref);
        bsonWriter.writeEndDocument();
    }

    @Override
    public void visit(final SampleDatagramVisitor visitor) {
        visitor.accept(this);
        nexthop.visit(visitor);
    }
}
