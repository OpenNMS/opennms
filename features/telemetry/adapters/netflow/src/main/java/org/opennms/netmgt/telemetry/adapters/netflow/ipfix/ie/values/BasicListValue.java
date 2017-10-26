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

import static org.opennms.netmgt.telemetry.adapters.netflow.ipfix.BufferUtils.uint8;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.FieldSpecifier;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.FieldValue;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.InvalidPacketException;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.Packet;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.Value;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.session.Template;

public class BasicListValue extends ListValue {
    public final List<Value> values;

    public BasicListValue(final String name,
                          final Semantic semantic,
                          final List<Value> values) {
        super(name, semantic);
        this.values = values;
    }

    public static BasicListValue parse(final String name,
                                       final ByteBuffer buffer) throws InvalidPacketException {
        final Semantic semantic = Semantic.find(uint8(buffer));
        final FieldSpecifier specifier = new FieldSpecifier(buffer);

        final Template.Field templateField = Packet.buildField(specifier);

        final List<Value> values = new LinkedList<>();
        while (buffer.hasRemaining()) {
            values.add(new FieldValue(templateField, buffer).value);
        }

        return new BasicListValue(name, semantic, values);
    }
}
