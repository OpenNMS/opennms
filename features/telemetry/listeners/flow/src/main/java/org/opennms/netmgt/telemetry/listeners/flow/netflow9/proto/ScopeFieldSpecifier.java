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

package org.opennms.netmgt.telemetry.listeners.flow.netflow9.proto;

import static org.opennms.netmgt.telemetry.listeners.flow.BufferUtils.uint16;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Optional;

import org.opennms.netmgt.telemetry.listeners.flow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.flow.ie.InformationElement;
import org.opennms.netmgt.telemetry.listeners.flow.ie.Value;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.UnsignedValue;
import org.opennms.netmgt.telemetry.listeners.flow.session.Field;
import org.opennms.netmgt.telemetry.listeners.flow.session.TemplateManager;

import com.google.common.base.MoreObjects;

public final class ScopeFieldSpecifier {
    public enum ScopeFieldType {
        SYSTEM(UnsignedValue.parserWith64Bit("SCOPE:SYSTEM", Optional.empty())),
        INTERFACE(UnsignedValue.parserWith64Bit("SCOPE:INTERFACE", Optional.empty())),
        LINE_CARD(UnsignedValue.parserWith64Bit("SCOPE:LINE_CARD", Optional.empty())),
        CACHE(UnsignedValue.parserWith64Bit("SCOPE:CACHE", Optional.empty())),
        TEMPLATE(UnsignedValue.parserWith64Bit("SCOPE:TEMPLATE", Optional.empty()));

        public final InformationElement parser;

        ScopeFieldType(final InformationElement parser) {
            this.parser = parser;
        }

        public static ScopeFieldType from(final ByteBuffer buffer) throws InvalidPacketException {
            final int type = uint16(buffer);
            switch (type) {
                case 0x0001:
                    return ScopeFieldType.SYSTEM;
                case 0x0002:
                    return ScopeFieldType.INTERFACE;
                case 0x0003:
                    return ScopeFieldType.LINE_CARD;
                case 0x0004:
                    return ScopeFieldType.CACHE;
                case 0x0005:
                    return ScopeFieldType.TEMPLATE;
                default:
                    throw new InvalidPacketException(buffer, "Invalid scope field type: 0x%04X", type);
            }
        }
    }

    private static class ScopedField implements Field {
        public final int length;
        public final ScopeFieldType type;

        private ScopedField(final int length,
                            final ScopeFieldType type) {
            this.length = length;
            this.type = Objects.requireNonNull(type);
        }

        @Override
        public int length() {
            return this.length;
        }

        @Override
        public Value<?> parse(final TemplateManager.TemplateResolver templateResolver,
                              final ByteBuffer buffer) throws InvalidPacketException {
            return this.type.parser.parse(templateResolver, buffer);
        }
    }

    /*
      0                   1                   2                   3
      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |     Scope Field Type N        |      Scope Field Length N     |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    */

    public final static int SIZE = 4;

    public final ScopeFieldType scopeFieldType;
    public final int scopeFieldLength; // uint16

    public final Field specifier;

    public ScopeFieldSpecifier(final ByteBuffer buffer) throws InvalidPacketException {
        this.scopeFieldType = ScopeFieldType.from(buffer);
        this.scopeFieldLength = uint16(buffer);

        if (this.scopeFieldLength > this.scopeFieldType.parser.getMaximumFieldLength() || this.scopeFieldLength < this.scopeFieldType.parser.getMinimumFieldLength()) {
            throw new InvalidPacketException(buffer, "Template scope field '%s' has illegal size: %d (min=%d, max=%d)",
                    this.scopeFieldType.parser.getName(),
                    this.scopeFieldLength,
                    this.scopeFieldType.parser.getMinimumFieldLength(),
                    this.scopeFieldType.parser.getMaximumFieldLength());
        }

        this.specifier = new ScopedField(this.scopeFieldLength, this.scopeFieldType);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("scopeFieldType", this.scopeFieldType)
                .add("scopeFieldLength", this.scopeFieldLength)
                .toString();
    }
}
