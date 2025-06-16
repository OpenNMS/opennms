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

import java.net.Inet6Address;
import java.net.UnknownHostException;

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.listeners.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramEnrichment;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramVisitor;

import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;

import io.netty.buffer.ByteBuf;

// typedef opaque ip_v6[16];

public class IpV6 {
    public final Inet6Address ip_v6;

    public IpV6(final ByteBuf buffer) {
        try {
            this.ip_v6 = (Inet6Address) Inet6Address.getByAddress(BufferUtils.bytes(buffer, 16));
        } catch (final UnknownHostException e) {
            // This only happens if byte array length is != 4
            throw Throwables.propagate(e);
        }
    }

    public Inet6Address getAddress() {
        return ip_v6;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("ip_v6", this.ip_v6)
                .toString();
    }

    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeString("address", this.ip_v6.getHostAddress());
        enr.getHostnameFor(this.ip_v6).ifPresent((hostname) -> bsonWriter.writeString("hostname", hostname));
        bsonWriter.writeEndDocument();
    }

    public void visit(SampleDatagramVisitor visitor) {
        visitor.accept(this);
    }
}
