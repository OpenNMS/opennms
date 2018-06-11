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

import java.nio.ByteBuffer;

import org.opennms.netmgt.telemetry.listeners.flow.InvalidPacketException;

import com.google.common.base.MoreObjects;

public final class FlowSetHeader {

    /*
     0                   1                   2                   3
     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |       FlowSet ID = 1          |          Length               |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    */

    public static final int TEMPLATE_SET_ID = 0;
    public static final int OPTIONS_TEMPLATE_SET_ID = 1;

    public enum Type {
        TEMPLATE_FLOWSET,
        OPTIONS_TEMPLATE_FLOWSET,
        DATA_FLOWSET
    }

    public static final int SIZE = 4;

    public final int setId; // uint16
    public final int length; // uint16

    public FlowSetHeader(final ByteBuffer buffer) throws InvalidPacketException {
        this.setId = uint16(buffer);
        if (this.setId < 256 && this.setId != TEMPLATE_SET_ID && this.setId != OPTIONS_TEMPLATE_SET_ID) {
            throw new InvalidPacketException(buffer, "Invalid set ID: %d", this.setId);
        }

        this.length = uint16(buffer);
    }

    public Type getType() {
        if (this.setId == TEMPLATE_SET_ID) return Type.TEMPLATE_FLOWSET;
        if (this.setId == OPTIONS_TEMPLATE_SET_ID) return Type.OPTIONS_TEMPLATE_FLOWSET;
        if (this.setId >= 256) return Type.DATA_FLOWSET;

        return null;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("flowSetId", setId)
                .add("length", length)
                .toString();
    }
}
