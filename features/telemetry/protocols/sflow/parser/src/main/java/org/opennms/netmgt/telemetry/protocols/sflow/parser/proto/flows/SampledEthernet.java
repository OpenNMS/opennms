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

// struct sampled_ethernet {
//      unsigned int length;   /* The length of the MAC packet received on the
//                                network, excluding lower layer encapsulations
//                                and framing bits but including FCS octets */
//      mac src_mac;           /* Source MAC address */
//      mac dst_mac;           /* Destination MAC address */
//      unsigned int type;     /* Ethernet packet type */
// };

public class SampledEthernet implements FlowData {
    public final long length;
    public final Mac src_mac;
    public final Mac dst_mac;
    public final long type;

    public SampledEthernet(final ByteBuf buffer) throws InvalidPacketException {
        this.length = BufferUtils.uint32(buffer);
        this.src_mac = new Mac(buffer);
        this.dst_mac = new Mac(buffer);
        this.type = BufferUtils.uint32(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("length", this.length)
                .add("src_mac", this.src_mac)
                .add("dst_mac", this.dst_mac)
                .add("type", this.type)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("length", this.length);
        bsonWriter.writeName("src_mac");
        this.src_mac.writeBson(bsonWriter, enr);
        bsonWriter.writeName("dst_mac");
        this.dst_mac.writeBson(bsonWriter, enr);
        bsonWriter.writeInt64("type", this.type);
        bsonWriter.writeEndDocument();
    }

    @Override
    public void visit(SampleDatagramVisitor visitor) {
        visitor.accept(this);
    }
}
