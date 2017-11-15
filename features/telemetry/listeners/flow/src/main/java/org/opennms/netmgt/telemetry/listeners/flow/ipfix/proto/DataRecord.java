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

package org.opennms.netmgt.telemetry.listeners.flow.ipfix.proto;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.opennms.netmgt.telemetry.listeners.flow.ipfix.session.Field;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.session.TemplateManager;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.session.Template;

import com.google.common.base.MoreObjects;

public final class DataRecord implements Record {

    /*
     +--------------------------------------------------+
     | Field Value                                      |
     +--------------------------------------------------+
     | Field Value                                      |
     +--------------------------------------------------+
      ...
     +--------------------------------------------------+
     | Field Value                                      |
     +--------------------------------------------------+
    */

    public final Template template;
    public final List<FieldValue> fields;

    public DataRecord(final TemplateManager.TemplateResolver templateResolver,
                      final Template template,
                      final ByteBuffer buffer) throws InvalidPacketException {
        this.template = template;

        final List<FieldValue> values = new ArrayList<>(this.template.count());
        for (final Field templateField : this.template) {
            values.add(new FieldValue(templateResolver, templateField, buffer));
        }

        this.fields = Collections.unmodifiableList(values);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("fields", fields)
                .toString();
    }

    public static Set.RecordParser<DataRecord> parser(final TemplateManager.TemplateResolver templateResolver, final int templateId) throws InvalidPacketException {
        final Template template = templateResolver.lookup(templateId)
                .orElseThrow(() -> new InvalidPacketException("Unknown Template ID: %d", templateId));

        return new Set.RecordParser<DataRecord>() {
            @Override
            public DataRecord parse(final ByteBuffer buffer) throws InvalidPacketException {
                return new DataRecord(templateResolver, template, buffer);
            }

            @Override
            public int getMinimumRecordLength() {
                // For variable length fields we assume at least the length value (1 byte) to be present
                return Stream.concat(template.scopeFields.stream(), template.valueFields.stream())
                        .mapToInt(f -> f.length != FieldValue.VARIABLE_SIZED ? f.length : 1).sum();
            }
        };
    }
}
