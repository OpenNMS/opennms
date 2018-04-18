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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.flows.classification.persistence.api.ClassificationGroupDao;
import org.opennms.netmgt.flows.classification.persistence.api.ClassificationRuleDao;
import org.opennms.netmgt.flows.classification.persistence.api.Group;
import org.opennms.netmgt.flows.classification.persistence.api.GroupBuilder;
import org.opennms.netmgt.flows.classification.persistence.api.Groups;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;
import org.opennms.netmgt.flows.classification.persistence.api.RuleBuilder;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml"})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
public class ClassificationRuleDaoIT {

    @Autowired
    private ClassificationRuleDao ruleDao;

    @Autowired
    private ClassificationGroupDao groupDao;

    private Group customGroup;

    private Group staticGroup;

    @Before
    public void before() {
        this.customGroup = new GroupBuilder().withName(Groups.USER_DEFINED).build();
        this.staticGroup = new GroupBuilder().withName(Groups.SYSTEM_DEFINED).build();
        groupDao.save(customGroup);
        groupDao.save(staticGroup);
    }

    @After
    public void after() {
        groupDao.findAll().forEach(group -> groupDao.delete(group));
    }

    @Test
    public void verifyCRUD() {
        // Nothing created yet
        assertEquals(0, ruleDao.countAll());

        // Create dummy
        final Rule rule = new RuleBuilder()
                .withName("HTTP")
                .withDstAddress("127.0.0.1")
                .withDstPort("80,8080")
                .withProtocol("tcp")
                .withGroup(customGroup)
                .build();

        // create and verify creation
        ruleDao.saveOrUpdate(rule);
        assertEquals(1, ruleDao.countAll());

        // Update
        rule.setName("HTTP2");
        ruleDao.update(rule);
        assertEquals(1, ruleDao.countAll());
        assertEquals("HTTP2", ruleDao.get(rule.getId()).getName());
        assertEquals(Groups.USER_DEFINED, ruleDao.get(rule.getId()).getGroup().getName());

        // Delete
        rule.getGroup().removeRule(rule);
        ruleDao.delete(rule);
        assertEquals(0, ruleDao.countAll());
    }

    @Test
    public void verifyFetchingOnlyEnabledRules() {
        // Create a bunch of rules
        ruleDao.save(new RuleBuilder().withName("Rule 1").withDstPort(1000).withGroup(staticGroup).build());
        ruleDao.save(new RuleBuilder().withName("Rule 2").withDstPort(1000).withGroup(customGroup).build());

        // Verify creation
        assertThat(ruleDao.findAllEnabledRules(), hasSize(2));

        // Disable group
        staticGroup.setEnabled(false);
        groupDao.saveOrUpdate(staticGroup);
        assertThat(ruleDao.findAllEnabledRules(), hasSize(1));

        // Disable yet another group
        customGroup.setEnabled(false);
        groupDao.saveOrUpdate(customGroup);
        assertThat(ruleDao.findAllEnabledRules(), hasSize(0));
    }

    // Does not test ALL combinations, but some
    @Test
    public void verifyFindByDefinition() {
        final Rule fullyDefinedRule = new RuleBuilder()
                .withName("HTTP")
                .withSrcAddress("10.0.0.1")
                .withSrcPort("55555")
                .withDstAddress("127.0.0.1")
                .withDstPort("80,8080")
                .withProtocol("tcp")
                .withExporterFilter("some-filter-value")
                .withGroup(customGroup)
                .build();
        ruleDao.save(fullyDefinedRule);

        // Create dummy rule
        final Rule tmpRule = new Rule();
        tmpRule.setName("dummy"); // name does not matter
        assertThat(ruleDao.findByDefinition(tmpRule), hasSize(0));

        // define src address
        tmpRule.setSrcAddress(fullyDefinedRule.getSrcAddress());
        assertThat(ruleDao.findByDefinition(tmpRule), hasSize(0));

        // define src port
        tmpRule.setSrcPort(fullyDefinedRule.getSrcPort());
        assertThat(ruleDao.findByDefinition(tmpRule), hasSize(0));

        // Define dst address
        tmpRule.setDstAddress(fullyDefinedRule.getDstAddress());
        assertThat(ruleDao.findByDefinition(tmpRule), hasSize(0));

        // Define dst port
        tmpRule.setDstPort(fullyDefinedRule.getDstPort());
        assertThat(ruleDao.findByDefinition(tmpRule), hasSize(0));

        // Define protocol
        tmpRule.setProtocol(fullyDefinedRule.getProtocol());
        assertThat(ruleDao.findByDefinition(tmpRule), hasSize(0));

        // Define exporter filter
        tmpRule.setExporterFilter(fullyDefinedRule.getExporterFilter());
        assertThat(ruleDao.findByDefinition(tmpRule), hasSize(1));

        // Now add another rule to another group
        final Rule anotherRule = new RuleBuilder().fromRule(fullyDefinedRule).build();
        anotherRule.getGroup().removeRule(anotherRule);
        anotherRule.setGroup(staticGroup);
        ruleDao.save(anotherRule);

        // should exist twice
        assertThat(ruleDao.findByDefinition(tmpRule), hasSize(2));

        // Should exist only once
        assertThat(ruleDao.findByDefinition(tmpRule, customGroup), is(fullyDefinedRule));
        assertThat(ruleDao.findByDefinition(tmpRule, staticGroup), is(anotherRule));
    }
}
