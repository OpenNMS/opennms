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

package org.opennms.netmgt.flows.classification.persistence.impl;

import java.util.List;
import java.util.Objects;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.flows.classification.persistence.api.ClassificationRuleDao;
import org.opennms.netmgt.flows.classification.persistence.api.Group;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;

public class ClassificationRuleDaoImpl extends AbstractDaoHibernate<Rule, Integer> implements ClassificationRuleDao {

    public ClassificationRuleDaoImpl() {
        super(Rule.class);
    }

    @Override
    public List<Rule> findAllEnabledRules() {
        return findMatching(
                new CriteriaBuilder(Rule.class)
                    .alias("group", "group")
                    .eq("group.enabled", true)
                    .toCriteria());
    }

    @Override
    public Rule findByDefinition(Rule rule, Group group) {
        Objects.requireNonNull(rule);
        Objects.requireNonNull(group);
        CriteriaBuilder criteriaBuilder = createCriteriaBuilderDefinition(rule);
        criteriaBuilder.alias("group", "group");
        criteriaBuilder.eq("group.id", group.getId());
        // Exclude me, otherwise it will always at least return the rule
        if (rule.getId() != null) {
            criteriaBuilder.not().eq("id", rule.getId());
        }
        final List<Rule> matchingRules = findMatching(criteriaBuilder.toCriteria());
        return matchingRules.isEmpty() ? null : matchingRules.get(0);
    }

    @Override
    public List<Rule> findByDefinition(Rule rule) {
        Objects.requireNonNull(rule);
        final CriteriaBuilder builder = createCriteriaBuilderDefinition(rule);
        final List<Rule> matchingRules = findMatching(builder.toCriteria());
        return matchingRules;
    }

    private static CriteriaBuilder createCriteriaBuilderDefinition(Rule rule) {
        final CriteriaBuilder builder = new CriteriaBuilder(Rule.class);

        // DST
        if (rule.hasDstAddressDefinition()) {
            builder.ilike("dstAddress", rule.getDstAddress());
        } else {
            builder.isNull("dstAddress");
        }
        if (rule.hasDstPortDefinition()) {
            builder.ilike("dstPort", rule.getDstPort());
        } else {
            builder.isNull("dstPort");
        }

        // SOURCE
        if (rule.hasSrcAddressDefinition()) {
            builder.ilike("srcAddress", rule.getSrcAddress());
        } else {
            builder.isNull("srcAddress");
        }
        if (rule.hasSrcPortDefinition()) {
            builder.ilike("srcPort", rule.getSrcPort());
        } else {
            builder.isNull("srcPort");
        }

        // COMMON
        if (rule.hasProtocolDefinition()) {
            builder.ilike("protocol", rule.getProtocol());
        } else {
            builder.isNull("protocol");
        }
        if (rule.hasExportFilterDefinition()) {
            builder.ilike("exporterFilter", rule.getExporterFilter());
        } else {
            builder.isNull("exporterFilter");
        }
        return builder;
    }

}
