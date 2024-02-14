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
