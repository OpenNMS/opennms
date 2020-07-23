/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow5.proto;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.slice;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.opennms.netmgt.telemetry.protocols.netflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.RecordProvider;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;

import io.netty.buffer.ByteBuf;

public final class Packet implements Iterable<Record>, RecordProvider {

    public final Header header;

    public final List<Record> records;

    public Packet(final Header header,
                  final ByteBuf buffer) throws InvalidPacketException {
        this.header = Objects.requireNonNull(header);

        final List<Record> records = new LinkedList();

        while (buffer.isReadable(Record.SIZE)
                && records.size() < this.header.count) {
            final Record record = new Record(this, slice(buffer, Record.SIZE));
            records.add(record);
        }

        if (records.size() != this.header.count) {
            throw new InvalidPacketException(buffer, "Incomplete packet");
        }

        this.records = records;
    }

    @Override
    public Iterator<Record> iterator() {
        return this.records.iterator();
    }

    @Override
    public Stream<Iterable<Value<?>>> getRecords() {
        final Iterable<Value<?>> header = this.header.asValues();

        return this.records.stream()
                .map(Record::asValues)
                .map(record -> Iterables.concat(header, record));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("header", this.header)
                .add("records", this.records)
                .toString();
    }
}
