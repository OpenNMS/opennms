/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow9.proto;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint16;

import java.util.List;
import java.util.Optional;

import org.opennms.netmgt.telemetry.protocols.netflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.MissingTemplateException;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.InformationElement;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.InformationElementDatabase;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.UnsignedValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.Field;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.Scope;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.Session;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import io.netty.buffer.ByteBuf;

public final class ScopeFieldSpecifier implements Field, Scope {

    public static final String SCOPE_SYSTEM = "SCOPE:SYSTEM";
    public static final String SCOPE_INTERFACE = "SCOPE:INTERFACE";
    public static final String SCOPE_LINE_CARD = "SCOPE:LINE_CARD";
    public static final String SCOPE_CACHE = "SCOPE:CACHE";
    public static final String SCOPE_TEMPLATE = "SCOPE:TEMPLATE";

    /*
      0                   1                   2                   3
      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |     Scope Field Type N        |      Scope Field Length N     |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    */

    public final static int SIZE = 4;

    public final int fieldType; // uint16
    public final int fieldLength; // uint16

    public final InformationElement field;
    public final InformationElementDatabase informationElementDatabase;

    public ScopeFieldSpecifier(final InformationElementDatabase informationElementDatabase, final ByteBuf buffer) throws InvalidPacketException {
        this.fieldType = uint16(buffer);
        this.fieldLength = uint16(buffer);

        this.informationElementDatabase = informationElementDatabase;
        this.field = from(informationElementDatabase, this.fieldType)
                .orElseThrow(() -> new InvalidPacketException(buffer, "Invalid scope field type: 0x%04X", this.fieldType));

        if (this.fieldLength > this.field.getMaximumFieldLength() || this.fieldLength < this.field.getMinimumFieldLength()) {
            throw new InvalidPacketException(buffer, "Template scope field '%s' has illegal size: %d (min=%d, max=%d)",
                    this.field.getName(),
                    this.fieldLength,
                    this.field.getMinimumFieldLength(),
                    this.field.getMaximumFieldLength());
        }
    }

    @Override
    public Value<?> parse(Session.Resolver resolver, ByteBuf buffer) throws InvalidPacketException, MissingTemplateException {
        try {
            return this.field.parse(informationElementDatabase, resolver, buffer);
        } catch (final InvalidPacketException e) {
            throw new InvalidPacketException(e, "Failed to parse Netflow9 scope field: fieldType=%d", this.fieldType);
        }
    }

    @Override
    public int length() {
        return this.fieldLength;
    }

    @Override
    public String getName() {
        return this.field.getName();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("scopeFieldType", this.fieldType)
                .add("scopeFieldLength", this.fieldLength)
                .toString();
    }

    private static Optional<InformationElement> from(final InformationElementDatabase informationElementDatabase, final int fieldType) {
        switch (fieldType) {
            case 0x0001:
                return Optional.of(UnsignedValue.parserWith64Bit(informationElementDatabase, SCOPE_SYSTEM, Optional.empty()));
            case 0x0002:
                return Optional.of(UnsignedValue.parserWith64Bit(informationElementDatabase, SCOPE_INTERFACE, Optional.empty()));
            case 0x0003:
                return Optional.of(UnsignedValue.parserWith64Bit(informationElementDatabase, SCOPE_LINE_CARD, Optional.empty()));
            case 0x0004:
                return Optional.of(UnsignedValue.parserWith64Bit(informationElementDatabase, SCOPE_CACHE, Optional.empty()));
            case 0x0005:
                return Optional.of(UnsignedValue.parserWith64Bit(informationElementDatabase, SCOPE_TEMPLATE, Optional.empty()));
            default:
                return Optional.empty();
        }
    }

    public static List<Value<?>> buildScopeValues(final DataRecord record) {
        final ImmutableList.Builder<Value<?>> values = ImmutableList.builder();

        values.add(new UnsignedValue(ScopeFieldSpecifier.SCOPE_SYSTEM, record.set.packet.header.sourceId));
        values.add(new UnsignedValue(ScopeFieldSpecifier.SCOPE_TEMPLATE, record.set.template.id));

        return values.build();
    }
}
