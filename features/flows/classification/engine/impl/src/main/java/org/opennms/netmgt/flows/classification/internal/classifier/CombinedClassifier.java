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
import org.opennms.netmgt.flows.classification.internal.matcher.DstAddressMatcher;
import org.opennms.netmgt.flows.classification.internal.matcher.DstPortMatcher;
import org.opennms.netmgt.flows.classification.internal.matcher.Matcher;
import org.opennms.netmgt.flows.classification.internal.matcher.ProtocolMatcher;
import org.opennms.netmgt.flows.classification.internal.matcher.SrcAddressMatcher;
import org.opennms.netmgt.flows.classification.internal.matcher.SrcPortMatcher;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;
import org.opennms.netmgt.flows.classification.persistence.api.RuleComparator;

public class CombinedClassifier implements Classifier {

    private final List<Matcher> matchers;
    private final String application;
    private final Rule rule;
    private final RuleComparator ruleComparator = new RuleComparator();

    public CombinedClassifier(Rule rule) {
        Objects.requireNonNull(rule);
        final List<Matcher> matchers = new ArrayList<>();
        if (rule.hasProtocolDefinition()) {
            matchers.add(new ProtocolMatcher(rule.getProtocol()));
        }
        if (rule.hasSrcPortDefinition()) {
            matchers.add(new SrcPortMatcher(rule.getSrcPort()));
        }
        if (rule.hasSrcAddressDefinition()) {
            matchers.add(new SrcAddressMatcher(rule.getSrcAddress()));
        }
        if (rule.hasDstAddressDefinition()) {
            matchers.add(new DstAddressMatcher(rule.getDstAddress()));
        }
        if (rule.hasDstPortDefinition()) {
            matchers.add(new DstPortMatcher(rule.getDstPort()));
        }
        this.rule = rule;
        this.application = rule.getName();
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
        return application;
    }

    @Override
    public int compareTo(Classifier o) {
        return ruleComparator.compare(this.rule, ((CombinedClassifier) o).rule);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final CombinedClassifier that = (CombinedClassifier) o;
        return Objects.equals(rule.getName(), that.rule.getName())
                && Objects.equals(rule.getDstAddress(), that.rule.getDstAddress())
                && Objects.equals(rule.getDstPort(), that.rule.getDstPort())
                && Objects.equals(rule.getSrcAddress(), that.rule.getSrcAddress())
                && Objects.equals(rule.getSrcPort(), that.rule.getSrcPort())
                && Objects.equals(rule.getProtocol(), that.rule.getProtocol())
                && Objects.equals(rule.getGroup(), that.rule.getGroup())
                && Objects.equals(rule.getPosition(), that.rule.getPosition());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                rule.getName(), rule.getDstAddress(), rule.getDstPort(), rule.getSrcAddress(),
                rule.getSrcPort(), rule.getProtocol(), rule.getGroup(), rule.getPosition()
        );
    }
}
