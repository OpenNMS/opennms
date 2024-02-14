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
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.AsciiString;

import com.google.common.base.MoreObjects;
import com.google.common.primitives.UnsignedLong;

import io.netty.buffer.ByteBuf;

// struct http_request {
//   http_method method;        /* method */
//   version protocol;          /* HTTP protocol version */
//   string uri<255>;           /* URI exactly as it came from the client */
//   string host<64>;           /* Host value from request header */
//   string referer<255>;       /* Referer value from request header */
//   string useragent<128>;     /* User-Agent value from request header */
//   string xff<64>;            /* X-Forwarded-For value
//                                 from request header */
//   string authuser<32>;       /* RFC 1413 identity of user*/
//   string mime_type<64>;      /* Mime-Type of response */
//   unsigned hyper req_bytes;  /* Content-Length of request */
//   unsigned hyper resp_bytes; /* Content-Length of response */
//   unsigned int uS;           /* duration of the operation
//                                 (in microseconds) */
//   int status;                /* HTTP status code */
// };

public class HttpRequest implements FlowData {
    public final HttpMethod method;
    public final Version protocol;
    public final AsciiString uri;
    public final AsciiString host;
    public final AsciiString referer;
    public final AsciiString useragent;
    public final AsciiString xff;
    public final AsciiString authuser;
    public final AsciiString mime_type;
    public final UnsignedLong req_bytes;
    public final UnsignedLong resp_bytes;
    public final long uS;
    public final Integer status;

    public HttpRequest(final ByteBuf buffer) throws InvalidPacketException {
        this.method = HttpMethod.from(buffer);
        this.protocol = new Version(buffer);
        this.uri = new AsciiString(buffer);
        this.host = new AsciiString(buffer);
        this.referer = new AsciiString(buffer);
        this.useragent = new AsciiString(buffer);
        this.xff = new AsciiString(buffer);
        this.authuser = new AsciiString(buffer);
        this.mime_type = new AsciiString(buffer);
        this.req_bytes = BufferUtils.uint64(buffer);
        this.resp_bytes = BufferUtils.uint64(buffer);
        this.uS = BufferUtils.uint32(buffer);
        this.status = BufferUtils.sint32(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("method", this.method)
                .add("protocol", this.protocol)
                .add("uri", this.uri)
                .add("host", this.host)
                .add("referer", this.referer)
                .add("useragent", this.useragent)
                .add("xff", this.xff)
                .add("authuser", this.authuser)
                .add("mime_type", this.mime_type)
                .add("req_bytes", this.req_bytes)
                .add("resp_bytes", this.resp_bytes)
                .add("uS", this.uS)
                .add("status", this.status)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeName("method");
        this.method.writeBson(bsonWriter, enr);
        bsonWriter.writeName("protocol");
        this.protocol.writeBson(bsonWriter, enr);
        bsonWriter.writeString("uri", this.uri.value);
        bsonWriter.writeString("host", this.host.value);
        bsonWriter.writeString("referer", this.referer.value);
        bsonWriter.writeString("useragent", this.useragent.value);
        bsonWriter.writeString("xff", this.xff.value);
        bsonWriter.writeString("authuser", this.authuser.value);
        bsonWriter.writeString("mime_type", this.mime_type.value);
        bsonWriter.writeInt64("req_bytes", this.req_bytes.longValue());
        bsonWriter.writeInt64("resp_bytes", this.resp_bytes.longValue());
        bsonWriter.writeInt64("uS", this.uS);
        bsonWriter.writeInt32("status", this.status);
        bsonWriter.writeEndDocument();
    }

    @Override
    public void visit(final SampleDatagramVisitor visitor) {
        visitor.accept(this);
    }
}
