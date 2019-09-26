/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.classification.internal.csv;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.opennms.netmgt.flows.classification.persistence.api.Rule;
import org.opennms.netmgt.flows.classification.persistence.api.RuleBuilder;

public class CsvBuilder {
    private final List<Rule> rules = new ArrayList<>();
    private boolean includeHeader = true;

    public CsvBuilder withHeader(boolean includeHeader) {
        this.includeHeader = includeHeader;
        return this;
    }

    public CsvBuilder withRule(RuleBuilder ruleBuilder) {
        Objects.requireNonNull(ruleBuilder);
        this.withRule(ruleBuilder.build());
        return this;
    }

    public CsvBuilder withRule(Rule rule) {
        Objects.requireNonNull(rule);
        this.rules.add(rule);
        return this;
    }

    public CsvBuilder withRules(List<Rule> rules) {
        Objects.requireNonNull(rules);
        this.rules.addAll(rules);
        return this;
    }

    public String build() {
        final StringBuilder csv = new StringBuilder();
        if (includeHeader) {
            csv.append(CsvServiceImpl.HEADERS_STRING);
        }
        final String rowFormat = IntStream.range(0, CsvServiceImpl.HEADERS.length)
                .mapToObj(i -> "%s")
                .collect(Collectors.joining(";"));
        csv.append(
                rules.stream()
                        .map(rule ->
                                String.format(rowFormat,
                                        rule.getName() == null ? "" : rule.getName(),
                                        rule.getProtocol() == null ? "" : rule.getProtocol(),
                                        rule.getSrcAddress() == null ? "" : rule.getSrcAddress(),
                                        rule.getSrcPort() == null ? "" : rule.getSrcPort(),
                                        rule.getDstAddress() == null ? "" : rule.getDstAddress(),
                                        rule.getDstPort() == null ? "" : rule.getDstPort(),
                                        rule.getExporterFilter() == null ? "" : rule.getExporterFilter(),
                                        rule.isOmnidirectional() ? "true" : "false"
                                )
                        )
                        .collect(Collectors.joining("\n")));
        return csv.toString();
    }
}
