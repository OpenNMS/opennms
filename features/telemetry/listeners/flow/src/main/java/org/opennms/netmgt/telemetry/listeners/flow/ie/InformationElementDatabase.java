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

import java.util.Optional;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;

public class InformationElementDatabase {

    public static class Key {
        private final Optional<Long> enterpriseNumber;
        private final Integer informtionElementIdentifier;

        public Key(Optional<Long> enterpriseNumber, Integer informationElementNumber) {
            this.enterpriseNumber = enterpriseNumber;
            this.informtionElementIdentifier = informationElementNumber;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return Objects.equal(enterpriseNumber, key.enterpriseNumber) &&
                    Objects.equal(informtionElementIdentifier, key.informtionElementIdentifier);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(enterpriseNumber, informtionElementIdentifier);
        }

        public Optional<Long> getEnterpriseNumber() {
            return enterpriseNumber;
        }

        public Integer getInformtionElementIdentifier() {
            return informtionElementIdentifier;
        }
    }

    @FunctionalInterface
    public interface ValueParserFactory {
        Value.Parser parser(final String name);
    }

    public interface Provider {
        void load(final ImmutableMap.Builder<InformationElementDatabase.Key, InformationElement> builder);
    }

    public static final InformationElementDatabase instance = new InformationElementDatabase(
            new org.opennms.netmgt.telemetry.listeners.flow.ipfix.InformationElementProvider(),
            new org.opennms.netmgt.telemetry.listeners.flow.v9.InformationElementProvider());

    private final ImmutableMap<Key, InformationElement> elements;

    private InformationElementDatabase(final Provider... providers) {
        final ImmutableMap.Builder<Key, InformationElement> builder = ImmutableMap.builder();

        for (final Provider provider : providers) {
            provider.load(builder);
        }
        this.elements = builder.build();
    }

    public Optional<InformationElement> lookup(final Optional<Long> enterpriseNumber, final int informationElementIdentifier) {
        return Optional.ofNullable(this.elements.get(new Key(enterpriseNumber, informationElementIdentifier)));
    }

    public Optional<InformationElement> lookup(final int informationElementIdetifier) {
        return lookup(Optional.empty(), informationElementIdetifier);
    }
}
