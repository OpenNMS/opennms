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

package org.opennms.netmgt.flows.classification.service.publisher.impl;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.ipc.twin.memory.MemoryTwinPublisher;
import org.opennms.core.ipc.twin.memory.MemoryTwinSubscriber;
import org.opennms.core.ipc.twin.test.AbstractTwinBrokerIT;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.dao.support.DefaultFilterWatcher;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.flows.classification.dto.RuleDTO;
import org.opennms.netmgt.flows.classification.persistence.api.ClassificationGroupDao;
import org.opennms.netmgt.flows.classification.persistence.api.ClassificationRuleDao;
import org.opennms.netmgt.flows.classification.persistence.api.Group;
import org.opennms.netmgt.flows.classification.persistence.api.GroupBuilder;
import org.opennms.netmgt.flows.classification.persistence.api.Groups;
import org.opennms.netmgt.flows.classification.persistence.api.RuleBuilder;
import org.opennms.netmgt.flows.classification.service.ClassificationService;
import org.opennms.netmgt.flows.classification.service.internal.DefaultClassificationService;
import org.opennms.netmgt.flows.classification.service.internal.csv.CsvBuilder;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml"})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
public class ClassificationRulePublisherIT {

    @Autowired
    private ClassificationRuleDao ruleDao;

    @Autowired
    private ClassificationGroupDao groupDao;

    @Autowired
    private FilterDao filterDao;

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private DatabasePopulator databasePopulator;

    private MemoryTwinPublisher twinPublisher;

    private ClassificationService classificationService;

    private ClassificationRulePublisher classificationRulePublisher;

    private Group userGroupDb; // the user group that is attached to hibernate
    private Group userGroupCsv; // the user group that is not attached to hibernate

    @BeforeTransaction
    public void setUpDatabase() {
        this.databasePopulator.populateDatabase();
    }

    @Before
    public void setUp() throws Exception {
        this.twinPublisher = new MemoryTwinPublisher();

        final var filterWatcher = new DefaultFilterWatcher();
        filterWatcher.setFilterDao(this.filterDao);
        filterWatcher.setSessionUtils(this.sessionUtils);
        filterWatcher.afterPropertiesSet();

        classificationService = new DefaultClassificationService(
                ruleDao,
                groupDao,
                filterDao,
                filterWatcher,
                sessionUtils);
        assertThat("The groups should be pre-populated from liquibase", groupDao.countAll(), is(2));
        assertTrue("The rules should be pre-populated from liquibase", ruleDao.countAll() > 0);
        userGroupDb = groupDao.findByName(Groups.USER_DEFINED);
        userGroupCsv = new GroupBuilder().withName(Groups.USER_DEFINED).build();

        this.classificationRulePublisher = new ClassificationRulePublisher(this.classificationService,
                                                                           this.twinPublisher);
    }

    @AfterTransaction
    public void tearDownDatabase() {
        this.databasePopulator.resetDatabase();
    }

    @Test
    public void testRulePublishing() throws Exception {
        // Disable system group for smaller test set
        this.sessionUtils.withTransaction(() -> {
            final var sysgroup = this.groupDao.findByName(Groups.SYSTEM_DEFINED);
            sysgroup.setEnabled(false);
            this.classificationService.saveGroup(sysgroup);
        });

        final var subscriber = new MemoryTwinSubscriber(this.twinPublisher, "Test");
        final var tracker = AbstractTwinBrokerIT.Tracker.subscribe(subscriber,
                                                                   RuleDTO.TWIN_KEY,
                                                                   RuleDTO.TWIN_TYPE);

        final String csv = new CsvBuilder()
                .withHeader(false)
                .withRule(new RuleBuilder()
                                  .withGroup(userGroupCsv)
                                  .withName("rule1")
                                  .withDstAddress("127.0.0.1")
                                  .withDstPort(222)
                                  .withProtocol("tcp,udp")
                                  .build())
                .withRule(new RuleBuilder()
                                  .withGroup(userGroupCsv)
                                  .withName("rule2")
                                  .withDstAddress("127.0.0.1")
                                  .withDstPort(222)
                                  .withProtocol("tcp,udp")
                                  .withExporterFilter("IPADDR != '0.0.0.0'") // A filter that matches all nodes
                                  .build())
                .build();

        classificationService.importRules(userGroupDb.getId(), new ByteArrayInputStream(csv.getBytes()), false, true);

        await().until(tracker::getLog, contains(
                emptyCollectionOf(RuleDTO.class),
                hasItems(RuleDTO.builder()
                                .withPosition(0)
                                .withName("rule1")
                                .withDstAddress("127.0.0.1")
                                .withDstPort(222)
                                .withProtocols(17, 6)
                                .build(),
                         RuleDTO.builder()
                                .withPosition(1)
                                .withName("rule2")
                                .withDstAddress("127.0.0.1")
                                .withDstPort(222)
                                .withProtocols(17, 6)
                                // As this is a match-all filter, we expect to see all known interfaces
                                .withExporters(this.databasePopulator.getIpInterfaceDao().findAll().stream()
                                                                     .map(OnmsIpInterface::getIpAddress)
                                                                     .map(InetAddressUtils::str)
                                                                     .collect(Collectors.toList()))
                                .build())));
    }
}
