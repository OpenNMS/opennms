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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.ClassificationRuleProvider;
import org.opennms.netmgt.flows.classification.internal.classifier.Classifier;
import org.opennms.netmgt.flows.classification.internal.classifier.CombinedClassifier;
import org.opennms.netmgt.flows.classification.internal.value.PortValue;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;
import org.opennms.netmgt.flows.classification.persistence.api.RuleComparator;

public class DefaultClassificationEngine implements ClassificationEngine {

    private final List<List<Classifier>> classifierPortList = new ArrayList<>();
    private final Comparator<Rule> ruleComparator = new RuleComparator();
    private final ClassificationRuleProvider ruleProvider;

    public DefaultClassificationEngine(ClassificationRuleProvider ruleProvider) {
        this.ruleProvider = Objects.requireNonNull(ruleProvider);
        this.reload();
    }

    @Override
    public void reload() {
        final List<Rule> rules = new ArrayList<>();
        rules.addAll(ruleProvider.getRules());
        createClassifiers(rules);
    }

    @Override
    public String classify(ClassificationRequest classificationRequest) {
        final List<Classifier> filteredClassifiers = getClassifiers(classificationRequest);
        final Optional<String> first = filteredClassifiers.stream()
                .map(classifier -> classifier.classify(classificationRequest))
                .filter(classifier -> classifier != null)
                .findFirst();

        // We return null instead of 'Undefined', to let the caller (e.g. rest service, or ui) decide
        // what an unmapped definition should be named.
        // This prevents a collision with an existing rule, which may map to 'Undefined'
        return first.orElse(null);
    }

    private void createClassifiers(final List<Rule> rules) {
        final List<List<Rule>> rulePortList = new ArrayList<>();

        // Initialize each element
        for (int i=0; i<65536; i++) {
            rulePortList.add(new ArrayList<>());
        }

        // Bind each rule to a port
        final List<Rule> anyPortRule = new ArrayList<>();
        for (Rule eachRule : rules) {
            if (eachRule.hasPortDefinition()) {
                for (Integer eachPort : new PortValue(eachRule.getPort()).getPorts()) {
                    rulePortList.get(eachPort).add(eachRule);
                }
            } else {
                anyPortRule.add(eachRule);
            }
        }

        // Bind classifiers to ANY port
        for (final List<Rule> theRules : rulePortList) {
            theRules.addAll(anyPortRule);
        }

        // Sort rules
        for (int i=0; i<rulePortList.size(); i++) {
            final List<Rule> portRules = rulePortList.get(i);
            Collections.sort(portRules, ruleComparator);
        }

        // Finally create classifiers
        classifierPortList.clear();
        for (int i=0; i<rulePortList.size(); i++) {
            final List<Rule> portRules = rulePortList.get(i);
            final List<Classifier> classifiers = portRules.stream().map(CombinedClassifier::new).collect(Collectors.toList());
            classifierPortList.set(i, classifiers);
        }
    }

    private List<Classifier> getClassifiers(ClassificationRequest request) {
        return classifierPortList.get(request.getPort());
    }
}
