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

// union address switch (address_type type) {
//    case UNKNOWN:
//      void;
//    case IP_V4:
//      ip_v4 ip;
//    case IP_V6:
//      ip_v6 ip;
// };

public class Address {
    public final AddressType type;
    public final IpV4 ipV4;
    public final IpV6 ipV6;

    public Address(final ByteBuf buffer) throws InvalidPacketException {
        this.type = AddressType.from(buffer);
        switch (this.type) {
            case IP_V4:
                this.ipV4 = new IpV4(buffer);
                this.ipV6 = null;
                break;
            case IP_V6:
                this.ipV4 = null;
                this.ipV6 = new IpV6(buffer);
                break;
            default:
                throw new IllegalStateException();
        }
    }

    public Address(final AddressType type, final IpV4 ipV4, final IpV6 ipV6) {
        this.type = type;
        this.ipV4 = ipV4;
        this.ipV6 = ipV6;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", this.type)
                .add("ipV4", this.ipV4)
                .add("ipV6", this.ipV6)
                .toString();
    }

    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();

        switch (this.type) {
            case IP_V4:
                bsonWriter.writeName("ipv4");
                this.ipV4.writeBson(bsonWriter, enr);
                break;
            case IP_V6:
                bsonWriter.writeName("ipv6");
                this.ipV6.writeBson(bsonWriter, enr);
                break;
            default:
                throw new IllegalStateException();
        }

        bsonWriter.writeEndDocument();
    }

    public void visit(SampleDatagramVisitor visitor) {
        visitor.accept(this);
        switch (this.type) {
            case IP_V4:
                ipV4.visit(visitor);
                break;
            case IP_V6:
                ipV6.visit(visitor);
                break;
            default:
                throw new IllegalStateException();
        }
    }
}
