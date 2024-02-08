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

public final class TemplateRecord implements Record {

    public final TemplateSet set;  // Enclosing set

    public final TemplateRecordHeader header;

    public final List<FieldSpecifier> fields;

    public TemplateRecord(final TemplateSet set,
                          final TemplateRecordHeader header,
                          final ByteBuf buffer) throws InvalidPacketException {
        this.set = Objects.requireNonNull(set);

        this.header = Objects.requireNonNull(header);

        final List<FieldSpecifier> fields = new LinkedList<>();
        for (int i = 0; i < this.header.fieldCount; i++) {
            final FieldSpecifier field = new FieldSpecifier(buffer);
            fields.add(field);
        }

        this.fields = Collections.unmodifiableList(fields);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("header", header)
                .add("fields", fields)
                .toString();
    }
}
