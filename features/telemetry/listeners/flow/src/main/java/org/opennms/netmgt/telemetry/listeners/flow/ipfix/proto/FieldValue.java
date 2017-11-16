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

import static org.opennms.netmgt.telemetry.listeners.flow.ipfix.BufferUtils.slice;
import static org.opennms.netmgt.telemetry.listeners.flow.ipfix.BufferUtils.uint16;
import static org.opennms.netmgt.telemetry.listeners.flow.ipfix.BufferUtils.uint8;

import java.nio.ByteBuffer;

import org.opennms.netmgt.telemetry.listeners.flow.ie.Value;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.session.Field;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.session.TemplateManager;

public class FieldValue {

    /*
      0                   1                   2                   3
      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     | Length (< 255)|          Information Field                  |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |                      ... continuing as needed                 |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

      0                   1                   2                   3
      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |      255      |      Length (0 to 65535)      |       IE      |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |                      ... continuing as needed                 |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    */

    public static final int VARIABLE_SIZED = 0xFFFF;
    public static final int VARIABLE_SIZED_EXTENDED = 0xFF;

    public final Value value;

    public FieldValue(final TemplateManager.TemplateResolver templateResolver, final Field templateField, final ByteBuffer buffer) throws InvalidPacketException {
        int length = templateField.length;
        if (length == VARIABLE_SIZED) {
            length = uint8(buffer);
            if (length == VARIABLE_SIZED_EXTENDED) {
                length = uint16(buffer);
            }
        }

        value = templateField.parse(templateResolver, slice(buffer, length));
    }
}
