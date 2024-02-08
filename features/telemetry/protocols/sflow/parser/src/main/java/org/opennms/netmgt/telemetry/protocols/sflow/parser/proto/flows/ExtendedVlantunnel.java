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

// struct extended_vlantunnel { 
//   unsigned int stack<>;  /* List of stripped 802.1Q TPID/TCI layers. Each 
//                             TPID,TCI pair is represented as a single 32 bit 
//                             integer. Layers listed from outermost to 
//                             innermost. */ 
// };

public class ExtendedVlantunnel implements FlowData {
    public final Array<UnsignedInteger> stack;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("stack", this.stack)
                .toString();
    }

    public ExtendedVlantunnel(final ByteBuf buffer) throws InvalidPacketException {
        this.stack = new Array(buffer, Optional.empty(), BufferUtils::uint32);
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartArray();
        for (final UnsignedInteger unsignedInteger : stack) {
            bsonWriter.writeInt64(unsignedInteger.longValue());
        }
        bsonWriter.writeEndArray();
    }

    @Override
    public void visit(final SampleDatagramVisitor visitor) {
        visitor.accept(this);
    }
}
