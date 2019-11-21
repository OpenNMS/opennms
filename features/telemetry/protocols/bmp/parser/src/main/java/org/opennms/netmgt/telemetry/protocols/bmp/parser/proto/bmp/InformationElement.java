/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp;

import static org.opennms.netmgt.telemetry.common.utils.BufferUtils.bytes;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.opennms.netmgt.telemetry.protocols.bmp.parser.InvalidPacketException;

public class InformationElement extends TLV<InformationElement.Type, String, Void> {

    public InformationElement(final ByteBuffer buffer) throws InvalidPacketException {
        super(buffer, Type::from, null);
    }

    public enum Type implements TLV.Type<String, Void> {
        STRING {
            @Override
            public String parse(final ByteBuffer buffer, final Void parameter) {
                return new String(bytes(buffer, buffer.remaining()), StandardCharsets.UTF_8);
            }
        },

        SYS_DESCR {
            @Override
            public String parse(final ByteBuffer buffer, final Void parameter) {
                return new String(bytes(buffer, buffer.remaining()), StandardCharsets.US_ASCII);
            }
        },

        SYS_NAME {
            @Override
            public String parse(final ByteBuffer buffer, final Void parameter) {
                return new String(bytes(buffer, buffer.remaining()), StandardCharsets.US_ASCII);
            }
        };

        private static Type from(final int type) {
            switch (type) {
                case 0:
                    return STRING;
                case 1:
                    return SYS_DESCR;
                case 2:
                    return SYS_NAME;
                default:
                    throw new IllegalArgumentException("Unknown information type");
            }
        }
    }
}
