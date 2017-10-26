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

package org.opennms.netmgt.telemetry.adapters.netflow.ipfix;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import com.google.common.primitives.UnsignedLong;

public class BufferUtils {

    private BufferUtils() {}

    public static ByteBuffer slice(final ByteBuffer buffer, final int size) {
        if (buffer.position() + size > buffer.limit()) {
            new BufferUnderflowException();
        }

        final ByteBuffer result = buffer.slice();
        buffer.position(buffer.position() + size);

        result.limit(size);

        return result;
    }

    public static int uint8(final ByteBuffer buffer) {
        return buffer.get() & 0xFF;
    }

    public static int uint16(final ByteBuffer buffer) {
        return ((buffer.get() & 0xFF) << 8)
             | ((buffer.get() & 0xFF) << 0);
    }

    public static long uint32(final ByteBuffer buffer) {
        return ((buffer.get() & 0xFF) << 24)
             | ((buffer.get() & 0xFF) << 16)
             | ((buffer.get() & 0xFF) << 8)
             | ((buffer.get() & 0xFF) << 0);
    }

    public static UnsignedLong uint64(final ByteBuffer buffer) {
        return UnsignedLong.fromLongBits(((buffer.get() & 0xFF) << 56)
                | ((buffer.get() & 0xFF) << 48)
                | ((buffer.get() & 0xFF) << 40)
                | ((buffer.get() & 0xFF) << 32)
                | ((buffer.get() & 0xFF) << 24)
                | ((buffer.get() & 0xFF) << 16)
                | ((buffer.get() & 0xFF) << 8)
                | ((buffer.get() & 0xFF) << 0));
    }

    public static byte[] bytes(final ByteBuffer buffer, final int size) {
        final byte[] result = new byte[size];
        buffer.get(result);
        return result;
    }

}
