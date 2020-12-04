/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto;

import java.util.List;
import java.util.Objects;

public class Message {
    private final String collectorHashId;
    private final Type type;

    private final List<Record> records;

    public Message(final String collectorHashId,
                   final Type type,
                   final List<Record> records) {
        this.collectorHashId = Objects.requireNonNull(collectorHashId);
        this.type = Objects.requireNonNull(type);
        this.records = Objects.requireNonNull(records);

        for (final Record record : records) {
            assert record.getType() == type;
        }
    }

    private static void serializeHeader(final StringBuffer buffer, final String key, final String value) {
        buffer.append(key)
              .append(": ")
              .append(value)
              .append('\n');
    }

    public final void serialize(final StringBuffer buffer) {
        // Serialize records to data buffer (required to know buffer length beforehand)
        final StringBuffer data = new StringBuffer();
        for (final Record record : this.records) {
            record.serialize(data);
        }

        // Write headers
        serializeHeader(buffer, "V", "1.7");
        serializeHeader(buffer, "C_HASH_ID", this.collectorHashId);
        serializeHeader(buffer, "T", this.type.toString());
        serializeHeader(buffer, "L", Integer.toString(data.length()));
        serializeHeader(buffer, "R", Integer.toString(this.records.size()));

        // Headers and data separated by double newline
        buffer.append('\n');

        // Append data from buffer
        buffer.append(data);
    }

    public String getCollectorHashId() {
        return this.collectorHashId;
    }

    public Type getType() {
        return this.type;
    }

    public List<Record> getRecords() {
        return records;
    }

    public int count() {
        return this.records.size();
    }
}
