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

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.BufferUtils;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.Value;

import com.google.common.base.MoreObjects;

public class IPv6AddressValue extends Value {
    public final Inet6Address inet6Address;

    public IPv6AddressValue(final String name,
                            final Inet6Address inet6Address) {
        super(name);
        this.inet6Address = inet6Address;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", getName())
                .add("inet6Address", inet6Address)
                .toString();
    }

    public static Value.Parser parser(final String name) {
        return new Value.Parser() {
            @Override
            public Value parse(final ByteBuffer buffer) throws IllegalValueException {
                try {
                    return new IPv6AddressValue(name, (Inet6Address) Inet4Address.getByAddress(BufferUtils.bytes(buffer, 16)));
                } catch (UnknownHostException e) {
                    throw new IllegalValueException("Error parsing IPv6 value: ", e);
                }
            }

            @Override
            public int getMaximumFieldLength() {
                return 16;
            }

            @Override
            public int getMinimumFieldLength() {
                return 16;
            }
        };
    }
}
