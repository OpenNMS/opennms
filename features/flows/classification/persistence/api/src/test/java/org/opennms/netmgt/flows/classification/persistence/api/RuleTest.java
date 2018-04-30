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

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

public class RuleTest {

    @Test
    public void verifyCalculatePriority() {
        final Rule leastConcreteRule = new RuleBuilder().withName("dummy").build();
        final Rule mostConcreteRule = new RuleBuilder()
                .withName("opennms-monitor")
                .withProtocol("tcp")
                .withDstAddress("10.0.0.1").withDstPort("8980")
                .withSrcAddress("10.0.0.2").withSrcPort(5231)
                .withExporterFilter("categoryName='Test'").build();
        final Rule tcpRule = new RuleBuilder().withName("tcpTraffic").withProtocol("tcp").build();
        final Rule httpRule = new RuleBuilder().withName("http").withProtocol("tcp,udp").withDstPort("80,8980,8080").build();
        final Rule portRule = new RuleBuilder().withName("443-traffic").withDstPort("443").build();

        assertEquals(0, leastConcreteRule.calculatePriority());
        assertEquals(1, tcpRule.calculatePriority());
        assertEquals(3, portRule.calculatePriority());
        assertEquals(4, httpRule.calculatePriority());
        assertEquals(26, mostConcreteRule.calculatePriority());
    }

    @Test
    public void verifyRuleComparator() {
        final Rule leastConcreteRule = new RuleBuilder().withName("dummy").build();
        final Rule mostConcreteRule = new RuleBuilder().withName("opennms-monitor")
                .withProtocol("tcp")
                .withDstAddress("10.0.0.1").withDstPort("8980")
                .withSrcAddress("10.0.0.2").withSrcPort(5231)
                .withExporterFilter("categoryName='Test'").build();
        final Rule tcpRule = new RuleBuilder().withName("tcpTraffic").withProtocol("tcp").build();
        final Rule httpRule = new RuleBuilder().withName("http").withProtocol("tcp,udp").withDstPort("80,8980,8080").build();
        final Rule portRule = new RuleBuilder().withName("443-traffic").withDstPort("443").build();
        final Rule srcRule = new RuleBuilder().withName("src-traffic").withSrcAddress("10.0.0.1").withSrcPort("5578").build();

        final List<Rule> expectedList = Lists.newArrayList(mostConcreteRule, srcRule /* src address and src port is > only ony dst port */, httpRule, portRule, tcpRule, leastConcreteRule);
        final List<Rule> actualRules = Lists.newArrayList(leastConcreteRule, mostConcreteRule, tcpRule, httpRule, portRule, srcRule);
        Collections.sort(actualRules, new RulePriorityComparator());

        assertEquals(actualRules, expectedList);
    }
}