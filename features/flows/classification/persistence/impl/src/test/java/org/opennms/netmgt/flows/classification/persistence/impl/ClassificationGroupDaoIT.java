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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.sql.DataSource;

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
@JUnitTemporaryDatabase
public class ClassificationGroupDaoIT {

    @Autowired
    private ClassificationGroupDao groupDao;

    @Autowired
    private ClassificationRuleDao ruleDao;

    @Autowired
    private DataSource dataSource;

    @Before
    public void before() {
        groupDao.findAll().forEach(group -> groupDao.delete(group));
    }

    @Test
    public void verifyCRUD() {
        Group group = new GroupBuilder().withName(Groups.SYSTEM_DEFINED).build();

        // CREATE
        int groupId = groupDao.save(group);

        // READ
        Group readGroup = groupDao.get(groupId);
        assertThat(group.getName(), is(readGroup.getName()));
        assertThat(group.getId(), is(readGroup.getId()));
        assertThat(readGroup.getId(), is(groupId));

        // UPDATE
        group.setName("static new");
        groupDao.update(group);
        readGroup = groupDao.get(groupId);
        assertThat(readGroup.getName(), is("static new"));

        // DELETE
        groupDao.delete(group);
        assertThat(groupDao.countAll(), is(0));
    }

    @Test
    public void verifyCascadeOnInsertAndDelete() {
        // Verify nothing inserted yet
        assertThat(groupDao.countAll(), is(0));
        assertThat(ruleDao.countAll(), is(0));

        // INSERT
        final Group group = new GroupBuilder().withName("custom")
                .withRule(new RuleBuilder().withName("http").withDstPort(80).build())
                .withRule(new RuleBuilder().withName("http").withDstPort(8080).build())
                .build();
        groupDao.save(group);
        assertThat(groupDao.countAll(), is(1));
        assertThat(ruleDao.countAll(), is(2));

        // UPDATE
        group.getRules().get(0).setName("http2");
        group.getRules().remove(1);
        groupDao.saveOrUpdate(group);
        assertThat(groupDao.countAll(), is(1));
        assertThat(ruleDao.countAll(), is(1));
        assertThat(ruleDao.get(group.getRules().get(0).getId()).getName(), is("http2"));

        // DELETE
        groupDao.delete(group.getId());
        assertThat(groupDao.countAll(), is(0));
        assertThat(ruleDao.countAll(), is(0));
    }

    @Test
    @Transactional
    public void verifyKeepsOrder() {
        // INSERT
        Group group = new GroupBuilder().withName(Groups.SYSTEM_DEFINED)
                .withRule(new RuleBuilder().withName("http").withDstPort(80).withPosition(2).build())
                .withRule(new RuleBuilder().withName("http").withDstPort(8080).withPosition(1).build())
                .build();
        groupDao.save(group);
        group = groupDao.findByName(Groups.SYSTEM_DEFINED);

        final Rule rule1 = group.getRules().get(0);
        assertThat(rule1.getPosition(), is(1));
        assertThat(rule1.getName(), is("http"));
        assertThat(rule1.getDstPort(), is("8080"));

        final Rule rule2 = group.getRules().get(1);
        assertThat(rule2.getPosition(), is(2));
        assertThat(rule2.getName(), is("http"));
        assertThat(rule2.getDstPort(), is("80"));
    }
}
