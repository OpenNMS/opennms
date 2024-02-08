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

// struct extended_user {
//    charset src_charset;        /* Character set for src_user */
//    opaque src_user<>;          /* User ID associated with packet source */
//    charset dst_charset;        /* Character set for dst_user */
//    opaque dst_user<>;          /* User ID associated with packet destination */
// };

public class ExtendedUser implements FlowData {
    public final Charset src_charset;
    public final Opaque<byte[]> src_user;
    public final Charset dst_charset;
    public final Opaque<byte[]> dst_user;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("src_charset", this.src_charset)
                .add("src_user", this.src_user)
                .add("dst_charset", this.dst_charset)
                .add("dst_user", this.dst_user)
                .toString();
    }

    public ExtendedUser(final ByteBuf buffer) throws InvalidPacketException {
        this.src_charset = new Charset(buffer);
        this.src_user = new Opaque(buffer, Optional.empty(), Opaque::parseBytes);
        this.dst_charset = new Charset(buffer);
        this.dst_user = new Opaque(buffer, Optional.empty(), Opaque::parseBytes);
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeName("src_charset");
        this.src_charset.writeBson(bsonWriter, enr);
        bsonWriter.writeName("src_user");
        bsonWriter.writeBinaryData(new BsonBinary(this.src_user.value));
        bsonWriter.writeName("dst_charset");
        this.dst_charset.writeBson(bsonWriter, enr);
        bsonWriter.writeName("dst_user");
        bsonWriter.writeBinaryData(new BsonBinary(this.dst_user.value));
        bsonWriter.writeEndDocument();
    }

    @Override
    public void visit(final SampleDatagramVisitor visitor) {
        visitor.accept(this);
    }
}
