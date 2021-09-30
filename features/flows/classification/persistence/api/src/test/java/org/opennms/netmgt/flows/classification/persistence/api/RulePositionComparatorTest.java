/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.classification.persistence.api;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

public class RulePositionComparatorTest {

    @Test
    public void verifyRulePositionComparator() {

        final Group group1 = new GroupBuilder().withName("group1").withPosition(1).build();
        final Rule rule1_1 = createAndAddRule(group1, 1);
        final Rule rule1_2 = createAndAddRule(group1, 2);
        final Rule rule1_3 = createAndAddRule(group1, 3);

        final Group group2 = new GroupBuilder().withName("group2").withPosition(2).build();
        final Rule rule2_1 = createAndAddRule(group2, 1);
        final Rule rule2_2 = createAndAddRule(group2, 2);
        final Rule rule2_3 = createAndAddRule(group2, 3);

        final List<Rule> expectedList = Lists.newArrayList(rule1_1, rule1_2, rule1_3, rule2_1, rule2_2, rule2_3);
        final List<Rule> actualRules = Lists.newArrayList(rule2_2, rule1_3, rule2_1, rule2_3, rule1_2, rule1_1);
        actualRules.sort(new RulePositionComparator());

        assertEquals(expectedList, actualRules);
    }

    private Rule createAndAddRule(Group group, int position) {
        Rule rule = new RuleBuilder()
                .withPosition(position)
                .withName(String.format("rule%s_%s", group.getPosition(), position))
                .build();
        group.addRule(rule);
        return rule;
    }
}
