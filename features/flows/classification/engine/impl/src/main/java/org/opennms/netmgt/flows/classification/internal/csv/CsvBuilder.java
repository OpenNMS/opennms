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
