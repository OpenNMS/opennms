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

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.telemetry.listeners.flow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.flow.session.Template;
import org.opennms.netmgt.telemetry.listeners.flow.session.Session;

import com.google.common.base.MoreObjects;

public final class DataSet extends FlowSet<DataRecord> {
    private final Session.Resolver resolver;

    public final Template template;

    public final List<DataRecord> records;

    public DataSet(final Packet packet,
                   final FlowSetHeader header,
                   final Session.Resolver resolver,
                   final ByteBuffer buffer) throws InvalidPacketException {
        super(packet, header);

        this.resolver = Objects.requireNonNull(resolver);
        this.template = this.resolver.lookupTemplate(this.header.setId)
                .orElseThrow(() -> new InvalidPacketException(buffer, "Unknown Template ID: %d", this.header.setId));

        final int minimumRecordLength = template.stream()
                .mapToInt(f -> f.length()).sum();

        final List<DataRecord> records = new LinkedList();
        while (buffer.remaining() >= minimumRecordLength) {
            records.add(new DataRecord(this, resolver, template, buffer));
        }

        if (records.size() == 0) {
            throw new InvalidPacketException(buffer, "Empty set");
        }

        this.records = Collections.unmodifiableList(records);
    }

    @Override
    public Iterator<DataRecord> iterator() {
        return this.records.iterator();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("header", header)
                .add("records", records)
                .toString();
    }
}
