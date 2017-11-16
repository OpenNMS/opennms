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

import java.nio.ByteBuffer;

import org.opennms.netmgt.telemetry.listeners.flow.ie.InformationElementDatabase;
import org.opennms.netmgt.telemetry.listeners.flow.ie.Value;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.proto.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.session.TemplateManager;

import com.google.common.base.MoreObjects;

public class OctetArrayValue extends Value<byte[]> {
    public final byte[] value;

    public OctetArrayValue(final String name,
                           final byte[] value) {
        super(name);
        this.value = value;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", getName())
                .add("data", value)
                .toString();
    }

    public static Value.Parser parser(final String name) {
        return parserWithLimits(0, 0xFFFF).parser(name);
    }

    public static InformationElementDatabase.ValueParserFactory parserWithLimits(final int minimum, final int maximum) {
        return name -> new Parser() {
            @Override
            public Value<?> parse(TemplateManager.TemplateResolver templateResolver, ByteBuffer buffer) throws InvalidPacketException {
                return new OctetArrayValue(name, BufferUtils.bytes(buffer, buffer.remaining()));
            }

            @Override
            public int getMaximumFieldLength() {
                return maximum;
            }

            @Override
            public int getMinimumFieldLength() {
                return minimum;
            }
        };
    }

    @Override
    public byte[] getValue() {
        return this.value;
    }

    @Override
    public void visit(final Visitor visitor) {
        visitor.accept(this);
    }
}
