/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.listeners.flow.netflow9.proto;

import static org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils.uint16;
import static org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils.uint32;

import java.nio.ByteBuffer;

import org.opennms.netmgt.telemetry.listeners.flow.InvalidPacketException;

import com.google.common.base.MoreObjects;

public class Header {

    /*
     0                   1                   2                   3
     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |       Version Number          |            Count              |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |                           sysUpTime                           |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |                           UNIX Secs                           |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |                       Sequence Number                         |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |                        Source ID                              |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    */

    public static final int SIZE = 20;

    public static final int VERSION = 0x0009;

    public final int versionNumber; // uint16 - must be 0x0009
    public final int count; // uint16
    public final long sysUpTime; // uint32
    public final long unixSecs; // uint32
    public final long sequenceNumber; // uint32
    public final long sourceId; // uint32

    public Header(final ByteBuffer buffer) throws InvalidPacketException {
        this.versionNumber = uint16(buffer);
        if (this.versionNumber != VERSION) {
            throw new InvalidPacketException(buffer, "Invalid version number: 0x%04X", versionNumber);
        }

        this.count = uint16(buffer);
        if (this.count <= 0) {
            throw new InvalidPacketException(buffer, "Empty packet");
        }

        this.sysUpTime = uint32(buffer);
        this.unixSecs = uint32(buffer);
        this.sequenceNumber = uint32(buffer);
        this.sourceId = uint32(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("versionNumber", versionNumber)
                .add("count", count)
                .add("sysUpTime", sysUpTime)
                .add("unixSecs", unixSecs)
                .add("sequenceNumber", sequenceNumber)
                .add("sourceId", sourceId)
                .toString();
    }
}
