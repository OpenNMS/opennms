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

package org.opennms.netmgt.flows.classification.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.ClassificationRuleProvider;
import org.opennms.netmgt.flows.classification.FilterService;
import org.opennms.netmgt.flows.classification.internal.classifier.Classifier;
import org.opennms.netmgt.flows.classification.internal.classifier.CombinedClassifier;
import org.opennms.netmgt.flows.classification.internal.value.PortValue;
import org.opennms.netmgt.flows.classification.persistence.api.DefaultRuleDefinition;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;
import org.opennms.netmgt.flows.classification.persistence.api.RuleDefinition;
import org.opennms.netmgt.flows.classification.persistence.api.RulePositionComparator;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;

public class DefaultClassificationEngine implements ClassificationEngine {

    // (port) -> rule mapping
    private final List<List<RuleDefinition>> rulePortList = new ArrayList<>(Rule.MAX_PORT_VALUE + 1);
    // (RuleDefinition) -> Combined Classifier
    private final Map<RuleDefinition, CombinedClassifier> ruleClassifierMap = new HashMap<>();
    // Cache to load the list of classifiers
    private final LoadingCache<Integer, List<Classifier>> portClassifiersCache;
    private final Comparator<RuleDefinition> ruleComparator = new RulePositionComparator();
    private final ClassificationRuleProvider ruleProvider;
    private final List<Rule> invalidRules = Lists.newArrayList();
    private final FilterService filterService;

    public DefaultClassificationEngine(final ClassificationRuleProvider ruleProvider, final FilterService filterService) {
        this(ruleProvider, filterService, true);
    }

    public DefaultClassificationEngine(final ClassificationRuleProvider ruleProvider, final FilterService filterService, final boolean initialize) {
        this.ruleProvider = Objects.requireNonNull(ruleProvider);
        this.filterService = Objects.requireNonNull(filterService);
        this.portClassifiersCache = CacheBuilder.newBuilder().build(new CacheLoader<Integer, List<Classifier>>() {
            @Override
            public List<Classifier> load(Integer port) throws Exception {
                final List<RuleDefinition> ruleDefinitions = rulePortList.get(port);
                final List<Classifier> classifiers = ruleDefinitions.stream().map(rule -> {
                    if (!ruleClassifierMap.containsKey(rule)) {
                        final DefaultRuleDefinition portRule = new DefaultRuleDefinition();
                        portRule.setName(rule.getName());
                        portRule.setProtocol(rule.getProtocol());
                        portRule.setSrcAddress(rule.getSrcAddress());
                        portRule.setDstAddress(rule.getDstAddress());
                        portRule.setExporterFilter(rule.getExporterFilter());
                        portRule.setGroupPosition(rule.getGroupPosition());
                        portRule.setPosition(rule.getPosition());

                        // If none, the value of either src or dst port may be empty, as the filtering already occurred
                        // through the index of the rule in the classifierPortList.
                        if (rule.hasDstPortDefinition()) {
                            portRule.setDstPort(rule.getDstPort());
                        }
                        if (rule.hasSrcPortDefinition()) {
                            portRule.setSrcPort(rule.getSrcPort());
                        }
                        ruleClassifierMap.put(rule, new CombinedClassifier(portRule, filterService));
                    }
                    return ruleClassifierMap.get(rule);
                }).collect(Collectors.toList());
                return classifiers;
            }
        });

        if (initialize) {
            this.reload();
        }
    }

    private static RuleDefinition reverseRule(final RuleDefinition rule) {
        final DefaultRuleDefinition result = new DefaultRuleDefinition();
        result.setName(rule.getName());
        result.setDstAddress(rule.getSrcAddress());
        result.setDstPort(rule.getSrcPort());
        result.setSrcAddress(rule.getDstAddress());
        result.setSrcPort(rule.getDstPort());
        result.setProtocol(rule.getProtocol());
        result.setExporterFilter(rule.getExporterFilter());
        result.setGroupPosition(rule.getGroupPosition());
        return result;
    }

