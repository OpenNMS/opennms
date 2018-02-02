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

package org.opennms.netmgt.flows.classification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.opennms.netmgt.flows.classification.classifier.Classifier;
import org.opennms.netmgt.flows.classification.classifier.CombinedClassifier;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;
import org.opennms.netmgt.flows.classification.provider.ClassificationRuleProvider;
import org.opennms.netmgt.flows.classification.provider.StaticClassificationRuleProvider;
import org.opennms.netmgt.flows.classification.value.PortValue;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class DefaultClassificationEngine implements ClassificationEngine {

    private final ClassificationRuleProvider ruleProvider;
    private final Map<Integer, List<Classifier>> classifierPortMap = new HashMap<>();
    private final boolean useStaticRules;

    // This can be a DAO or similar in the future
    public DefaultClassificationEngine(ClassificationRuleProvider ruleProvider) {
        this(ruleProvider, false);
    }

    public DefaultClassificationEngine(ClassificationRuleProvider ruleProvider, boolean useStaticRules) {
        this.ruleProvider = Objects.requireNonNull(ruleProvider);
        this.useStaticRules = useStaticRules;
        this.reload();
    }

    @Override
    public void reload() {
        final List<Rule> rules = new ArrayList<>();
        rules.addAll(ruleProvider.getRules());

        // For now we just apply static rules at the end.
        // This ensures that user-defined rules are loaded first
        if (useStaticRules) {
            try {
                rules.addAll(new StaticClassificationRuleProvider().getRules());
            } catch (IOException e) {
                LoggerFactory.getLogger(getClass()).error("Could not load static rules {}", e.getMessage(), e);
            }

        }
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
        classifierPortMap.clear();
        classifierPortMap.put(null, new ArrayList<>()); // Values for ANY port

        // Create classifiers and bind them to a port
        for (Rule eachRule : rules) {
            final Classifier classifier = new CombinedClassifier(eachRule);
            if (!Strings.isNullOrEmpty(eachRule.getPort())) {
                for (Integer eachPort : new PortValue(eachRule.getPort()).getPorts()) {
                    classifierPortMap.putIfAbsent(eachPort, new ArrayList<>());
                    classifierPortMap.get(eachPort).add(classifier);
                }
            } else {
                classifierPortMap.get(null).add(classifier);
            }
        }
    }

    private List<Classifier> getClassifiers(ClassificationRequest request) {
        // Get classifiers for the given port
        final List<Classifier> classifiers = classifierPortMap.getOrDefault(request.getPort(), new ArrayList<>());
        // Add all classifiers, which match to ANY port
        classifiers.addAll(classifierPortMap.getOrDefault(null, new ArrayList<>()));
        if (classifiers.size() > 1) {
            Collections.sort(classifiers, new ClassificationSorter());
        }
        return classifiers;
    }
}
