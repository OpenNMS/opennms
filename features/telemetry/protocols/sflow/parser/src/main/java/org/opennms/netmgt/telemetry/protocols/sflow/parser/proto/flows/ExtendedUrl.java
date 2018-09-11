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
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.AsciiString;

import com.google.common.base.MoreObjects;

// struct extended_url {
//    url_direction direction;    /* Direction of connection */
//    string url<>;               /* The HTTP request-line (see RFC 2616) */
//    string host<>;              /* The host field from the HTTP header */
// };

public class ExtendedUrl implements FlowData {
    public final UrlDirection direction;
    public final AsciiString url;
    public final AsciiString host;

    public ExtendedUrl(final ByteBuffer buffer) throws InvalidPacketException {
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
    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeName("direction");
        this.direction.writeBson(bsonWriter);
        bsonWriter.writeString("url", this.url.value);
        bsonWriter.writeString("host", this.host.value);
        bsonWriter.writeEndDocument();
    }
}
