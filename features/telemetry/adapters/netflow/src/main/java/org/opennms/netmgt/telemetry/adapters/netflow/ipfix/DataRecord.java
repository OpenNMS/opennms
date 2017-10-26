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
import static org.opennms.netmgt.telemetry.adapters.netflow.ipfix.BufferUtils.uint16;
import static org.opennms.netmgt.telemetry.adapters.netflow.ipfix.BufferUtils.uint8;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.session.Template;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.Value;

import com.google.common.base.MoreObjects;

public final class DataRecord implements Record {

    /*
      0                   1                   2                   3
      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     | Length (< 255)|          Information Field                  |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |                      ... continuing as needed                 |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

      0                   1                   2                   3
      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |      255      |      Length (0 to 65535)      |       IE      |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |                      ... continuing as needed                 |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    */

    public static final int VARIABLE_SIZED = 0xFFFF;
    public static final int VARIABLE_SIZED_EXTENDED = 0xFF;

    public final List<Value> values;

    DataRecord(final Template template,
               final ByteBuffer buffer) {

        final List<Value> values = new ArrayList<>(template.count());
        for (final Template.Field field : template) {
            int size = field.size;
            if (size == VARIABLE_SIZED) {
                size = uint8(buffer);
                if (size == VARIABLE_SIZED_EXTENDED) {
                    size = uint16(buffer);
                }
            }

            final Value value = field.parse(slice(buffer, size));
            values.add(value);
        }

        this.values = Collections.unmodifiableList(values);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("values", values)
                .toString();
    }

    public static Set.RecordParser<DataRecord> parser(final Template template) {
        return new Set.RecordParser<DataRecord>() {
            @Override
            public DataRecord parse(final ByteBuffer buffer) {
                return new DataRecord(template, buffer);
            }

            @Override
            public int getMinimumRecordLength() {
                // For variable length fields we assume at least the length value (1 byte) to be present
                return Stream.concat(template.scopeFields.stream(), template.valueFields.stream())
                        .mapToInt(f -> f.size != VARIABLE_SIZED ? f.size : 1).sum();
            }
        };
    }
}
