/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.telemetry.protocols.netflow.parser.ie;

import java.util.Objects;
import java.util.Optional;

import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.BooleanValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.DateTimeValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.FloatValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.IPv4AddressValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.IPv6AddressValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.ListValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.MacAddressValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.NullValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.OctetArrayValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.SignedValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.StringValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.UndeclaredValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.UnsignedValue;

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
