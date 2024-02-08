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
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramEnrichment;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramVisitor;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

// struct extended_mpls { 
//    next_hop nexthop;           /* Address of the next hop */ 
//    label_stack in_stack;       /* Label stack of received packet */ 
//    label_stack out_stack;      /* Label stack for transmitted packet */ 
// };

public class ExtendedMpls implements FlowData {
    public final NextHop nexthop;
    public final LabelStack in_stack;
    public final LabelStack out_stack;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("nexthop", this.nexthop)
                .add("in_stack", this.in_stack)
                .add("out_stack", this.out_stack)
                .toString();
    }

    public ExtendedMpls(final ByteBuf buffer) throws InvalidPacketException {
        this.nexthop = new NextHop(buffer);
        this.in_stack = new LabelStack(buffer);
        this.out_stack = new LabelStack(buffer);
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeName("nexthop");
        this.nexthop.writeBson(bsonWriter, enr);
        bsonWriter.writeName("in_stack");
        this.in_stack.writeBson(bsonWriter, enr);
        bsonWriter.writeName("out_stack");
        this.out_stack.writeBson(bsonWriter, enr);
        bsonWriter.writeEndDocument();
    }

    @Override
    public void visit(final SampleDatagramVisitor visitor) {
        visitor.accept(this);
        nexthop.visit(visitor);
    }
}
