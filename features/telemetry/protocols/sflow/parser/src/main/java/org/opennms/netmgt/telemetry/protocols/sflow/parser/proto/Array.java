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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.opennms.netmgt.telemetry.listeners.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

public class Array<T> implements Iterable<T> {
    @FunctionalInterface
    public interface Parser<T> {
        T parse(final ByteBuf buffer) throws InvalidPacketException;
    }

    public final int size;
    public final List<T> values;

    public Array(final ByteBuf buffer,
                 final Optional<Integer> size,
                 final Parser<? extends T> parser) throws InvalidPacketException {

        this.size = size.orElseGet(() -> (int) BufferUtils.uint32(buffer));

        final List<T> values = new ArrayList<>(this.size);
        for (int i = 0; i < this.size; i++) {
            values.add(parser.parse(buffer));
        }
        this.values = Collections.unmodifiableList(values);
    }

    public Array(final int size, final List<T> values) {
        this.size = size;
        this.values = values;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("size", this.size)
                .add("values", this.values)
                .toString();
    }

    @Override
    public Iterator<T> iterator() {
        return values.iterator();
    }
}
