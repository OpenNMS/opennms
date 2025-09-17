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
package org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix.proto;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.telemetry.protocols.netflow.parser.InvalidPacketException;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.InformationElementDatabase;

public final class OptionsTemplateRecord implements Record {

    public final OptionsTemplateRecordHeader header;

    public final List<FieldSpecifier> scopes;
    public final List<FieldSpecifier> fields;

    public OptionsTemplateRecord(final InformationElementDatabase informationElementDatabase, final OptionsTemplateRecordHeader header,
                                 final ByteBuf buffer) throws InvalidPacketException {
        this.header = Objects.requireNonNull(header);

        final List<FieldSpecifier> scopes = new LinkedList<>();
        for (int i = 0; i < this.header.scopeFieldCount; i++) {
            final FieldSpecifier scopeField = new FieldSpecifier(informationElementDatabase, buffer);

            scopes.add(scopeField);
        }

        final List<FieldSpecifier> fields = new LinkedList<>();
        for (int i = this.header.scopeFieldCount; i < this.header.fieldCount; i++) {
            final FieldSpecifier field = new FieldSpecifier(informationElementDatabase, buffer);

            fields.add(field);
        }

        this.scopes = Collections.unmodifiableList(scopes);
        this.fields = Collections.unmodifiableList(fields);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("header", header)
                .add("scopes", scopes)
                .add("fields", fields)
                .toString();
    }
}
