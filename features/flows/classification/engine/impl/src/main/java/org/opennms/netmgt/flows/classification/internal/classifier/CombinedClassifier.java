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

package org.opennms.netmgt.flows.classification.internal.classifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.FilterService;
import org.opennms.netmgt.flows.classification.internal.matcher.DstAddressMatcher;
import org.opennms.netmgt.flows.classification.internal.matcher.DstPortMatcher;
import org.opennms.netmgt.flows.classification.internal.matcher.FilterMatcher;
import org.opennms.netmgt.flows.classification.internal.matcher.Matcher;
import org.opennms.netmgt.flows.classification.internal.matcher.ProtocolMatcher;
import org.opennms.netmgt.flows.classification.internal.matcher.SrcAddressMatcher;
import org.opennms.netmgt.flows.classification.internal.matcher.SrcPortMatcher;
import org.opennms.netmgt.flows.classification.persistence.api.RulePriorityComparator;
import org.opennms.netmgt.flows.classification.persistence.api.RuleDefinition;

public class CombinedClassifier implements Classifier {

    private final List<Matcher> matchers;
    private final RuleDefinition ruleDefinition;
    private final RulePriorityComparator rulePriorityComparator = new RulePriorityComparator();

    public CombinedClassifier(RuleDefinition ruleDefinition, FilterService filterService) {
        Objects.requireNonNull(ruleDefinition);
        final List<Matcher> matchers = new ArrayList<>();
        if (ruleDefinition.hasProtocolDefinition()) {
            matchers.add(new ProtocolMatcher(ruleDefinition.getProtocol()));
        }
        if (ruleDefinition.hasSrcPortDefinition()) {
            matchers.add(new SrcPortMatcher(ruleDefinition.getSrcPort()));
        }
        if (ruleDefinition.hasSrcAddressDefinition()) {
            matchers.add(new SrcAddressMatcher(ruleDefinition.getSrcAddress()));
        }
        if (ruleDefinition.hasDstAddressDefinition()) {
            matchers.add(new DstAddressMatcher(ruleDefinition.getDstAddress()));
        }
        if (ruleDefinition.hasDstPortDefinition()) {
            matchers.add(new DstPortMatcher(ruleDefinition.getDstPort()));
        }
        if (ruleDefinition.hasExportFilterDefinition()) {
            matchers.add(new FilterMatcher(ruleDefinition.getExporterFilter(), filterService));
        }
        this.ruleDefinition = ruleDefinition;
        this.matchers = matchers;
    }

    @Override
    public String classify(ClassificationRequest request) {
        boolean matches = true;
        for (Matcher m : matchers) {
            matches = matches && m.matches(request);
            if (!matches) {
                return null;
            }
        }
        return ruleDefinition.getName();
    }

    @Override
    public int compareTo(Classifier o) {
        return rulePriorityComparator.compare(this.ruleDefinition, ((CombinedClassifier) o).ruleDefinition);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final CombinedClassifier that = (CombinedClassifier) o;
        return Objects.equals(ruleDefinition, that.ruleDefinition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ruleDefinition);
    }
}
