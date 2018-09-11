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

package org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix.proto;

import static org.opennms.netmgt.telemetry.common.utils.BufferUtils.uint16;
import static org.opennms.netmgt.telemetry.common.utils.BufferUtils.uint32;

import java.nio.ByteBuffer;
import java.util.Optional;

import org.opennms.netmgt.telemetry.protocols.netflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.Protocol;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.InformationElement;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.InformationElementDatabase;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.UndeclaredValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.Field;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.Scope;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;

public final class FieldSpecifier implements Field, Scope {
    private static final Logger LOG = LoggerFactory.getLogger(FieldSpecifier.class);

    /*
     0                   1                   2                   3
     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |E|  Information Element ident. |        Field Length           |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |                      Enterprise Number                        |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    */

    public final int informationElementId; // uint16 { enterprise_bit:1, element_id: 15 }
    public final int fieldLength; // uint16

    public final Optional<Long> enterpriseNumber; // uint32

    public final InformationElement informationElement;

    public FieldSpecifier(final ByteBuffer buffer) throws InvalidPacketException {
        final int elementId = uint16(buffer);

        this.informationElementId = elementId & 0x7FFF;
        this.fieldLength = uint16(buffer);

        if ((elementId & 0x8000) == 0) {
            this.enterpriseNumber = Optional.empty();
        } else {
            long enterpriseNumber = uint32(buffer);
            this.enterpriseNumber = Optional.of(enterpriseNumber);
        }

        this.informationElement = InformationElementDatabase.instance
                .lookup(Protocol.IPFIX, this.enterpriseNumber, this.informationElementId).orElseGet(() -> {
                    LOG.warn("Undeclared information element: {}", UndeclaredValue.nameFor(this.enterpriseNumber, this.informationElementId));
                    return UndeclaredValue.parser(this.enterpriseNumber, this.informationElementId);
                });

        if (this.fieldLength > this.informationElement.getMaximumFieldLength() || this.fieldLength < this.informationElement.getMinimumFieldLength()) {
            throw new InvalidPacketException(buffer, "Template field '%s' has illegal size: %d (min=%d, max=%d)",
                    this.informationElement.getName(),
                    this.fieldLength,
                    this.informationElement.getMinimumFieldLength(),
                    this.informationElement.getMaximumFieldLength());
        }
    }

    @Override
    public Value<?> parse(final Session.Resolver resolver, final ByteBuffer buffer) throws InvalidPacketException {
        return this.informationElement.parse(resolver, buffer);
    }

    @Override
    public int length() {
        return this.fieldLength;
    }

    @Override
    public String getName() {
        return this.informationElement.getName();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("informationElementId", informationElementId)
                .add("enterpriseNumber", enterpriseNumber)
                .add("fieldLength", fieldLength)
                .toString();
    }
}
