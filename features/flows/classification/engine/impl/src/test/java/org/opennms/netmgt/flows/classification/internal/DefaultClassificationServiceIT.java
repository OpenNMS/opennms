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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.flows.classification.ClassificationService;
import org.opennms.netmgt.flows.classification.FilterService;
import org.opennms.netmgt.flows.classification.internal.csv.CsvBuilder;
import org.opennms.netmgt.flows.classification.internal.provider.DaoClassificationRuleProvider;
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
public class DefaultClassificationServiceIT {

    @Autowired
    private ClassificationRuleDao ruleDao;

    @Autowired
    private ClassificationGroupDao groupDao;

    @Autowired
    private FilterDao filterDao;

    @Autowired
    private SessionUtils sessionUtils;

    private ClassificationService classificationService;

    private Group userGroupDb; // the user group that is attached to hibernate
    private Group userGroupCsv; // the user group that is not attached to hibernate

    @Before
    public void setUp() {
        FilterService filterService = new DefaultFilterService(filterDao);
        classificationService = new DefaultClassificationService(
                ruleDao,
                groupDao,
                new DefaultClassificationEngine(
                        new DaoClassificationRuleProvider(ruleDao), filterService),
                filterService,
                sessionUtils);
        userGroupDb = new GroupBuilder().withName(Groups.USER_DEFINED).build();
        groupDao.save(userGroupDb);
        assertThat(ruleDao.countAll(), is(0));
        userGroupCsv = new GroupBuilder().withName(Groups.USER_DEFINED).build();
    }

    @After
    public void tearDown() {
        groupDao.findAll().forEach(group -> groupDao.delete(group));
    }

    @Test
    public void verifyCsvImport() {
        // Rules
        final Rule http2Rule = new RuleBuilder().withGroup(userGroupCsv).withName("http2").withProtocol("TCP,UDP").withDstAddress("127.0.0.1").build();
        final Rule googleRule = new RuleBuilder().withGroup(userGroupCsv).withName("google").withDstAddress("8.8.8.8").build();
        final Rule opennmsMonitorRule = new RuleBuilder()
                .withName("opennms-monitor")
                .withGroup(userGroupCsv)
                .withProtocol("TCP")
                .withSrcAddress("10.0.0.1").withSrcPort(1000)
                .withDstAddress("10.0.0.2").withDstPort(8980)
                .build();

        // Dummy input
        final String csv = new CsvBuilder()
                .withRule(http2Rule)
                .withRule(googleRule)
                .withRule(opennmsMonitorRule)
                .build();

        // Import
        final InputStream inputStream = new ByteArrayInputStream(csv.getBytes());
        classificationService.importRules(userGroupDb.getId(), inputStream, true, true);

        // Verify
        assertThat(ruleDao.countAll(), is(3));
        assertThat(ruleDao.findByDefinition(new RuleBuilder()
                .withGroup(userGroupCsv)
                .withName("http") // name differs
                .withProtocol("tcp,udp")
                .withDstAddress("127.0.0.1").build()), hasSize(1));
        assertThat(ruleDao.findByDefinition(googleRule), hasSize(1));
        assertThat(ruleDao.findByDefinition(opennmsMonitorRule), hasSize(1));
    }

    @Test
    public void verifyCsvImportWithoutHeader() {
        // Dummy input
        final String csv = new CsvBuilder()
                .withHeader(false)
                .withRule(new RuleBuilder().withGroup(userGroupCsv).withName("http2").withProtocol("TCP,UDP").withDstAddress("127.0.0.1"))
                .withRule(new RuleBuilder().withGroup(userGroupCsv).withName("google").withDstAddress("8.8.8.8"))
                .build();

        // Import
        final InputStream inputStream = new ByteArrayInputStream(csv.getBytes());
        classificationService.importRules(userGroupDb.getId(), inputStream, false, true);

        // Verify
        assertThat(ruleDao.countAll(), is(2));
        assertThat(ruleDao.findByDefinition(new RuleBuilder()
                .withGroup(userGroupCsv)
                .withName("http")
                .withProtocol("tcp,udp")
                .withDstAddress("127.0.0.1").build()), hasSize(1));
        assertThat(ruleDao.findByDefinition(new RuleBuilder()
                .withGroup(userGroupCsv)
                .withName("google")
                .withDstAddress("8.8.8.8").build()), hasSize(1));
    }

    @Test
    public void verifyCsvImportWithDeletingEverythingBefore() {
        // Save a rule
        Rule rule = new RuleBuilder()
                .withGroup(userGroupDb)
                .withName("http")
                .withProtocol("tcp,udp")
                .build();
        ruleDao.save(rule);

        // verify it is created
        assertThat(ruleDao.countAll(), is(1));

        // define csv and import
        final String csv = new CsvBuilder()
                .withRule(new RuleBuilder()
                        .withGroup(this.userGroupCsv)
                        .withName("http2")
                        .withDstAddress("127.0.0.1")
                        .withProtocol("TCP,UDP"))
                .build();
        classificationService.importRules(userGroupDb.getId(), new ByteArrayInputStream(csv.getBytes()), true, true);

        // Verify original one is deleted, but count is still 1
        assertThat(ruleDao.findByDefinition(rule), hasSize(0));
        assertThat(ruleDao.countAll(), is(1));
    }

    @Test
    public void verifyCsvImportWithoutDeletingExistingRules() {
        // Save a rule
        Rule rule1 = new RuleBuilder()
                .withGroup(userGroupDb)
                .withName("rule1")
                .withProtocol("tcp,udp")
                .withDstPort(111)
                .build();
        ruleDao.save(rule1);

        // verify it is created
        assertThat(ruleDao.countAll(), is(1));

        // Define another rule, to import
        Rule rule2 = new RuleBuilder()
                .withGroup(userGroupCsv)
                .withName("rule2")
                .withDstAddress("127.0.0.1")
                .withDstPort(222)
                .withProtocol("tcp,udp")
                .build();

        // define csv and import
        boolean hasHeader = false;
        boolean deleteExistingRules = false;
        final String csv = new CsvBuilder().withRule(rule2).withHeader(hasHeader).build();
        classificationService.importRules(userGroupDb.getId(), new ByteArrayInputStream(csv.getBytes()), hasHeader, deleteExistingRules);
        assertThat(ruleDao.countAll(), is(2));

        // Verify original one is retained, and new one was added
        assertThat(ruleDao.findByDefinition(rule1), hasSize(1));
        assertThat(ruleDao.findByDefinition(rule2), hasSize(1));

    }
}
