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

package org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values;

import static org.opennms.netmgt.telemetry.common.utils.BufferUtils.bytes;

import java.nio.ByteBuffer;
import java.util.Optional;

import org.opennms.netmgt.telemetry.protocols.netflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.InformationElement;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.Session;

import com.google.common.base.MoreObjects;

public class UndeclaredValue extends Value<byte[]> {
    public final byte[] value;

    public UndeclaredValue(final int informationElementId,
                           final byte[] value) {
        this(Optional.empty(), informationElementId, value);
    }

    public UndeclaredValue(final Optional<Long> enterpriseNumber,
                           final int informationElementId,
                           final byte[] value) {
        super(nameFor(enterpriseNumber, informationElementId), Optional.empty());
        this.value = value;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", getName())
                .add("data", value)
                .toString();
    }

    @Override
    public byte[] getValue() {
        return this.value;
    }

    @Override
    public void visit(final Visitor visitor) {
        visitor.accept(this);
    }

    public static InformationElement parser(final int informationElementId) {
        return parser(Optional.empty(), informationElementId);
    }

    public static InformationElement parser(final Optional<Long> enterpriseNumber,
                                            final int informationElementId) {
        return new InformationElement() {
            @Override
            public Value<?> parse(final Session.Resolver resolver, final ByteBuffer buffer) throws InvalidPacketException {
                return new UndeclaredValue(enterpriseNumber, informationElementId, bytes(buffer, buffer.remaining()));
            }

            @Override
            public String getName() {
                return nameFor(enterpriseNumber, informationElementId);
            }

            @Override
            public int getMinimumFieldLength() {
                return 0;
            }

            @Override
            public int getMaximumFieldLength() {
                return 0xFFFF;
            }
        };
    }

    public static String nameFor(final Optional<Long> enterpriseNumber,
                                 final int informationElementId) {
        return enterpriseNumber.map(en -> Long.toString(en) + ':').orElse("") + Integer.toString(informationElementId);
    }
}
