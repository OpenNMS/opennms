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

package org.opennms.netmgt.telemetry.protocols.sflow.parser.proto;

import java.util.Optional;

import org.opennms.netmgt.telemetry.listeners.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

public class Opaque<T> {

    @FunctionalInterface
    public interface Parser<T> {
        T parse(final ByteBuf buffer) throws InvalidPacketException;
    }

    public final int length;
    public final T value;

    public Opaque(final ByteBuf buffer,
                  final Optional<Integer> length,
                  final Parser<T> parser) throws InvalidPacketException {
        this.length = length.orElseGet(() -> (int) BufferUtils.uint32(buffer));
        this.value = parser.parse(BufferUtils.slice(buffer, this.length));

        // Skip over optional padding
        BufferUtils.skip(buffer, (4 - (this.length % 4)) % 4);
    }

    public Opaque(final int length, final T t) {
        this.length = length;
        this.value = t;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("length", this.length)
                .add("value", this.value)
                .toString();
    }

    public static <T> T parseUnknown(final ByteBuf buffer) throws InvalidPacketException {
        // This will consume the whole buffer and always returns null
        BufferUtils.skip(buffer, buffer.readableBytes());
        return null;
    }

    public static byte[] parseBytes(final ByteBuf buffer) throws InvalidPacketException {
        return BufferUtils.bytes(buffer, buffer.readableBytes());
    }
}
