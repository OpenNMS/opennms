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

package org.opennms.netmgt.telemetry.listeners.flow.ipfix.session;

import java.nio.ByteBuffer;

import org.opennms.netmgt.telemetry.listeners.flow.ipfix.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.Value;

public final class EnterpriseField extends Field {

    public static final class EnterpriseValue extends Value<byte[]> {
        public final int informationElementId;
        public final long enterpriseNumber;

        public final byte[] data;

        public EnterpriseValue(final int informationElementId,
                               final long enterpriseNumber,
                               final byte[] data) {
            super("enterprise");
            this.informationElementId = informationElementId;
            this.enterpriseNumber = enterpriseNumber;

            this.data = data;
        }

        @Override
        public byte[] getValue() {
            return this.data;
        }

        @Override
        public void visit(Visitor visitor) {
            visitor.accept(this);
        }
    }

    private final int informationElementId;
    private final long enterpriseNumber;

    public EnterpriseField(final int length,
                           final int informationElementId,
                           final long enterpriseNumber) {
        super(length);
        this.informationElementId = informationElementId;
        this.enterpriseNumber = enterpriseNumber;
    }

    @Override
    public Value parse(final TemplateManager.TemplateResolver templateResolver, final ByteBuffer buffer) {
        return new EnterpriseValue(this.informationElementId, this.enterpriseNumber, BufferUtils.bytes(buffer, buffer.remaining()));
    }
}
