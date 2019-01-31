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

// enum os_name {
//    unknown   = 0,
//    other     = 1,
//    linux     = 2,
//    windows   = 3,
//    darwin    = 4,
//    hpux      = 5,
//    aix       = 6,
//    dragonfly = 7,
//    freebsd   = 8,
//    netbsd    = 9,
//    openbsd   = 10,
//    osf       = 11,
//    solaris   = 12
// };

public enum OsName {
    unknown(0),
    other(1),
    linux(2),
    windows(3),
    darwin(4),
    hpux(5),
    aix(6),
    dragonfly(7),
    freebsd(8),
    netbsd(9),
    openbsd(10),
    osf(11),
    solaris(12);

    public final int value;

    OsName(final int value) {
        this.value = value;
    }

    public static OsName from(final ByteBuffer buffer) throws InvalidPacketException {
        final int value = (int) BufferUtils.uint32(buffer);
        switch (value) {
            case 0:
                return unknown;
            case 1:
                return other;
            case 2:
                return linux;
            case 3:
                return windows;
            case 4:
                return darwin;
            case 5:
                return hpux;
            case 6:
                return aix;
            case 7:
                return dragonfly;
            case 8:
                return freebsd;
            case 9:
                return netbsd;
            case 10:
                return openbsd;
            case 11:
                return osf;
            case 12:
                return solaris;
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
