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

import org.opennms.netmgt.telemetry.protocols.netflow.parser.Protocol;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.NullValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class InformationElementDatabase {
    public static class Key {
        private final Protocol protocol;
        private final Optional<Long> enterpriseNumber;
        private final Integer informationElementIdentifier;
        private String source;

        public Key(final Protocol protocol,
                   final Optional<Long> enterpriseNumber,
                   final Integer informationElementNumber) {
            this(protocol, enterpriseNumber, informationElementNumber, null);
        }

        public Key(final Protocol protocol,
                   final Optional<Long> enterpriseNumber,
                   final Integer informationElementNumber,
                   final String source) {
            this.protocol = Objects.requireNonNull(protocol);
            this.enterpriseNumber = Objects.requireNonNull(enterpriseNumber);
            this.informationElementIdentifier = Objects.requireNonNull(informationElementNumber);
            this.source = source;
        }

        @Override
        public String toString() {
            return "Key{" +
                    "protocol=" + protocol +
                    ", enterpriseNumber=" + enterpriseNumber +
                    ", informationElementIdentifier=" + informationElementIdentifier +
                    ", source='" + source + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return protocol == key.protocol && Objects.equals(enterpriseNumber, key.enterpriseNumber) && Objects.equals(informationElementIdentifier, key.informationElementIdentifier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(protocol, enterpriseNumber, informationElementIdentifier);
        }
    }

    @FunctionalInterface
    public interface ValueParserFactory {
        InformationElement parser(final InformationElementDatabase database, final String name, final Optional<Semantics> semantics);
    }

    public interface Adder {
        void add(final InformationElementDatabase.Key key, final InformationElement element);

        default void add(final Protocol protocol,
                         final Optional<Long> enterpriseNumber,
                         final int informationElementNumber,
                         final ValueParserFactory parserFactory,
                         final String name,
                         final Optional<Semantics> semantics,
                         final InformationElementDatabase database) {
            this.add(new InformationElementDatabase.Key(protocol, enterpriseNumber, informationElementNumber), parserFactory.parser(database, name, semantics));
        }

        default void add(final Protocol protocol,
                         final Optional<Long> enterpriseNumber,
                         final int informationElementNumber,
                         final ValueParserFactory parserFactory,
                         final String name,
                         final Optional<Semantics> semantics,
                         final InformationElementDatabase database,
                         final String source) {
            this.add(new InformationElementDatabase.Key(protocol, enterpriseNumber, informationElementNumber, source), parserFactory.parser(database, name, semantics));
        }

        default void add(final Protocol protocol,
                         final int informationElementNumber,
                         final ValueParserFactory parserFactory,
                         final String name,
                         final Optional<Semantics> semantics,
                         final InformationElementDatabase database) {
            this.add(protocol, Optional.empty(), informationElementNumber, parserFactory, name, semantics, database);
        }

        default void add(final Protocol protocol,
                         final int informationElementNumber,
                         final ValueParserFactory parserFactory,
                         final String name,
                         final Semantics semantics,
                         final InformationElementDatabase database) {
            this.add(protocol, Optional.empty(), informationElementNumber, parserFactory, name, Optional.of(semantics), database);
        }

        void clear(final String source);
    }

    public interface Provider {
        void load(final Adder adder);
        InformationElementDatabase getDatabase();
        void setDatabase(final InformationElementDatabase database);
    }

    private Map<Key, InformationElement> elements;

    public InformationElementDatabase(final InformationElementXmlProvider informationElementXmlProvider) {
        this(new org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix.InformationElementProvider(),
                informationElementXmlProvider,
             new org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow9.InformationElementProvider());
    }

    public InformationElementDatabase(final Provider... providers) {
        addProviders(providers);
    }

    private void addProviders(final Provider... providers) {
        final AdderImpl adder = new AdderImpl();

        // Add null element - this derives from the standard but is required by some exporters
        adder.add(Protocol.NETFLOW9, 0, NullValue::parser, "null", Optional.empty(), this);
        adder.add(Protocol.IPFIX, 0, NullValue::parser, "null", Optional.empty(), this);

        // Load providers
        for (final Provider provider : providers) {
            provider.load(adder);
        }

        this.elements = adder.getInformationElementMap();
    }

    public Optional<InformationElement> lookup(final Protocol protocol, final Optional<Long> enterpriseNumber, final int informationElementIdentifier) {
        return Optional.ofNullable(this.elements.get(new Key(protocol, enterpriseNumber, informationElementIdentifier)));
    }

    public Optional<InformationElement> lookup(final Protocol protocol, final int informationElementIdentifier) {
        return lookup(protocol, Optional.empty(), informationElementIdentifier);
    }

    private static class AdderImpl implements Adder {
        private final Map<Key, InformationElement> informationElementMap = new HashMap<>();

        @Override
        public void add(final Key key, final InformationElement element) {
            synchronized (informationElementMap) {
                Objects.requireNonNull(key);
                Objects.requireNonNull(element);

                informationElementMap.put(key, element);
            }
        }

        public Map<Key, InformationElement> getInformationElementMap() {
            return this.informationElementMap;
        }

        @Override
        public void clear(final String source) {
            synchronized (informationElementMap) {
                informationElementMap.keySet().removeIf(key -> Objects.equals(key.source, source));
            }
        }
    }
}
