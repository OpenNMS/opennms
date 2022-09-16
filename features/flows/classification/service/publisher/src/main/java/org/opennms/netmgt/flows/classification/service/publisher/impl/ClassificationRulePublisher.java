/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.classification.service.publisher.impl;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.core.ipc.twin.api.TwinPublisher;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.FilterWatcher;
import org.opennms.netmgt.flows.classification.dto.RuleDTO;
import org.opennms.netmgt.flows.classification.persistence.api.Protocol;
import org.opennms.netmgt.flows.classification.persistence.api.Protocols;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;
import org.opennms.netmgt.flows.classification.persistence.api.RuleDefinition;
import org.opennms.netmgt.flows.classification.service.ClassificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class ClassificationRulePublisher implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(ClassificationRulePublisher.class);

    private final TwinPublisher.Session<List<RuleDTO>> publisher;

    private final Closeable ruleWatcher;

    private final FilterWatcher filterWatcher;

    private FilterWatcher.Session filterWatcherSession;

    public ClassificationRulePublisher(final ClassificationService classificationService,
                                       final FilterWatcher filterWatcher,
                                       final TwinPublisher twinPublisher) throws IOException {
        this.filterWatcher = Objects.requireNonNull(filterWatcher);

        this.publisher = twinPublisher.register(RuleDTO.TWIN_KEY, RuleDTO.TWIN_TYPE, null);

        this.ruleWatcher = classificationService.listen(this::rulesChanged);
    }

    private void rulesChanged(final List<Rule> rules) {
        if (this.filterWatcherSession != null) {
            this.filterWatcherSession.close();
        }

        // Register watcher for all rules with exporter filters
        final var filters = rules.stream()
                                 .map(Rule::getExporterFilter)
                                 .filter(Predicate.not(Strings::isNullOrEmpty))
                                 .collect(Collectors.toSet());

        this.filterWatcherSession = this.filterWatcher.watch(filters,
                                                             (results -> filtersChanged(rules, results)));
    }

    private void filtersChanged(final List<Rule> rules,
                                       final FilterWatcher.FilterResults results) {
        // TODO fooker: Sort the list and make the position inherent?

        final var x = rules.stream()
                           .sorted(Comparator.comparingInt(Rule::getGroupPosition)
                                             .thenComparingInt(Rule::getPosition))
                           .flatMap(rule -> {
                               if (rule.isOmnidirectional()) {
                                   return Stream.of(resolveRule(rule, results), resolveRule(rule.reversedRule(), results));
                               } else {
                                   return Stream.of(resolveRule(rule, results));
                               }
                           })
                           .collect(Collectors.toList());

        try {
            this.publisher.publish(x);
        } catch (final IOException e) {
            LOG.error("Publishing classification rules failed", e);
        }
    }

    private static RuleDTO resolveRule(final RuleDefinition rule,
                                       final FilterWatcher.FilterResults results) {
        // TODO fooker: error handling of protocols

        final List<String> exporters = Strings.isNullOrEmpty(rule.getExporterFilter())
                                       ? List.of()
                                       : results.getRuleNodeIpServiceMap().getOrDefault(rule.getExporterFilter(), Collections.emptyMap())
                                                .values().stream()
                                                .flatMap(node -> node.keySet().stream())
                                                .map(InetAddressUtils::str)
                                                .collect(Collectors.toList());

        return RuleDTO.builder()
                      .withName(rule.getName())
                      .withProtocols(Strings.isNullOrEmpty(rule.getProtocol())
                                     ? List.of()
                                     : Arrays.stream(rule.getProtocol().split(","))
                                             .map(Protocols::getProtocol)
                                             .map(Protocol::getDecimal)
                                             .collect(Collectors.toList()))
                      .withSrcPort(rule.getSrcPort())
                      .withSrcAddress(rule.getSrcAddress())
                      .withDstPort(rule.getDstPort())
                      .withDstAddress(rule.getDstAddress())
                      .withPosition(rule.getGroupPosition() << 16 | rule.getPosition())
                      .withExporters(exporters)
                      .build();
    }

    @Override
    public void close() throws IOException {
        if (this.filterWatcherSession != null) {
            this.filterWatcherSession.close();
        }

        if (this.ruleWatcher != null) {
            this.ruleWatcher.close();
        }

        if (this.publisher != null) {
            this.publisher.close();
        }
    }
}
