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
        actualRules.sort(RulePositionComparator.INSTANCE);

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