    @Override
    public void reload() {
        // Reset existing data
        ruleClassifierMap.clear();
        rulePortList.clear();
        invalidRules.clear();
        portClassifiersCache.invalidateAll();

        // Load all rules and validate them
        final List<Rule> validRules = Lists.newArrayList();
        ruleProvider.getRules().forEach(rule -> {
            try {
                new CombinedClassifier(rule, filterService);
                validRules.add(rule);
            } catch (Exception ex) {
                LoggerFactory.getLogger(getClass()).error("Rule {} is not valid. Ignoring rule.", rule, ex);
                invalidRules.add(rule);
            }
        });

        // Expand omnidirectional rules to reversed ones
        final List<RuleDefinition> rules = validRules.stream()
                .flatMap(rule -> rule.isOmnidirectional() && (rule.hasSrcPortDefinition() || rule.hasSrcAddressDefinition() || rule.hasDstPortDefinition() || rule.hasDstAddressDefinition())
                        ? Stream.of(rule, reverseRule(rule))
                        : Stream.of(rule))
                .collect(Collectors.toList());

        // Rules which are not bound to a src OR dst port are stored here temporarily
        final List<RuleDefinition> anyPortRules = new ArrayList<>();

        // Initialize each element
        for (int i=Rule.MIN_PORT_VALUE; i<=Rule.MAX_PORT_VALUE; i++) {
            rulePortList.add(new ArrayList<>());
        }

        // Technically there are 2^16 * 2^16 combinations, but it is possible to correctly pre-sort the rules
        // by sorting them to all possible ports (2^16 possibilities).
        // In case only src OR dst port is defined, the rule is sorted in the according port.
        // In case src AND dst port are defined, the rule is only sorted by dst port.
        // In case neither src NOR dst port are defined, the rule is applied to ALL ports.
        for (RuleDefinition eachRule : rules) {
            // src AND dst port are defined, only map rule to dst port
            if (eachRule.hasSrcPortDefinition() && eachRule.hasDstPortDefinition()) {
                for (Integer eachPort : new PortValue(eachRule.getDstPort()).getPorts()) {
                    final List<RuleDefinition> portRules = rulePortList.get(eachPort);
                    if (!portRules.contains(eachRule)) {
                        portRules.add(eachRule);
                    }
                }
            } else if (eachRule.hasSrcPortDefinition() || eachRule.hasDstPortDefinition()) {
                // either src or dst is defined
                final PortValue portValue = new PortValue(eachRule.hasDstPortDefinition() ? eachRule.getDstPort() : eachRule.getSrcPort());
                for (Integer eachPort : portValue.getPorts()) {
                    rulePortList.get(eachPort).add(eachRule);
                }
            } else if (!eachRule.hasDstPortDefinition() && !eachRule.hasSrcPortDefinition()) {
                // Special treatment for rules which don't define a src or dst port, which are added ONCE
                // to all ports
                anyPortRules.add(eachRule);
            }
        }

        // Add rules with no port mapping to ALL ports, if not already added
        for (final List<RuleDefinition> theRules : rulePortList) {
            theRules.addAll(anyPortRules);
        }

        // Reduce memory pressure
        anyPortRules.clear();
        rules.clear();

        // Sort rules by position
        for (int i=0; i<rulePortList.size(); i++) {
            final List<RuleDefinition> portRules = rulePortList.get(i);
            portRules.sort(ruleComparator);
        }

        // pre-populate cache
        for (int i=0; i<rulePortList.size(); i++) {
            getClassifiers(i);
        }
    }

    @Override
    public List<Rule> getInvalidRules() {
        return Collections.unmodifiableList(invalidRules);
    }

    @Override
    public String classify(ClassificationRequest classificationRequest) {
        final Collection<Classifier> filteredClassifiers = getClassifiers(classificationRequest);
        final Optional<String> first = filteredClassifiers.stream()
                .map(classifier -> classifier.classify(classificationRequest))
                .filter(classifier -> classifier != null)
                .findFirst();

        // We return null instead of 'Undefined', to let the caller (e.g. rest service, or ui) decide
        // what an unmapped definition should be named.
        // This prevents a collision with an existing rule, which may map to 'Undefined'
        return first.orElse(null);
    }

    private Collection<Classifier> getClassifiers(ClassificationRequest request) {
        final List<Classifier> srcPortClassifiers = getClassifiers(request.getSrcPort());
        final List<Classifier> dstPortClassifiers = getClassifiers(request.getDstPort());

        // If rules for either src or dst ports are empty, use the opposite
        if (srcPortClassifiers.isEmpty()) {
            return dstPortClassifiers;
        }
        if (dstPortClassifiers.isEmpty()) {
            return srcPortClassifiers;
        }

        // If both are equal, nothing to do, just use either srcPortClassifiers or dstPortClassifiers
        if (Objects.equals(srcPortClassifiers, dstPortClassifiers)) {
            return dstPortClassifiers;
        }

        // If rules for src and dst port exist and they are not identical,
        // they must be deduped (merging and sorting the classifiers)
        // 1. Merge
        final List<Classifier> classifiers = new ArrayList<>();
        classifiers.addAll(srcPortClassifiers);

        // Only add not already added classifiers
        for (Classifier c : dstPortClassifiers) {
            if (!classifiers.contains(c)) {
                classifiers.add(c);
            }
        }

        // 2. Sort
        Collections.sort(classifiers);
        return classifiers;
    }

    private List<Classifier> getClassifiers(int port) {
        try {
            return portClassifiersCache.get(port);
        } catch (ExecutionException ex) {
            throw Throwables.propagate(ex.getCause());
        }
    }
}
