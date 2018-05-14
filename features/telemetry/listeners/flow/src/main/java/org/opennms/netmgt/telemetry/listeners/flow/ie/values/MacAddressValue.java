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

package org.opennms.netmgt.telemetry.listeners.flow.ie.values;

import static org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils.bytes;

import java.nio.ByteBuffer;
import java.util.Optional;

import org.opennms.netmgt.telemetry.listeners.flow.ie.InformationElement;
import org.opennms.netmgt.telemetry.listeners.flow.ie.Semantics;
import org.opennms.netmgt.telemetry.listeners.flow.ie.Value;
import org.opennms.netmgt.telemetry.listeners.flow.session.Session;

import com.google.common.base.MoreObjects;

public class MacAddressValue extends Value<byte[]> {
    public final byte[] value;

    public MacAddressValue(final String name,
                           final Optional<Semantics> semantics,
                           final byte[] value) {
        super(name, semantics);
        this.value = value;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", getName())
                .add("macAddressOctets", value)
                .toString();
    }

    public static InformationElement parser(final String name, final Optional<Semantics> semantics) {
        return new InformationElement() {
            @Override
            public Value<?> parse(final Session.Resolver resolver, final ByteBuffer buffer) {
                return new MacAddressValue(name, semantics, bytes(buffer, 6));
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public int getMinimumFieldLength() {
                return 6;
            }

            @Override
            public int getMaximumFieldLength() {
                return 6;
            }
        };
    }

    @Override
    public byte[] getValue() {
        return this.value;
    }

    @Override
    public void visit(final Visitor visitor) {
        visitor.accept(this);
    }
}
