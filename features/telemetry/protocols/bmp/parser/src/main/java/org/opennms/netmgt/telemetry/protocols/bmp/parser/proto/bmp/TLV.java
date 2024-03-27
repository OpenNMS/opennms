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
package org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.skip;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.slice;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint16;

import java.util.Optional;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import org.opennms.netmgt.telemetry.protocols.bmp.parser.BmpParser;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.InvalidPacketException;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ForwardingList;

import io.netty.buffer.ByteBuf;

public class TLV<T extends TLV.Type<V, P>, V, P> {
    public final T type;     // uint16
    public final int length; // uint16
    public final V value;    // byte[length]

    public TLV(final ByteBuf buffer, final IntFunction<T> typer, final P parameter, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
        final int type = uint16(buffer);

        this.type = typer.apply(type);
        this.length = uint16(buffer);

        if (this.type != null) {
            this.value = this.type.parse(slice(buffer, this.length), parameter, peerInfo);
        } else {
            BmpParser.RATE_LIMITED_LOG.debug("Unknown type: {}", type);
            this.value = null;
            skip(buffer, this.length);
        }
    }

    public boolean isValid() {
        return this.type != null;
    }

    @FunctionalInterface
    public interface Type<V, P> {
        V parse(final ByteBuf buffer, final P parameter, final Optional<PeerInfo> peerInfo) throws InvalidPacketException;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", this.type)
                .add("length", this.length)
                .add("value", this.value)
                .toString();
    }

    public static class List<E extends TLV<T, V, ?>, T extends TLV.Type<V, ?>, V> extends ForwardingList<E> {
        private final java.util.List<E> elements;

        private List(final java.util.List<E> elements) {
            this.elements = elements;
        }

        public static <E extends TLV<T, V, ?>, T extends TLV.Type<V, ?>, V> List<E, T, V> wrap(final java.util.List<E> list) {
            return new List<>(list);
        }

        @Override
        protected java.util.List<E> delegate() {
            return this.elements;
        }

        public Stream<V> all(final T type) {
            return this.elements.stream()
                    .filter(e -> e.type == type)
                    .map(e -> e.value);
        }

        public Optional<V> first(final T type) {
            return  this.elements.stream()
                    .filter(e -> e.type == type)
                    .findFirst()
                    .map(e -> e.value);
        }
    }
}
