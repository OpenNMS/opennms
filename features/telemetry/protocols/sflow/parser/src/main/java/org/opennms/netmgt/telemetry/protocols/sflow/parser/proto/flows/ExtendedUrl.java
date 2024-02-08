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
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.AsciiString;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

// struct extended_url {
//    url_direction direction;    /* Direction of connection */
//    string url<>;               /* The HTTP request-line (see RFC 2616) */
//    string host<>;              /* The host field from the HTTP header */
// };

public class ExtendedUrl implements FlowData {
    public final UrlDirection direction;
    public final AsciiString url;
    public final AsciiString host;

    public ExtendedUrl(final ByteBuf buffer) throws InvalidPacketException {
        this.direction = UrlDirection.from(buffer);
        this.url = new AsciiString(buffer);
        this.host = new AsciiString(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("direction", this.direction)
                .add("url", this.url)
                .add("host", this.host)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeName("direction");
        this.direction.writeBson(bsonWriter, enr);
        bsonWriter.writeString("url", this.url.value);
        bsonWriter.writeString("host", this.host.value);
        bsonWriter.writeEndDocument();
    }

    @Override
    public void visit(final SampleDatagramVisitor visitor) {
        visitor.accept(this);
    }
}
