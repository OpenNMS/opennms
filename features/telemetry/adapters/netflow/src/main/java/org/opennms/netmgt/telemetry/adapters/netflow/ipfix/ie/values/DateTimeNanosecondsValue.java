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

package org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.values;

import static org.opennms.netmgt.telemetry.adapters.netflow.ipfix.BufferUtils.bytes;

import java.nio.ByteBuffer;

import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.BufferUtils;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.Value;

public class DateTimeNanosecondsValue extends Value {
    public final long seconds;
    public final long fraction;

    public DateTimeNanosecondsValue(final String name,
                                    final long seconds,
                                    final long fraction) {
        super(name);
        this.seconds = seconds;
        this.fraction = fraction;
    }

    public static Value.Parser parser(final String name) {
        return new Value.Parser() {
            @Override
            public Value parse(final ByteBuffer buffer) {
                final long seconds = BufferUtils.uint32(buffer);
                final long fraction = BufferUtils.uint32(buffer);
                return new DateTimeNanosecondsValue(name, seconds, fraction);
            }

            @Override
            public int getMaximumFieldLength() {
                return 8;
            }

            @Override
            public int getMinimumFieldLength() {
                return 8;
            }
        };
    }
}
