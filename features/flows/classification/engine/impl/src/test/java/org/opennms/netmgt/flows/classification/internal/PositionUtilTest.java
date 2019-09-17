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

package org.opennms.netmgt.flows.classification.internal;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.opennms.netmgt.flows.classification.persistence.api.Group;
import org.opennms.netmgt.flows.classification.persistence.api.GroupBuilder;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;
import org.opennms.netmgt.flows.classification.persistence.api.RuleBuilder;

public class PositionUtilTest {

    private List<Rule> rules = new ArrayList<>();
    private Group group = new Group();
    private List<Group> groups = new ArrayList<>();
    private int idCounter;

    @Test
    public void rulesShouldBeSortedByTheirPosition () {
        Rule rule2 = createAndAddRule("rule2", 2);
        Rule rule1 = createAndAddRule("rule1", 1);
        Rule rule3 = createAndAddRule("rule3", 3);
        Rule rule4 = createAndAddRule("rule4", 4);
        Rule ourRule = createAndAddRule("ourRule", 3); // should be moved to position 3
        group.setRules(rules);
        assertEquals(Arrays.asList(rule1, rule2, ourRule, rule3, rule4), PositionUtil.sortRulePositions(ourRule));
    }

    @Test
    public void groupsShouldBeSortedByTheirPosition () {
        Group group2 = createAndAddGroup(2);
        Group group1 = createAndAddGroup(1);
        Group predefinedGroup = new GroupBuilder().withPosition(0).withName("pre-defined").withReadOnly(true).build(); // should always go last
        groups.add(predefinedGroup);
        Group group3 = createAndAddGroup(3);
        Group group4 = createAndAddGroup(4);
        Group ourGroup = createAndAddGroup(3, "ourGroup"); // should be moved to position 3
        assertEquals(Arrays.asList(group1, group2, ourGroup, group3, group4, predefinedGroup), PositionUtil.sortGroupPositions(ourGroup, groups));
    }

    private Rule createAndAddRule(String name, int position) {
        Rule rule = new RuleBuilder()
                .withName(name)
                .withPosition(position)
                .build();
        rule.setId(idCounter++);
        rules.add(rule);
        group.addRule(rule);
        return rule;
    }

    private Group createAndAddGroup(int position, String name) {
        Group group = new GroupBuilder()
                .withName(name)
                .withPosition(position)
                .build();
        group.setId(idCounter++);
        groups.add(group);
        return group;
    }

    private Group createAndAddGroup(int position) {
        return createAndAddGroup(position, "group"+position);
    }
}
