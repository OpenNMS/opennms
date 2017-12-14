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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.opennms.netmgt.flows.classification.classifier.Classifier;
import org.opennms.netmgt.flows.classification.persistence.ClassificationRuleDAO;
import org.opennms.netmgt.flows.classification.persistence.Rule;

public class DefaultClassificationEngine implements ClassificationEngine {

    private final ClassificationRuleDAO ruleDAO;

    private final List<Classifier> classifierList = new ArrayList<>();

    // This can be a DAO or similar in the future
    public DefaultClassificationEngine(ClassificationRuleDAO ruleDAO) {
        this.ruleDAO = Objects.requireNonNull(ruleDAO);
        this.reload();
    }

    protected DefaultClassificationEngine(Supplier<List<Rule>> supplier) {
        this((ClassificationRuleDAO) () -> supplier.get());
    }

    @Override
    public void reload() {
        classifierList.clear();
        ruleDAO.findAll().forEach(rule -> {
            Classifier classifier = rule.toClassifier();
            if (!classifierList.contains(classifier)) {
                classifierList.add(classifier);
            }
        });
        Collections.sort(classifierList, new ClassificationSorter());
    }

    @Override
    public String classify(ClassificationRequest classificationRequest) {
        final Optional<String> first = classifierList.stream()
                .map(classifier -> classifier.classify(classificationRequest))
                .filter(classifier -> classifier != null)
                .findFirst();

        // We return null instead of 'Undefined', to let the caller (e.g. rest service, or ui) decide
        // what an unmapped definition should be named.
        // This prevents a collision with an existing rule, which may map to 'Undefined'
        return first.orElse(null);
    }

}
