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

import static org.opennms.netmgt.telemetry.listeners.flow.ipfix.BufferUtils.uint8;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import org.opennms.netmgt.telemetry.listeners.flow.ipfix.proto.FieldSpecifier;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.proto.FieldValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.proto.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.Value;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.session.TemplateManager;

public class BasicListValue extends ListValue<Value<?>> {
    /*
      0                   1                   2                   3
      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |   Semantic    |0|          Field ID           |   Element...  |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     | ...Length     |           basicList Content ...               |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |                              ...                              |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |                              ...                              |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    */

    private final List<Value<?>> values;

    public BasicListValue(final String name,
                          final Semantic semantic,
                          final List<Value<?>> values) {
        super(name, semantic);
        this.values = values;
    }

    public static Parser parser(final String name) {
        return new Value.Parser() {

            @Override
            public Value<?> parse(final TemplateManager.TemplateResolver templateResolver, final ByteBuffer buffer) throws InvalidPacketException {
                final Semantic semantic = Semantic.find(uint8(buffer));
                final FieldSpecifier field = new FieldSpecifier(buffer);

                final List<Value<?>> values = new LinkedList<>();
                while (buffer.hasRemaining()) {
                    values.add(new FieldValue(templateResolver, field.specifier, buffer).value);
                }

                return new BasicListValue(name, semantic, values);
            }

            @Override
            public int getMaximumFieldLength() {
                return 0xFFFF;
            }

            @Override
            public int getMinimumFieldLength() {
                return 1 + 4;
            }
        };
    }

    @Override
    public List<Value<?>> getValue() {
        return this.values;
    }

    @Override
    public void visit(final Visitor visitor) {
        visitor.accept(this);
    }
}
