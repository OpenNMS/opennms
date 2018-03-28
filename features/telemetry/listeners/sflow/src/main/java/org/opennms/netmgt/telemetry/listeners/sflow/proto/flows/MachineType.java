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

// enum machine_type {
//    unknown = 0,
//    other   = 1,
//    x86     = 2,
//    x86_64  = 3,
//    ia64    = 4,
//    sparc   = 5,
//    alpha   = 6,
//    powerpc = 7,
//    m68k    = 8,
//    mips    = 9,
//    arm     = 10,
//    hppa    = 11,
//    s390    = 12
// };

public enum MachineType {
    unknown(0),
    other(1),
    x86(2),
    x86_64(3),
    ia64(4),
    sparc(5),
    alpha(6),
    powerpc(7),
    m68k(8),
    mips(9),
    arm(10),
    hppa(11),
    s390(12);

    public final int value;

    MachineType(final int value) {
        this.value = value;
    }

    public static MachineType from(final ByteBuffer buffer) throws InvalidPacketException {
        final int value = (int) BufferUtils.uint32(buffer);
        switch (value) {
            case 0:
                return unknown;
            case 1:
                return other;
            case 2:
                return x86;
            case 3:
                return x86_64;
            case 4:
                return ia64;
            case 5:
                return sparc;
            case 6:
                return alpha;
            case 7:
                return powerpc;
            case 8:
                return m68k;
            case 9:
                return mips;
            case 10:
                return arm;
            case 11:
                return hppa;
            case 12:
                return s390;
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
