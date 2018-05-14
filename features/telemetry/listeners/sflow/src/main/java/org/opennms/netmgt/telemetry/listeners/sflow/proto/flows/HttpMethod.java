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

package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.nio.ByteBuffer;

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;

import com.google.common.base.MoreObjects;

// enum http_method {
//   OTHER    = 0,
//   OPTIONS  = 1,
//   GET      = 2,
//   HEAD     = 3,
//   POST     = 4,
//   PUT      = 5,
//   DELETE   = 6,
//   TRACE    = 7,
//   CONNECT  = 8
// };

public enum HttpMethod {
    OTHER(0),
    OPTIONS(1),
    GET(2),
    HEAD(3),
    POST(4),
    PUT(5),
    DELETE(6),
    TRACE(7),
    CONNECT(8);

    public final int value;

    HttpMethod(final int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("value", this.value)
                .toString();
    }

    public static HttpMethod from(final ByteBuffer buffer) throws InvalidPacketException {
        final int value = (int) BufferUtils.uint32(buffer);
        switch (value) {
            case 0:
                return OTHER;
            case 1:
                return OPTIONS;
            case 2:
                return GET;
            case 3:
                return HEAD;
            case 4:
                return POST;
            case 5:
                return PUT;
            case 6:
                return DELETE;
            case 7:
                return TRACE;
            case 8:
                return CONNECT;
            default:
                throw new InvalidPacketException(buffer, "Unknown value: {}", value);
        }
    }

    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeInt32(this.value);
    }
}
