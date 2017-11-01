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

package org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.values;

import static org.opennms.netmgt.telemetry.adapters.netflow.ipfix.BufferUtils.slice;
import static org.opennms.netmgt.telemetry.adapters.netflow.ipfix.BufferUtils.uint8;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.DataRecord;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.InvalidPacketException;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.Set;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.SetHeader;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.Value;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.session.Session;

import com.google.common.collect.Lists;

public class SubTemplateMultiListValue extends ListValue<List<List<Value<?>>>> {

    private final List<List<List<Value<?>>>> values;

    public SubTemplateMultiListValue(final String name,
                                     final Semantic semantic,
                                     final List<List<List<Value<?>>>> values) {
        super(name, semantic);
        this.values = values;
    }


    public static Parser parser(final String name) {
        return new Value.Parser() {

            @Override
            public Value<?> parse(final Session.TemplateResolver templateResolver, final ByteBuffer buffer) throws InvalidPacketException {
                final Semantic semantic = Semantic.find(uint8(buffer));

                final List<List<List<Value<?>>>> values = new LinkedList<>();
                while (buffer.hasRemaining()) {
                    final SetHeader header = new SetHeader(buffer);
                    if (header.setId <= 255) {
                        throw new InvalidPacketException("Invalid template ID: %d", header.setId);
                    }

                    final ByteBuffer payloadBuffer = slice(buffer, header.length - SetHeader.SIZE);
                    final Set<DataRecord> dataSet = new Set<>(header, DataRecord.parser(templateResolver, header.setId), payloadBuffer);

                    values.add(Lists.transform(dataSet.records, r -> Lists.transform(r.fields, f->f.value)));
                }

                return new SubTemplateMultiListValue(name, semantic, values);
            }

            @Override
            public int getMaximumFieldLength() {
                return 0xFFFF;
            }

            @Override
            public int getMinimumFieldLength() {
                return 0;
            }
        };
    }

    @Override
    public List<List<List<Value<?>>>> getValue() {
        return this.values;
    }
}
