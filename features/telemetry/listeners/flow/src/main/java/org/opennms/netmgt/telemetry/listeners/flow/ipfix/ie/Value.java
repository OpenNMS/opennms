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

package org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie;

import java.nio.ByteBuffer;

import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.BooleanValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.DateTimeValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.FloatValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.IPv4AddressValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.IPv6AddressValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.ListValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.MacAddressValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.OctetArrayValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.SignedValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.StringValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.UnsignedValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.proto.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.session.EnterpriseField;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.session.TemplateManager;

public abstract class Value<T> {

    public interface Parser {
        Value<?> parse(final TemplateManager.TemplateResolver templateResolver, final ByteBuffer buffer) throws InvalidPacketException;

        int getMaximumFieldLength();

        int getMinimumFieldLength();
    }

    public interface Visitor {
        void accept(final BooleanValue value);
        void accept(final DateTimeValue value);
        void accept(final FloatValue value);
        void accept(final IPv4AddressValue value);
        void accept(final IPv6AddressValue value);
        void accept(final MacAddressValue value);
        void accept(final OctetArrayValue value);
        void accept(final SignedValue value);
        void accept(final StringValue value);
        void accept(final UnsignedValue value);
        void accept(final ListValue value);

        void accept(final EnterpriseField.EnterpriseValue value);

    }

    private final String name;

    protected Value(final String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public abstract T getValue();

    public abstract void visit(final Visitor visitor);
}
