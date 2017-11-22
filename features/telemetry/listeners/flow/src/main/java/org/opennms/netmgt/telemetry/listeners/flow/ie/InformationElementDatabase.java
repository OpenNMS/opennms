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

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.opennms.netmgt.telemetry.listeners.flow.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

public class InformationElementDatabase {
    private static final Logger LOG = LoggerFactory.getLogger(InformationElementDatabase.class);

    public static class Key {
        private final Protocol protocol;
        private final Optional<Long> enterpriseNumber;
        private final Integer informtionElementIdentifier;

        public Key(final Protocol protocol,
                   final Optional<Long> enterpriseNumber,
                   final Integer informationElementNumber) {
            this.protocol = Objects.requireNonNull(protocol);
            this.enterpriseNumber = Objects.requireNonNull(enterpriseNumber);
            this.informtionElementIdentifier = Objects.requireNonNull(informationElementNumber);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key that = (Key) o;
            return Objects.equals(this.protocol, that.protocol) &&
                    Objects.equals(this.enterpriseNumber, that.enterpriseNumber) &&
                    Objects.equals(this.informtionElementIdentifier, that.informtionElementIdentifier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.protocol, this.enterpriseNumber, this.informtionElementIdentifier);
        }
    }

    @FunctionalInterface
    public interface ValueParserFactory {
        Value.Parser parser(final String name);
    }

    public interface Adder {
        void add(final InformationElementDatabase.Key key, final InformationElement element);

        default void add(final Protocol protocol,
                         final Optional<Long> enterpriseNumber,
                         final Integer informationElementNumber,
                         final String name,
                         final InformationElementDatabase.ValueParserFactory parserFactory,
                         final Optional<Semantics> semantics) {
            this.add(new InformationElementDatabase.Key(protocol, enterpriseNumber, informationElementNumber), new InformationElement(name, parserFactory.parser(name), semantics));
        }

        default void add(final Protocol protocol,
                         final Integer informationElementNumber,
                         final String name,
                         final InformationElementDatabase.ValueParserFactory parserFactory,
                         final Optional<Semantics> semantics) {
            this.add(protocol, Optional.empty(), informationElementNumber, name, parserFactory, semantics);
        }
    }

    public interface Provider {
        void load(final Adder adder);
    }

    public static final InformationElementDatabase instance = new InformationElementDatabase(
            new org.opennms.netmgt.telemetry.listeners.flow.ipfix.InformationElementProvider(),
            new org.opennms.netmgt.telemetry.listeners.flow.v9.InformationElementProvider());

    private final Map<Key, InformationElement> elements;

    private InformationElementDatabase(final Provider... providers) {
        final AdderImpl adder = new AdderImpl();
        for (final Provider provider : providers) {
            provider.load(adder);
        }

        this.elements = adder.build();
    }

    public Optional<InformationElement> lookup(final Protocol protocol, final Optional<Long> enterpriseNumber, final int informationElementIdentifier) {
        return Optional.ofNullable(this.elements.get(new Key(protocol, enterpriseNumber, informationElementIdentifier)));
    }

    public Optional<InformationElement> lookup(final Protocol protocol, final int informationElementIdetifier) {
        return lookup(protocol, Optional.empty(), informationElementIdetifier);
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
