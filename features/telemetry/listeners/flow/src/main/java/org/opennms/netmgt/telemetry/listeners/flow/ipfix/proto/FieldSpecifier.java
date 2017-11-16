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

package org.opennms.netmgt.telemetry.listeners.flow.ipfix.proto;

import java.nio.ByteBuffer;
import java.util.Optional;

import org.opennms.netmgt.telemetry.listeners.flow.ipfix.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.flow.ie.InformationElement;
import org.opennms.netmgt.telemetry.listeners.flow.ie.InformationElementDatabase;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.session.EnterpriseField;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.session.Field;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.session.StandardField;

import com.google.common.base.MoreObjects;

public final class FieldSpecifier {

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

    public final Field specifier;

    public FieldSpecifier(final ByteBuffer buffer) throws InvalidPacketException {
        final int elementId = BufferUtils.uint16(buffer);

        this.informationElementId = elementId & 0x7FFF;
        this.fieldLength = BufferUtils.uint16(buffer);

        if ((elementId & 0x8000) == 0) {
            this.enterpriseNumber = Optional.empty();

            final InformationElement informationElement = InformationElementDatabase.instance
                    .lookup(this.informationElementId)
                    .orElseThrow(() -> new InvalidPacketException("Undefined information element ID: %d", this.informationElementId));

            if (this.fieldLength > informationElement.getMaximumFieldLength() || this.fieldLength < informationElement.getMinimumFieldLength()) {
                throw new InvalidPacketException("Template field has illegal size: %d (min=%d, max=%d)",
                        this.fieldLength,
                        informationElement.getMinimumFieldLength(),
                        informationElement.getMaximumFieldLength());
            }

            this.specifier = new StandardField(this.fieldLength, informationElement);

        } else {
            long enterpriseNumber = BufferUtils.uint32(buffer);
            this.enterpriseNumber = Optional.of(enterpriseNumber);

            this.specifier = new EnterpriseField(this.fieldLength, this.informationElementId, enterpriseNumber);
        }

        if (this.fieldLength < 1) {
            throw new InvalidPacketException("Zero-sized field");
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("informationElementId", informationElementId)
                .add("fieldLength", fieldLength)
                .add("enterpriseNumber", enterpriseNumber)
                .toString();
    }
}
