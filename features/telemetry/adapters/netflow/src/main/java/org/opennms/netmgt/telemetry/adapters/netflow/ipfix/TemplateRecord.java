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

package org.opennms.netmgt.telemetry.adapters.netflow.ipfix;

import static org.opennms.netmgt.telemetry.adapters.netflow.ipfix.BufferUtils.slice;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.google.common.base.MoreObjects;

public final class TemplateRecord implements Record {

    public final TemplateRecordHeader header;

    public final List<FieldSpecifier> fields;

    TemplateRecord(final TemplateRecordHeader header,
                   final ByteBuffer buffer) {
        this.header = header;

        final List<FieldSpecifier> fields = new LinkedList<>();
        for (int i = 0; i < this.header.fieldCount; i++) {
            final FieldSpecifier field = new FieldSpecifier(buffer);
            fields.add(field);
        }

        this.fields = Collections.unmodifiableList(fields);
    }


    @Override
    public boolean isValid() {
        if (!this.header.isValid()) {
            return false;
        }

        for (final FieldSpecifier field : this.fields) {
            if (!field.isValid()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("header", header)
                .add("fields", fields)
                .toString();
    }

    public static Set.RecordParser<TemplateRecord> parser() {
        return new Set.RecordParser<TemplateRecord>() {
            @Override
            public TemplateRecord parse(final ByteBuffer buffer) {
                final ByteBuffer headerBuffer = slice(buffer, TemplateRecordHeader.SIZE);
                final TemplateRecordHeader header = new TemplateRecordHeader(headerBuffer);

                return new TemplateRecord(header, buffer);
            }

            @Override
            public int getMinimumRecordLength() {
                return TemplateRecordHeader.SIZE;
            }
        };
    }
}
