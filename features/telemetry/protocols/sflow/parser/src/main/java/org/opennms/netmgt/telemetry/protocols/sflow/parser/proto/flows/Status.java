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

import com.google.common.base.MoreObjects;

// enum status {
//   SUCCESS         = 0,
//   OTHER           = 1,
//   TIMEOUT         = 2,
//   INTERNAL_ERROR  = 3,
//   BAD_REQUEST     = 4,
//   FORBIDDEN       = 5,
//   TOO_LARGE       = 6,
//   NOT_IMPLEMENTED = 7,
//   NOT_FOUND       = 8,
//   UNAVAILABLE     = 9,
//   UNAUTHORIZED    = 10
// };

public enum Status {
    SUCCESS(0),
    OTHER(1),
    TIMEOUT(2),
    INTERNAL_ERROR(3),
    BAD_REQUEST(4),
    FORBIDDEN(5),
    TOO_LARGE(6),
    NOT_IMPLEMENTED(7),
    NOT_FOUND(8),
    UNAVAILABLE(9),
    UNAUTHORIZED(10);

    public final int value;

    Status(final int value) {
        this.value = value;
    }

    public static Status from(final ByteBuffer buffer) throws InvalidPacketException {
        final int value = (int) BufferUtils.uint32(buffer);
        switch (value) {
            case 0:
                return SUCCESS;
            case 1:
                return OTHER;
            case 2:
                return TIMEOUT;
            case 3:
                return INTERNAL_ERROR;
            case 4:
                return BAD_REQUEST;
            case 5:
                return FORBIDDEN;
            case 6:
                return TOO_LARGE;
            case 7:
                return NOT_IMPLEMENTED;
            case 8:
                return NOT_FOUND;
            case 9:
                return UNAVAILABLE;
            case 10:
                return UNAUTHORIZED;
            default:
                throw new InvalidPacketException(buffer, "Unknown value: {}", value);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("value", this.value)
                .toString();
    }

    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeInt32(this.value);
    }
}
