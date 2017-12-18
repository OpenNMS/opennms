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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.flows.classification.persistence.api.ClassificationRuleDao;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;
import org.opennms.netmgt.flows.classification.persistence.api.RuleBuilder;
import org.opennms.netmgt.flows.classification.internal.provider.StaticClassificationRuleProvider;
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
@JUnitTemporaryDatabase(reuseDatabase = false, tempDbClass = MockDatabase.class)
public class ClassificationRuleDaoIT {

    @Autowired
    private ClassificationRuleDao classificationDao;

    @Before
    public void before() {
    }

    @Test
    @Transactional
    public void verifyCRUD() {
        // Nothing created yet
        assertEquals(0, classificationDao.countAll());

        // Create dummy
        final Rule rule = new RuleBuilder()
                .withName("HTTP")
                .withIpAddress("127.0.0.1")
                .withPort("80,8080")
                .withProtocol("tcp").build();

        // create and verify creation
        classificationDao.saveOrUpdate(rule);
        assertEquals(1, classificationDao.countAll());

        // Update
        rule.setName("HTTP2");
        classificationDao.update(rule);
        assertEquals(1, classificationDao.countAll());
        assertEquals("HTTP2", classificationDao.get(rule.getId()).getName());

        // Delete
        classificationDao.delete(rule);
        assertEquals(0, classificationDao.countAll());
    }

    @Test
    @Transactional
    public void verifyPersistStaticRules() throws IOException {
        final List<Rule> rules = new StaticClassificationRuleProvider().getRules();
        rules.forEach(rule -> classificationDao.save(rule));
        assertEquals(rules.size(), classificationDao.countAll());
    }

}
