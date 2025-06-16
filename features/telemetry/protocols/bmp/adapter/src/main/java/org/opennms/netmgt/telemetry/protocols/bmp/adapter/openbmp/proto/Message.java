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
