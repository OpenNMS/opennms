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

package org.opennms.netmgt.flows.classification.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.opennms.netmgt.flows.classification.persistence.api.Rule;
import org.opennms.netmgt.flows.classification.persistence.api.RuleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

// Provides all rules defined in https://www.iana.org/assignments/service-names-port-numbers/service-names-port-numbers.txt
// NOTE: Empty rules (e.g. 24/udp # any private mail system) are ignored.
public class StaticClassificationRuleProvider implements ClassificationRuleProvider {

    private static final Logger LOG = LoggerFactory.getLogger(StaticClassificationRuleProvider.class);

    private final List<Rule> rules = new ArrayList<>();

    public StaticClassificationRuleProvider() throws IOException {
        final InputStream inputStream = getClass().getResourceAsStream("/services");
        final List<String> lines = readFromInputStream(inputStream);

        // key: service/port, value: list of protocols (e.g. tcp, udp)
        final Map<Object[], List<String>> servicePortProtocolMap = new HashMap<>();

        // Parse the documents
        for (String eachLine : lines) {
            LOG.debug("Parsing line '{}'", eachLine);

            // Parse line
            List<String> columns = StreamSupport.stream(Arrays.spliterator(eachLine.split("[ |\t]")), false)
                .filter(column -> !Strings.isNullOrEmpty(column))
                .collect(Collectors.toList())
                .subList(0, 2);

            // If there is a comment in the last column, there was an empty service name
            // (e.g. 24/udp # any private mail system)
            // We skip those for now
            if (columns.get(columns.size()-1).equals("#")) {
                LOG.debug("Skipping line '{}' as it defines an empty service name", eachLine);
                continue;
            }

            // Parse
            final String service = columns.get(0);
            final int port = Integer.parseInt(columns.get(1).split("/")[0]);
            final String protocol = columns.get(1).split("/")[1];

            // Remember
            final Object[] key = new Object[]{service, port};
            servicePortProtocolMap.putIfAbsent(key, new ArrayList<>());
            servicePortProtocolMap.get(key).add(protocol);
        }

        // Convert to rules
        for (Map.Entry<Object[], List<String>> entry :  servicePortProtocolMap.entrySet()) {
            Rule rule = new RuleBuilder()
                    .withName((String) entry.getKey()[0])
                    .withPort((int) entry.getKey()[1])
                    .withProtocol(entry.getValue().stream().collect(Collectors.joining(",")))
                    .build();
            rules.add(rule);
        }
    }

    private static List<String> readFromInputStream(InputStream inputStream) throws IOException {
        Objects.requireNonNull(inputStream);

        final List<String> lines = new ArrayList<>();
        try (BufferedReader input = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = input.readLine()) != null) {
                if (line.startsWith("#")) continue; // omit comment lines
                lines.add(line);
            }
        }
        return lines;
    }

    @Override
    public List<Rule> getRules() {
        return Collections.unmodifiableList(rules);
    }
}
