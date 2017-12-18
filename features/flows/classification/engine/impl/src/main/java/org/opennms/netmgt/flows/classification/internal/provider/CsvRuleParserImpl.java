/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.classification.internal.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.opennms.netmgt.flows.classification.CsvRuleParser;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;
import org.opennms.netmgt.flows.classification.persistence.api.RuleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class CsvRuleParserImpl implements CsvRuleParser {

    private Logger LOG = LoggerFactory.getLogger(CsvRuleParserImpl.class);

    private static class Key {
        private final String serviceName;
        private final int port;

        private Key(String serviceName, String port) {
            this.serviceName = Objects.requireNonNull(serviceName);
            this.port = Integer.parseInt(Objects.requireNonNull(port));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final Key key = (Key) o;
            boolean equals = Objects.equals(serviceName, key.serviceName)
                    && Objects.equals(port, key.port);
            return equals;
        }

        @Override
        public int hashCode() {
            return Objects.hash(serviceName, port);
        }

        public String getServiceName() {
            return serviceName;
        }

        public int getPort() {
            return port;
        }
    }

    @Override
    public List<Rule> parse(InputStream inputStream) throws IOException {
        Objects.requireNonNull(inputStream);

        final Multimap<Key, String> protocolPortMapping = ArrayListMultimap.create();
        final CSVParser parser = CSVFormat.RFC4180.parse(new InputStreamReader(inputStream));
        for (CSVRecord record : parser.getRecords()) {
            LOG.debug("Parsing record '{}'", record);
            final String serviceName = record.get(0);
            final String port = record.get(1);
            final String protocol = record.get(2);

            // Ignore empty services
            if ("".equals(serviceName)) {
                LOG.debug("Record {} has empty service name. Skipping.", record);
                continue;
            }

            // Only accept if we have an actual port
            if (!isNumber(port)) {
                LOG.debug("Port {} of record {} is not a valid number. Skipping.", port, record);
                continue;
            }

            // Create PortMapping
            protocolPortMapping.put(new Key(serviceName, port), protocol);
        }

        // Convert to rules
        final Map<Key, Collection<String>> collectionMap = protocolPortMapping.asMap();

        final List<Rule> rules = collectionMap.keySet().stream()
                .map(key -> new RuleBuilder()
                        .withName(key.getServiceName())
                        .withPort(key.getPort())
                        .withProtocol(collectionMap.get(key).stream().collect(Collectors.joining(",")))
                        .build()).collect(Collectors.toList());
        return rules;
    }

    private static boolean isNumber(String input) {
        try {
            Long.parseLong(input);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
