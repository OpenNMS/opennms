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

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.opennms.netmgt.telemetry.protocols.netflow.parser.Protocol;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.NullValue;

import com.google.common.collect.ImmutableMap;

public class InformationElementDatabase {
    public static class Key {
        private final Protocol protocol;
        private final Optional<Long> enterpriseNumber;
        private final Integer informationElementIdentifier;

        public Key(final Protocol protocol,
                   final Optional<Long> enterpriseNumber,
                   final Integer informationElementNumber) {
            this.protocol = Objects.requireNonNull(protocol);
            this.enterpriseNumber = Objects.requireNonNull(enterpriseNumber);
            this.informationElementIdentifier = Objects.requireNonNull(informationElementNumber);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key that = (Key) o;
            return Objects.equals(this.protocol, that.protocol) &&
                    Objects.equals(this.enterpriseNumber, that.enterpriseNumber) &&
                    Objects.equals(this.informationElementIdentifier, that.informationElementIdentifier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.protocol, this.enterpriseNumber, this.informationElementIdentifier);
        }
    }

    @FunctionalInterface
    public interface ValueParserFactory {
        InformationElement parser(final String name, final Optional<Semantics> semantics);
    }

    public interface Adder {
        void add(final InformationElementDatabase.Key key, final InformationElement element);

        default void add(final Protocol protocol,
                         final Optional<Long> enterpriseNumber,
                         final int informationElementNumber,
                         final ValueParserFactory parserFactory,
                         final String name,
                         final Optional<Semantics> semantics) {
            this.add(new InformationElementDatabase.Key(protocol, enterpriseNumber, informationElementNumber), parserFactory.parser(name, semantics));
        }

        default void add(final Protocol protocol,
                         final int informationElementNumber,
                         final ValueParserFactory parserFactory,
                         final String name,
                         final Optional<Semantics> semantics) {
            this.add(protocol, Optional.empty(), informationElementNumber, parserFactory, name, semantics);
        }

        default void add(final Protocol protocol,
                         final int informationElementNumber,
                         final ValueParserFactory parserFactory,
                         final String name,
                         final Semantics semantics) {
            this.add(protocol, Optional.empty(), informationElementNumber, parserFactory, name, Optional.of(semantics));
        }
    }

    public interface Provider {
        void load(final Adder adder);
    }

    public static final InformationElementDatabase instance = new InformationElementDatabase(
            new org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix.InformationElementProvider(),
            new org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix.InformationElementXmlProvider(),
            new org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow9.InformationElementProvider());

    private final Map<Key, InformationElement> elements;

    InformationElementDatabase(final Provider... providers) {
        final AdderImpl adder = new AdderImpl();

        // Add null element - this derives from the standard but is required by some exporters
        adder.add(Protocol.NETFLOW9, 0, NullValue::parser, "null", Optional.empty());
        adder.add(Protocol.IPFIX, 0, NullValue::parser, "null", Optional.empty());

        // Load providers
        for (final Provider provider : providers) {
            provider.load(adder);
        }

        this.elements = adder.build();
    }

    public Optional<InformationElement> lookup(final Protocol protocol, final Optional<Long> enterpriseNumber, final int informationElementIdentifier) {
        return Optional.ofNullable(this.elements.get(new Key(protocol, enterpriseNumber, informationElementIdentifier)));
    }

    public Optional<InformationElement> lookup(final Protocol protocol, final int informationElementIdentifier) {
        return lookup(protocol, Optional.empty(), informationElementIdentifier);
    }

    private static class AdderImpl implements Adder {
        private final ImmutableMap.Builder<Key, InformationElement> builder = ImmutableMap.builder();

        @Override
        public void add(final Key key, final InformationElement element) {
            Objects.requireNonNull(key);
            Objects.requireNonNull(element);

            builder.put(key, element);
        }

        public Map<Key, InformationElement> build() {
            return this.builder.build();
        }
    }
}
