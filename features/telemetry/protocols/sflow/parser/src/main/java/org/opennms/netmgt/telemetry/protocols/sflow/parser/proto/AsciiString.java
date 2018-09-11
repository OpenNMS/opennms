/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.sflow.parser.proto;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.opennms.netmgt.telemetry.common.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;

import com.google.common.base.MoreObjects;

public class AsciiString {
    public final int length;
    public final String value;

    public AsciiString(final ByteBuffer buffer) throws InvalidPacketException {
        this.length = (int) BufferUtils.uint32(buffer);
        this.value = new String(BufferUtils.bytes(buffer, this.length), StandardCharsets.US_ASCII);

        // Skip over optional padding
        BufferUtils.skip(buffer, (4 - (this.length % 4)) % 4);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("length", this.length)
                .add("value", this.value)
                .toString();
    }
}
