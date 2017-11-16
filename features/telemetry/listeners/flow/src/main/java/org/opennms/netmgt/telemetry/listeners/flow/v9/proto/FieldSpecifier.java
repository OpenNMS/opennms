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

package org.opennms.netmgt.telemetry.listeners.flow.v9.proto;

import java.nio.ByteBuffer;

import org.opennms.netmgt.telemetry.listeners.flow.ipfix.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.flow.ie.InformationElement;
import org.opennms.netmgt.telemetry.listeners.flow.ie.InformationElementDatabase;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.proto.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.session.Field;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.session.StandardField;

import com.google.common.base.MoreObjects;

public final class FieldSpecifier {

    /*
      0                   1                   2                   3
      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |        Field Type N           |         Field Length N        |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    */

    public final static int SIZE = 4;

    public final int fieldType; // uint16
    public final int fieldLength; // uint16

    public final Field specifier;

    public FieldSpecifier(final ByteBuffer buffer) throws InvalidPacketException {
        this.fieldType = BufferUtils.uint16(buffer);
        this.fieldLength = BufferUtils.uint16(buffer);

        final InformationElement informationElement = InformationElementDatabase.instance
                .lookup(this.fieldType)
                .orElseThrow(() -> new InvalidPacketException("Undefined information element ID: %d", this.fieldType));

        if (this.fieldLength > informationElement.getMaximumFieldLength() || this.fieldLength < informationElement.getMinimumFieldLength()) {
            throw new InvalidPacketException("Template field has illegal size: %d (min=%d, max=%d)",
                    this.fieldLength,
                    informationElement.getMinimumFieldLength(),
                    informationElement.getMaximumFieldLength());
        }

        this.specifier = new StandardField(this.fieldLength, informationElement);

        if (this.fieldLength < 1) {
            throw new InvalidPacketException("Zero-sized field");
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("fieldType", fieldType)
                .add("fieldLength", fieldLength)
                .toString();
    }
}
