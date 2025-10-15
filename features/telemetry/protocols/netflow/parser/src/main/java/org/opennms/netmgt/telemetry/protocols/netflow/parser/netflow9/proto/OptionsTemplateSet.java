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
package org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow9.proto;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.opennms.netmgt.telemetry.protocols.netflow.parser.InvalidPacketException;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.InformationElementDatabase;

public final class OptionsTemplateSet extends FlowSet<OptionsTemplateRecord> {

    public final List<OptionsTemplateRecord> records;

    public OptionsTemplateSet(final InformationElementDatabase informationElementDatabase,
                              final Packet packet,
                              final FlowSetHeader header,
                              final ByteBuf buffer) throws InvalidPacketException {
        super(packet, header);

        final List<OptionsTemplateRecord> records = new LinkedList();
        while (buffer.isReadable(OptionsTemplateRecordHeader.SIZE)) {
            final OptionsTemplateRecordHeader recordHeader = new OptionsTemplateRecordHeader(buffer);
            records.add(new OptionsTemplateRecord(informationElementDatabase, this, recordHeader, buffer));
        }

        if (records.size() == 0) {
            throw new InvalidPacketException(buffer, "Empty set");
        }

        this.records = Collections.unmodifiableList(records);
    }

    @Override
    public Iterator<OptionsTemplateRecord> iterator() {
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
