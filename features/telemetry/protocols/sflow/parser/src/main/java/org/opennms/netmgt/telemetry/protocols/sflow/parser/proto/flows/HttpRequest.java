/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows;

import java.nio.ByteBuffer;

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.common.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.AsciiString;

import com.google.common.base.MoreObjects;
import com.google.common.primitives.UnsignedLong;

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

    public HttpRequest(final ByteBuffer buffer) throws InvalidPacketException {
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
    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeName("method");
        this.method.writeBson(bsonWriter);
        bsonWriter.writeName("protocol");
        this.protocol.writeBson(bsonWriter);
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
}
