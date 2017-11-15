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

package org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values;

import static org.opennms.netmgt.telemetry.listeners.flow.ipfix.BufferUtils.uint16;
import static org.opennms.netmgt.telemetry.listeners.flow.ipfix.BufferUtils.uint8;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import org.opennms.netmgt.telemetry.listeners.flow.ipfix.proto.DataRecord;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.proto.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.Value;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.session.TemplateManager;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.session.Template;

import com.google.common.collect.Lists;

public class SubTemplateListValue extends ListValue<List<Value<?>>> {
    /*
      0                   1                   2                   3
      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |   Semantic    |         Template ID           |     ...       |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |                subTemplateList Content    ...                 |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |                              ...                              |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    */

    private final List<List<Value<?>>> values;

    public SubTemplateListValue(final String name,
                                final Semantic semantic,
                                final List<List<Value<?>>> values) {
        super(name, semantic);
        this.values = values;
    }

    public static Parser parser(final String name) {
        return new Value.Parser() {

            @Override
            public Value<?> parse(final TemplateManager.TemplateResolver templateResolver, final ByteBuffer buffer) throws InvalidPacketException {
                final Semantic semantic = Semantic.find(uint8(buffer));
                final int templateId = uint16(buffer);

                final Template template = templateResolver.lookup(templateId).orElseThrow(() -> new InvalidPacketException("Unknown Template ID: %d", templateId));

                final List<List<Value<?>>> values = new LinkedList<>();
                while (buffer.hasRemaining()) {
                    final DataRecord record = new DataRecord(templateResolver, template, buffer);
                    values.add(Lists.transform(record.fields, f -> f.value));
                }

                return new SubTemplateListValue(name, semantic, values);
            }

            @Override
            public int getMaximumFieldLength() {
                return 0xFFFF;
            }

            @Override
            public int getMinimumFieldLength() {
                return 3;
            }
        };
    }

    @Override
    public List<List<Value<?>>> getValue() {
        return this.values;
    }

    @Override
    public void visit(final Visitor visitor) {
        visitor.accept(this);
    }
}
