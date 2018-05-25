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

package org.opennms.netmgt.flows.classification.internal;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.ClassificationRequestBuilder;
import org.opennms.netmgt.flows.classification.persistence.api.ClassificationGroupDao;
import org.opennms.netmgt.flows.classification.persistence.api.ClassificationRuleDao;
import org.opennms.netmgt.flows.classification.persistence.api.Group;
import org.opennms.netmgt.flows.classification.persistence.api.GroupBuilder;
import org.opennms.netmgt.flows.classification.persistence.api.Groups;
import org.opennms.netmgt.flows.classification.persistence.api.ProtocolType;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;
import org.opennms.netmgt.flows.classification.persistence.api.RuleBuilder;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionOperations;

import com.google.common.collect.Lists;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml"})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class DefaultClassificationEngineIT {

    @Autowired
    private ClassificationGroupDao groupDao;

    @Autowired
    private ClassificationRuleDao ruleDao;

    @Autowired
    private DatabasePopulator databasePopulator;

    @Autowired
    private FilterDao filterDao;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private TransactionOperations operations;

    @Before
    public void setUp() {
        operations.execute(status -> {
            databasePopulator.populateDatabase();
            groupDao.save(new GroupBuilder().withName(Groups.USER_DEFINED).build());
            Group userDefinedGroup = groupDao.findByName(Groups.USER_DEFINED);

            final ArrayList<Rule> rules = Lists.newArrayList(
                    new RuleBuilder().withName("rule1").withDstPort(80).withGroup(userDefinedGroup).build(),
                    new RuleBuilder().withName("rule2").withDstPort(100).withExporterFilter("categoryName == 'Routers'").withGroup(userDefinedGroup).build(),
                    new RuleBuilder().withName("rule3").withDstPort(200).withExporterFilter("categoryName == 'Develop' | ipAddr == '192.168.1.1'").withGroup(userDefinedGroup).build(),
                    new RuleBuilder().withName("rule4").withDstPort(300).withExporterFilter("categoryName == 'Routers' & ipAddr == '192.168.1.1'").withGroup(userDefinedGroup).build()
            );
            for (Rule eachRule : rules) {
                ruleDao.save(eachRule);
            }
            nodeDao.flush();
            return null;
        });
    }

    @After
    public void tearDown() {
        operations.execute(status -> {
            databasePopulator.resetDatabase();
            groupDao.findAll().forEach(group -> groupDao.delete(group));
            return null;
        });
    }

    @Test
    public void verifyRuleFilter() {
        final ClassificationEngine classificationEngine = new DefaultClassificationEngine(() -> ruleDao.findAllEnabledRules(), new DefaultFilterService(filterDao));

        // Create request, that matches rule1
        final ClassificationRequest classificationRequest = new ClassificationRequestBuilder()
                .withExporterAddress("10.0.0.1")
                .withDstAddress("5.5.5.5").withDstPort(80)
                .withSrcAddress("192.168.0.1").withSrcPort(55557)
                .withProtocol(ProtocolType.TCP).build();
        assertThat(classificationEngine.classify(classificationRequest), is("rule1"));

        // update request to match rule 2
        classificationRequest.setDstPort(100);
        classificationRequest.setExporterAddress("192.168.1.1");
        assertThat(classificationEngine.classify(classificationRequest), is("rule2"));

        // update request to match rule 3
        classificationRequest.setDstPort(200);
        classificationRequest.setExporterAddress("192.168.1.1");
        assertThat(classificationEngine.classify(classificationRequest), is("rule3"));

        // update request to match rule 4
        classificationRequest.setDstPort(300);
        classificationRequest.setExporterAddress("192.168.1.1");
        assertThat(classificationEngine.classify(classificationRequest), is("rule4"));

        // Update no rule matches
        classificationRequest.setDstPort(443);
        assertThat(classificationEngine.classify(classificationRequest), is(nullValue()));
    }
}