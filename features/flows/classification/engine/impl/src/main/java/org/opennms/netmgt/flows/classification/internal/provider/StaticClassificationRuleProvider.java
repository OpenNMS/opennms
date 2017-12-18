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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.opennms.netmgt.flows.classification.ClassificationRuleProvider;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;
import org.opennms.netmgt.flows.classification.internal.value.RangedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Provides all rules defined in https://www.iana.org/assignments/service-names-port-numbers/service-names-port-numbers.txt
// NOTE: Empty rules (e.g. 24/udp # any private mail system) are ignored.
public class StaticClassificationRuleProvider implements ClassificationRuleProvider {

    private static final Logger LOG = LoggerFactory.getLogger(StaticClassificationRuleProvider.class);

    private final List<Rule> rules;

    public StaticClassificationRuleProvider() throws IOException {
        this.rules = new CsvRuleParserImpl().parse(getClass().getResourceAsStream("/services.csv"));
    }

    @Override
    public List<Rule> getRules() {
        return Collections.unmodifiableList(rules);
    }

    protected Rule getRule(String serviceName, int port) {
        final List<Rule> matchingRules = rules.stream()
                .filter(rule -> rule.getName().equalsIgnoreCase(serviceName) && new RangedValue(rule.getPort()).isInRange(port))
                .collect(Collectors.toList());
        if (matchingRules.size() > 1) {
            LOG.warn("Found more than 1 element. Returning 1st element in list");
            return matchingRules.get(0);
        }
        if (!matchingRules.isEmpty()) {
            return matchingRules.get(0);
        }
        // Not found
        return null;
    }
}
