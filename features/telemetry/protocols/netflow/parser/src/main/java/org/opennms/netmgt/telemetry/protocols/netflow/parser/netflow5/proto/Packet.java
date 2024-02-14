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
    public long getObservationDomainId() {
        return this.header.engineType << 8 + this.header.engineId;
    }

    @Override
    public long getSequenceNumber() {
        return this.header.flowSequence;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("header", this.header)
                .add("records", this.records)
                .toString();
    }
}
