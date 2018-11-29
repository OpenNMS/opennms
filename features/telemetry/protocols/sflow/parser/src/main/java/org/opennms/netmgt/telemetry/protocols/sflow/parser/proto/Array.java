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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.opennms.netmgt.telemetry.common.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;

import com.google.common.base.MoreObjects;

public class Array<T> implements Iterable<T> {
    @FunctionalInterface
    public interface Parser<T> {
        T parse(final ByteBuffer buffer) throws InvalidPacketException;
    }

    public final int size;
    public final List<T> values;

    public Array(final ByteBuffer buffer,
                 final Optional<Integer> size,
                 final Array.Parser<? extends T> parser) throws InvalidPacketException {

        this.size = size.orElseGet(() -> (int) BufferUtils.uint32(buffer));

        final List<T> values = new ArrayList<>(this.size);
        for (int i = 0; i < this.size; i++) {
            values.add(parser.parse(buffer));
        }
        this.values = Collections.unmodifiableList(values);
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
