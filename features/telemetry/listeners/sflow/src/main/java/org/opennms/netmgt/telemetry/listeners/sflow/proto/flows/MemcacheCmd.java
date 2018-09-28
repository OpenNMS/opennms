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

// enum memcache_cmd {
//   OTHER    = 0,
//   SET      = 1,
//   ADD      = 2,
//   REPLACE  = 3,
//   APPEND   = 4,
//   PREPEND  = 5,
//   CAS      = 6,
//   GET      = 7,
//   GETS     = 8,
//   INCR     = 9,
//   DECR     = 10,
//   DELETE   = 11,
//   STATS    = 12,
//   FLUSH    = 13,
//   VERSION  = 14,
//   QUIT     = 15,
//   TOUCH    = 16
// };

public enum MemcacheCmd {
    OTHER(0),
    SET(1),
    ADD(2),
    REPLACE(3),
    APPEND(4),
    PREPEND(5),
    CAS(6),
    GET(7),
    GETS(8),
    INCR(9),
    DECR(10),
    DELETE(11),
    STATS(12),
    FLUSH(13),
    VERSION(14),
    QUIT(15),
    TOUCH(16);

    public final int value;

    MemcacheCmd(final int value) {
        this.value = value;
    }

    public static MemcacheCmd from(final ByteBuffer buffer) throws InvalidPacketException {
        final int value = (int) BufferUtils.uint32(buffer);
        switch (value) {
            case 0:
                return OTHER;
            case 1:
                return SET;
            case 2:
                return ADD;
            case 3:
                return REPLACE;
            case 4:
                return APPEND;
            case 5:
                return PREPEND;
            case 6:
                return CAS;
            case 7:
                return GET;
            case 8:
                return GETS;
            case 9:
                return INCR;
            case 10:
                return DECR;
            case 11:
                return DELETE;
            case 12:
                return STATS;
            case 13:
                return FLUSH;
            case 14:
                return VERSION;
            case 15:
                return QUIT;
            case 16:
                return TOUCH;
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
