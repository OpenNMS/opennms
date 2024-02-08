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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

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
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml"})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
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
        this.customGroup = groupDao.findByName(Groups.USER_DEFINED);
        assertNotNull(Groups.USER_DEFINED + " classification group", this.customGroup);

        this.staticGroup = groupDao.findByName(Groups.SYSTEM_DEFINED);
        assertNotNull(Groups.SYSTEM_DEFINED + " classification group", this.staticGroup);
    }

    @Test
    public void verifyCRUD() {
        // Nothing of ours created yet (only default database schema)
        int initialCount = ruleDao.countAll();
        assertTrue("initial classification rules populated (count should be non-zero)", initialCount > 0);

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
        assertEquals(initialCount + 1, ruleDao.countAll());

        // Update
        rule.setName("HTTP2");
        ruleDao.update(rule);
        assertEquals(initialCount + 1, ruleDao.countAll());
        assertEquals("HTTP2", ruleDao.get(rule.getId()).getName());
        assertEquals(Groups.USER_DEFINED, ruleDao.get(rule.getId()).getGroup().getName());

        // Delete
        rule.getGroup().removeRule(rule);
        ruleDao.delete(rule);
        assertEquals(initialCount, ruleDao.countAll());
    }

    @Test
    public void verifyFetchingOnlyEnabledRules() {
        // Nothing of ours created yet (only default database schema)
        int initialCount = ruleDao.findAllEnabledRules().size();
        assertTrue("initial enabled classification rules should be populated (count should be non-zero)", initialCount > 0);

        // Create a bunch of rules
        ruleDao.save(new RuleBuilder().withName("Rule 1").withDstPort(1000).withGroup(staticGroup).build());
        ruleDao.save(new RuleBuilder().withName("Rule 2").withDstPort(1000).withGroup(customGroup).build());

        // Verify creation -- all default rules plus our two new ones
        assertThat(ruleDao.findAllEnabledRules(), hasSize(initialCount + 2));

        // Disable group -- no system-defined rules plus *one* of our new ones
        staticGroup.setEnabled(false);
        groupDao.saveOrUpdate(staticGroup);
        assertThat(ruleDao.findAllEnabledRules(), hasSize(1));

        // Disable other group -- no system-defined rules plus *none* of our new ones
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
