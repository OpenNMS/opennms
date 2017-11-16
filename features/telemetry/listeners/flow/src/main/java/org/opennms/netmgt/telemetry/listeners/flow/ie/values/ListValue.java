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

package org.opennms.netmgt.telemetry.listeners.flow.ie.values;

import static org.opennms.netmgt.telemetry.listeners.flow.ipfix.BufferUtils.uint16;
import static org.opennms.netmgt.telemetry.listeners.flow.ipfix.BufferUtils.uint8;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.opennms.netmgt.telemetry.listeners.flow.ipfix.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.flow.ie.Value;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.proto.DataRecord;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.proto.FieldSpecifier;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.proto.FieldValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.proto.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.proto.Set;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.proto.SetHeader;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.session.Template;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.session.TemplateManager;

import com.google.common.collect.Lists;

public class ListValue extends Value<List<List<Value<?>>>> {

    public enum Semantic {
        UNDEFINED,
        NONE_OF,
        EXACTLY_ONE_OF,
        ONE_OR_MORE_OF,
        ALL_OF,
        ORDERED;

        public static Semantic find(final int i) throws InvalidPacketException {
            switch (i) {
                case 0xFF:
                    return UNDEFINED;
                case 0x00:
                    return NONE_OF;
                case 0x01:
                    return EXACTLY_ONE_OF;
                case 0x02:
                    return ONE_OR_MORE_OF;
                case 0x03:
                    return ALL_OF;
                case 0x04:
                    return ORDERED;
                default:
                    throw new InvalidPacketException("Illegal semantic value: 0x%02x", i);
            }
        }
    }

    private final Semantic semantic;

    private final List<List<Value<?>>> values;

    public ListValue(final String name,
                     final Semantic semantic,
                     final List<List<Value<?>>> values) {
        super(name);
        this.semantic = semantic;
        this.values = values;
    }

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
    public static Parser parserWithBasicList(final String name) {
        return new Value.Parser() {

            @Override
            public Value<?> parse(final TemplateManager.TemplateResolver templateResolver, final ByteBuffer buffer) throws InvalidPacketException {
                final Semantic semantic = Semantic.find(uint8(buffer));
                final FieldSpecifier field = new FieldSpecifier(buffer);

                final List<List<Value<?>>> values = new LinkedList<>();
                while (buffer.hasRemaining()) {
                    values.add(Collections.singletonList(new FieldValue(templateResolver, field.specifier, buffer).value));
                }

                return new ListValue(name, semantic, values);
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
    public static Parser parserWithSubTemplateList(final String name) {
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

                return new ListValue(name, semantic, values);
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

    /*
      0                   1                   2                   3
      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |    Semantic   |         Template ID X         |Data Records...|
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     | ... Length X  |        Data Record X.1 Content ...            |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |                              ...                              |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |      ...      |        Data Record X.2 Content ...            |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |                              ...                              |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |      ...      |        Data Record X.L Content ...            |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |                              ...                              |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |      ...      |         Template ID Y         |Data Records...|
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     | ... Length Y  |        Data Record  Y.1 Content ...           |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |                              ...                              |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |      ...      |        Data Record Y.2 Content ...            |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |                              ...                              |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |      ...      |        Data Record Y.M Content ...            |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |                              ...                              |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |      ...      |         Template ID Z         |Data Records...|
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     | ... Length Z  |        Data Record Z.1 Content ...            |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |                              ...                              |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |      ...      |        Data Record Z.2 Content ...            |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |                              ...                              |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |      ...      |        Data Record Z.N Content ...            |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |                              ...                              |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |      ...      |
     +-+-+-+-+-+-+-+-+
    */
    public static Parser parserWithSubTemplateMultiList(final String name) {
        return new Value.Parser() {

            @Override
            public Value<?> parse(final TemplateManager.TemplateResolver templateResolver, final ByteBuffer buffer) throws InvalidPacketException {
                final Semantic semantic = Semantic.find(BufferUtils.uint8(buffer));

                final List<List<Value<?>>> values = new LinkedList<>();
                while (buffer.hasRemaining()) {
                    final SetHeader header = new SetHeader(buffer);
                    if (header.setId <= 255) {
                        throw new InvalidPacketException("Invalid template ID: %d", header.setId);
                    }

                    final ByteBuffer payloadBuffer = BufferUtils.slice(buffer, header.length - SetHeader.SIZE);
                    final Set<DataRecord> dataSet = new Set<>(header, DataRecord.parser(templateResolver, header.setId), payloadBuffer);

                    values.addAll(Lists.transform(dataSet.records, r -> Lists.transform(r.fields, f -> f.value)));
                }

                return new ListValue(name, semantic, values);
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
    public List<List<Value<?>>> getValue() {
        return this.values;
    }

    @Override
    public void visit(final Visitor visitor) {
        visitor.accept(this);
    }
}
