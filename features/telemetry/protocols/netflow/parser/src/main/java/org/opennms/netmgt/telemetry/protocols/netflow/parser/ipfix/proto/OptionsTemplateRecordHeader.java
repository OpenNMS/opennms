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

import java.nio.ByteBuffer;

import org.opennms.netmgt.telemetry.protocols.netflow.parser.InvalidPacketException;

import com.google.common.base.MoreObjects;

public final class OptionsTemplateRecordHeader {

    /*
      0                   1                   2                   3
      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |         Template ID (> 255)   |         Field Count           |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |      Scope Field Count        |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    */

    public static final int SIZE = 6;

    public final int templateId; // uint16
    public final int fieldCount; // uint16
    public final int scopeFieldCount; // uint16

    public OptionsTemplateRecordHeader(final ByteBuffer buffer) throws InvalidPacketException {
        this.templateId = uint16(buffer);
        if (this.templateId <= 255 && this.templateId != FlowSetHeader.OPTIONS_TEMPLATE_SET_ID) {
            // Since Template IDs are used as Set IDs in the Sets they describe
            throw new InvalidPacketException(buffer, "Invalid template ID: %d", this.templateId);
        }

        this.fieldCount = uint16(buffer);

        this.scopeFieldCount = uint16(buffer);
        if (this.scopeFieldCount > this.fieldCount) {
            throw new InvalidPacketException(buffer, "More scope fields than fields available: %d > %d", this.scopeFieldCount, this.fieldCount);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("templateId", templateId)
                .add("fieldCount", fieldCount)
                .add("scopeFieldCount", scopeFieldCount)
                .toString();
    }
}
