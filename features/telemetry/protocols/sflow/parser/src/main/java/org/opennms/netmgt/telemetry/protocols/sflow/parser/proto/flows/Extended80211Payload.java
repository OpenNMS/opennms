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

import org.bson.BsonBinary;
import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramEnrichment;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramVisitor;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.Opaque;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

// struct extended_80211_payload {
//    cipher_suite ciphersuite; /* encryption scheme used for this packet */
//    opaque       data<>;      /* unencrypted bytes from the payload */
// };

public class Extended80211Payload implements FlowData {
    public final CipherSuite ciphersuite;
    public final Opaque<byte[]> data;

    public Extended80211Payload(final ByteBuf buffer) throws InvalidPacketException {
        this.ciphersuite = new CipherSuite(buffer);
        this.data = new Opaque(buffer, Optional.empty(), Opaque::parseBytes);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("ciphersuite", this.ciphersuite)
                .add("data", this.data)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeName("ciphersuite");
        this.ciphersuite.writeBson(bsonWriter, enr);
        bsonWriter.writeBinaryData("data", new BsonBinary(this.data.value));
        bsonWriter.writeEndDocument();
    }

    @Override
    public void visit(SampleDatagramVisitor visitor) {
        visitor.accept(this);
    }
}
