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
import java.util.Optional;

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.common.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.Array;

import com.google.common.base.MoreObjects;
import com.google.common.primitives.UnsignedLong;

// struct app_operation {
//   context context;             /* attributes describing the operation */
//   utf8string status_descr<64>; /* additional text describing status
//                                   (e.g. "unknown client") */
//   unsigned hyper req_bytes;    /* size of request body (exclude headers) */
//   unsigned hyper resp_bytes;   /* size of response body (exclude headers) */
//   unsigned int uS;             /* duration of the operation (microseconds) */
//   status status;               /* status code */
// };

public class AppOperation implements FlowData {
    public final Context context;
    public final Array<Utf8string> status_descr;
    public final UnsignedLong req_bytes;
    public final UnsignedLong resp_bytes;
    public final long uS;
    public final Status status;

    public AppOperation(final ByteBuffer buffer) throws InvalidPacketException {
        this.context = new Context(buffer);
        this.status_descr = new Array(buffer, Optional.empty(), Utf8string::new);
        this.req_bytes = BufferUtils.uint64(buffer);
        this.resp_bytes = BufferUtils.uint64(buffer);
        this.uS = BufferUtils.uint32(buffer);
        this.status = Status.from(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("context", this.context)
                .add("status_descr", this.status_descr)
                .add("req_bytes", this.req_bytes)
                .add("resp_bytes", this.resp_bytes)
                .add("uS", this.uS)
                .add("", this.status)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();

        bsonWriter.writeName("context");
        this.context.writeBson(bsonWriter);

        bsonWriter.writeStartArray("status_descr");
        for (final Utf8string utf8string : this.status_descr) {
            utf8string.writeBson(bsonWriter);
        }
        bsonWriter.writeEndArray();

        bsonWriter.writeInt64("req_bytes", this.req_bytes.longValue());

        bsonWriter.writeInt64("resp_bytes", this.resp_bytes.longValue());

        bsonWriter.writeInt64("uS", this.uS);

        bsonWriter.writeName("status");
        this.status.writeBson(bsonWriter);

        bsonWriter.writeEndDocument();
    }
}
