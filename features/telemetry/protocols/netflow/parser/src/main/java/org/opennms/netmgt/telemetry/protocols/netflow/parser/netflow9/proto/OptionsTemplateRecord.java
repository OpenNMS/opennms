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
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.telemetry.protocols.netflow.parser.InvalidPacketException;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

public final class OptionsTemplateRecord implements Record {

    public final OptionsTemplateSet set;  // Enclosing set

    public final OptionsTemplateRecordHeader header;

    public final List<ScopeFieldSpecifier> scopes;
    public final List<FieldSpecifier> fields;

    public OptionsTemplateRecord(final OptionsTemplateSet set,
                                 final OptionsTemplateRecordHeader header,
                                 final ByteBuf buffer) throws InvalidPacketException {
        this.set = Objects.requireNonNull(set);

        this.header = Objects.requireNonNull(header);

        final List<ScopeFieldSpecifier> scopeFields = new LinkedList<>();
        for (int i = 0; i < this.header.optionScopeLength; i += ScopeFieldSpecifier.SIZE) {
            final ScopeFieldSpecifier scopeField = new ScopeFieldSpecifier(buffer);

            // Ignore scope fields without a value so they will always match during scope resolution
            if (scopeField.fieldLength == 0) {
                continue;
            }

            scopeFields.add(scopeField);
        }

        final List<FieldSpecifier> fields = new LinkedList<>();
        for (int i = 0; i < this.header.optionLength; i += FieldSpecifier.SIZE) {
            final FieldSpecifier field = new FieldSpecifier(buffer);
            fields.add(field);
        }

        this.scopes = Collections.unmodifiableList(scopeFields);
        this.fields = Collections.unmodifiableList(fields);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("header", header)
                .add("scopeFields", scopes)
                .add("fields", fields)
                .toString();
    }
}
