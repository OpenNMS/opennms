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

// struct sampled_ipv4 {
//    unsigned int length;     /* The length of the IP packet excluding 
//                                lower layer encapsulations */
//    unsigned int protocol;   /* IP Protocol type
//                                (for example, TCP = 6, UDP = 17) */
//    ip_v4 src_ip;            /* Source IP Address */
//    ip_v4 dst_ip;            /* Destination IP Address */
//    unsigned int src_port;   /* TCP/UDP source port number or equivalent */
//    unsigned int dst_port;   /* TCP/UDP destination port number or equivalent */
//    unsigned int tcp_flags;  /* TCP flags */
//    unsigned int tos;        /* IP type of service */
// };

public class SampledIpv4 implements FlowData {
    public final long length;
    public final long protocol;
    public final IpV4 src_ip;
    public final IpV4 dst_ip;
    public final long src_port;
    public final long dst_port;
    public final long tcp_flags;
    public final long tos;

    public SampledIpv4(final ByteBuf buffer) throws InvalidPacketException {
        this.length = BufferUtils.uint32(buffer);
        this.protocol = BufferUtils.uint32(buffer);
        this.src_ip = new IpV4(buffer);
        this.dst_ip = new IpV4(buffer);
        this.src_port = BufferUtils.uint32(buffer);
        this.dst_port = BufferUtils.uint32(buffer);
        this.tcp_flags = BufferUtils.uint32(buffer);
        this.tos = BufferUtils.uint32(buffer);
    }

    public SampledIpv4(final long length, final long protocol, final IpV4 src_ip, final IpV4 dst_ip, final long src_port, final long dst_port, final long tcp_flags, final long tos) {
        this.length = length;
        this.protocol = protocol;
        this.src_ip = src_ip;
        this.dst_ip = dst_ip;
        this.src_port = src_port;
        this.dst_port = dst_port;
        this.tcp_flags = tcp_flags;
        this.tos = tos;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("length", this.length)
                .add("protocol", this.protocol)
                .add("src_ip", this.src_ip)
                .add("dst_ip", this.dst_ip)
                .add("src_port", this.src_port)
                .add("dst_port", this.dst_port)
                .add("tcp_flags", this.tcp_flags)
                .add("tos", this.tos)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt32("length", (int) this.length);
        bsonWriter.writeInt32("protocol", (int) this.protocol);

        bsonWriter.writeName("src_ip");
        this.src_ip.writeBson(bsonWriter, enr);

        bsonWriter.writeName("dst_ip");
        this.dst_ip.writeBson(bsonWriter, enr);

        bsonWriter.writeInt32("src_port", (int) this.src_port);
        bsonWriter.writeInt32("dst_port", (int) this.dst_port);
        bsonWriter.writeInt32("tcp_flags", (int) this.tcp_flags);
        bsonWriter.writeInt32("tos", (int) this.tos);
        bsonWriter.writeEndDocument();
    }

    @Override
    public void visit(SampleDatagramVisitor visitor) {
        visitor.accept(this);
    }
}
