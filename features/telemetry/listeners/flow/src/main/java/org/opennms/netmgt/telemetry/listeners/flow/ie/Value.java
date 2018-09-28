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

package org.opennms.netmgt.telemetry.listeners.flow.ie;

import java.util.Objects;
import java.util.Optional;

import org.opennms.netmgt.telemetry.listeners.flow.ie.values.BooleanValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.DateTimeValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.FloatValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.IPv4AddressValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.IPv6AddressValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.ListValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.MacAddressValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.NullValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.OctetArrayValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.SignedValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.StringValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.UndeclaredValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.UnsignedValue;

public abstract class Value<T> {

    public interface Visitor {
        void accept(final NullValue value);

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

        void accept(final UndeclaredValue value);

    }

    private final String name;

    private final Optional<Semantics> semantics;

    protected Value(final String name,
                    final Optional<Semantics> semantics) {
        this.name = Objects.requireNonNull(name);
        this.semantics = Objects.requireNonNull(semantics);
    }

    public String getName() {
        return this.name;
    }

    public Optional<Semantics> getSemantics() {
        return this.semantics;
    }

    public abstract T getValue();

    public abstract void visit(final Visitor visitor);

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Value)) return false;
        final Value<?> value = (Value<?>) o;
        return Objects.equals(this.name, value.name) &&
                Objects.equals(this.getValue(), value.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.getValue());
    }
}
