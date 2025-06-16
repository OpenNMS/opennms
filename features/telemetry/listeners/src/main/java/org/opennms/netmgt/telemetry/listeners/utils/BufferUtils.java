/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.telemetry.listeners.utils;

import java.nio.BufferUnderflowException;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedLong;

import io.netty.buffer.ByteBuf;

public final class BufferUtils {

    private BufferUtils() {
    }

    public static ByteBuf slice(final ByteBuf buffer, final int size) {
        if (size > buffer.readableBytes()) {
            throw new BufferUnderflowException();
        }

        final ByteBuf result = buffer.slice(buffer.readerIndex(), size);
        buffer.readerIndex(buffer.readerIndex() + size);

        return result;
    }

    public static <R> R peek(final ByteBuf buffer, Function<ByteBuf, R> consumer) {
        final int position = buffer.readerIndex();
        try {
            return consumer.apply(buffer);
        } finally {
            buffer.readerIndex(position);
        }
    }

    public static float sfloat(final ByteBuf buffer) {
        return Float.intBitsToFloat(sint32(buffer));
    }

    public static UnsignedLong uint(final ByteBuf buffer, final int octets) {
        Preconditions.checkArgument(0 <= octets && octets <= 8);

        long result = 0;

        for (int i = 0; i < octets; i++) {
            result = (result << 8L) | (buffer.readUnsignedByte() & 0xFFL);
        }

        return UnsignedLong.fromLongBits(result);
    }

    public static Long sint(final ByteBuf buffer, final int octets) {
        Preconditions.checkArgument(0 <= octets && octets <= 8);

        long result = buffer.readUnsignedByte() & 0xFFL;
        boolean s = (result & 0x80L) != 0;
        if (s) {
            result = 0xFFFFFFFFFFFFFF80L | (result & 0x7FL);
        } else {
            result &= 0x7FL;
        }

        for (int i = 1; i < octets; i++) {
            result = (result << 8L) | (buffer.readUnsignedByte() & 0xFFL);
        }

        return result;
    }

    public static int uint8(final ByteBuf buffer) {
        return buffer.readUnsignedByte() & 0xFF;
    }

    public static int uint16(final ByteBuf buffer) {
        return ((buffer.readUnsignedByte() & 0xFF) << 8)
             | ((buffer.readUnsignedByte() & 0xFF) << 0);
    }

    public static int uint24(final ByteBuf buffer) {
        return ((buffer.readUnsignedByte() & 0xFF) << 16)
             | ((buffer.readUnsignedByte() & 0xFF) << 8)
             | ((buffer.readUnsignedByte() & 0xFF) << 0);
    }

    public static long uint32(final ByteBuf buffer) {
        return ((buffer.readUnsignedByte() & 0xFFL) << 24)
             | ((buffer.readUnsignedByte() & 0xFFL) << 16)
             | ((buffer.readUnsignedByte() & 0xFFL) << 8)
             | ((buffer.readUnsignedByte() & 0xFFL) << 0);
    }

    public static UnsignedLong uint64(final ByteBuf buffer) {
        return UnsignedLong.fromLongBits(
                ((buffer.readUnsignedByte() & 0xFFL) << 56)
              | ((buffer.readUnsignedByte() & 0xFFL) << 48)
              | ((buffer.readUnsignedByte() & 0xFFL) << 40)
              | ((buffer.readUnsignedByte() & 0xFFL) << 32)
              | ((buffer.readUnsignedByte() & 0xFFL) << 24)
              | ((buffer.readUnsignedByte() & 0xFFL) << 16)
              | ((buffer.readUnsignedByte() & 0xFFL) << 8)
              | ((buffer.readUnsignedByte() & 0xFFL) << 0));
    }

    public static Integer sint8(final ByteBuf buffer) {
        return Integer.valueOf(
                (buffer.readUnsignedByte() & 0xFF) << 0);
    }

    public static Integer sint16(final ByteBuf buffer) {
        return ((buffer.readUnsignedByte() & 0xFF) << 8)
             | ((buffer.readUnsignedByte() & 0xFF) << 0);
    }

    public static Integer sint24(final ByteBuf buffer) {
        return ((buffer.readUnsignedByte() & 0xFF) << 16)
             | ((buffer.readUnsignedByte() & 0xFF) << 8)
             | ((buffer.readUnsignedByte() & 0xFF) << 0);
    }

    public static Integer sint32(final ByteBuf buffer) {
        return ((buffer.readUnsignedByte() & 0xFF) << 24)
             | ((buffer.readUnsignedByte() & 0xFF) << 16)
             | ((buffer.readUnsignedByte() & 0xFF) << 8)
             | ((buffer.readUnsignedByte() & 0xFF) << 0);
    }

    public static Long sint64(final ByteBuf buffer) {
        return ((buffer.readUnsignedByte() & 0xFFL) << 56)
             | ((buffer.readUnsignedByte() & 0xFFL) << 48)
             | ((buffer.readUnsignedByte() & 0xFFL) << 40)
             | ((buffer.readUnsignedByte() & 0xFFL) << 32)
             | ((buffer.readUnsignedByte() & 0xFFL) << 24)
             | ((buffer.readUnsignedByte() & 0xFFL) << 16)
             | ((buffer.readUnsignedByte() & 0xFFL) << 8)
             | ((buffer.readUnsignedByte() & 0xFFL) << 0);
    }

    public static byte[] bytes(final ByteBuf buffer, final int size) {
        final byte[] result = new byte[size];
        buffer.readBytes(result);
        return result;
    }

    public static void skip(final ByteBuf buffer, final int size) {
        buffer.skipBytes(size);
    }

    public static <T, E extends Exception> List<T> repeatRemaining(final ByteBuf buffer, final Parser<T, E> parser) throws E {
        final List<T> elements = Lists.newArrayList();
        while (buffer.isReadable()) {
            elements.add(parser.parse(buffer));
        }
        return Collections.unmodifiableList(elements);
    }

    public static <T, E extends Exception> List<T> repeatCount(final ByteBuf buffer, final int count, final Parser<T, E> parser) throws E {
        final List<T> elements = Lists.newArrayList();
        for (int i = 0; i < count; i++) {
            elements.add(parser.parse(buffer));
        }
        return Collections.unmodifiableList(elements);
    }

    @FunctionalInterface
    public interface Parser<T, E extends Exception> {
        T parse(final ByteBuf buffer) throws E;
    }
}
