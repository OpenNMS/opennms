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

// union sample_datagram_type switch (datagram_version version) {
//    case VERSION5:
//       sample_datagram_v5 datagram;
// };

public class SampleDatagramType {
    public final DatagramVersion version;
    public final SampleDatagramV5 datagram;

    public SampleDatagramType(final ByteBuf buffer) throws InvalidPacketException {
        this.version = DatagramVersion.from(buffer);
        switch (this.version) {
            case VERSION5: {
                this.datagram = new SampleDatagramV5(buffer);
                break;
            }
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("version", this.version)
                .add("datagram", this.datagram)
                .toString();
    }

    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeName("version");
        this.version.writeBson(bsonWriter, enr);
        bsonWriter.writeName("datagram");
        this.datagram.writeBson(bsonWriter, enr);
        bsonWriter.writeEndDocument();
    }

    public void visit(SampleDatagramVisitor visitor) {
        visitor.accept(this);
        datagram.visit(visitor);
    }
}
