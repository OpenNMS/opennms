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
