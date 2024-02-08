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

// struct extended_socket_ipv6 {
//    unsigned int protocol;     /* IP Protocol type
//                                  (for example, TCP = 6, UDP = 17) */
//    ip_v6 local_ip;            /* local IP address */
//    ip_v6 remote_ip;           /* remote IP address */
//    unsigned int local_port;   /* TCP/UDP local port number or equivalent */
//    unsigned int remote_port;  /* TCP/UDP remote port number of equivalent */
// };

public class ExtendedSocketIpv6 implements FlowData {
    public final long protocol;
    public final IpV6 local_ip;
    public final IpV6 remote_ip;
    public final long local_port;
    public final long remote_port;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("protocol", this.protocol)
                .add("local_ip", this.local_ip)
                .add("remote_ip", this.remote_ip)
                .add("local_port", this.local_port)
                .add("remote_port", this.remote_port)
                .toString();
    }

    public ExtendedSocketIpv6(final ByteBuf buffer) throws InvalidPacketException {
        this.protocol = BufferUtils.uint32(buffer);
        this.local_ip = new IpV6(buffer);
        this.remote_ip = new IpV6(buffer);
        this.local_port = BufferUtils.uint32(buffer);
        this.remote_port = BufferUtils.uint32(buffer);
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("protocol", this.protocol);
        bsonWriter.writeName("local_ip");
        this.local_ip.writeBson(bsonWriter, enr);
        bsonWriter.writeName("remote_ip");
        this.remote_ip.writeBson(bsonWriter, enr);
        bsonWriter.writeInt64("local_port", this.local_port);
        bsonWriter.writeInt64("remote_port", this.remote_port);
        bsonWriter.writeEndDocument();
    }

    @Override
    public void visit(final SampleDatagramVisitor visitor) {
        visitor.accept(this);
    }
}
