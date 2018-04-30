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

package org.opennms.netmgt.flows.classification.internal.validation;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.opennms.netmgt.flows.classification.internal.validation.ValidatorTestUtils.verify;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.flows.classification.error.Errors;
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
public class GroupValidatorIT {

    @Autowired
    private ClassificationRuleDao ruleDao;

    @Autowired
    private ClassificationGroupDao groupDao;

    private GroupValidator groupValidator;

    @Before
    public void setUp() {
        groupDao.save(new GroupBuilder().withName(Groups.USER_DEFINED).build());
        groupDao.save(new GroupBuilder().withName(Groups.SYSTEM_DEFINED).build());
        groupValidator = new GroupValidator(ruleDao);

        assertThat(groupDao.countAll(), is(2));
        assertThat(ruleDao.countAll(), is(0));

    }

    @After
    public void tearDown() {
        groupDao.findAll().forEach(group -> groupDao.delete(group));

        assertThat(groupDao.countAll(), is(0));
        assertThat(ruleDao.countAll(), is(0));
    }

    @Test
    public void verifyValidation() {
        final Group userGroup = groupDao.findByName(Groups.USER_DEFINED);
        final Group systemGroup = groupDao.findByName(Groups.SYSTEM_DEFINED);

        // Empty group should succeed
        verify(() -> groupValidator.validate(userGroup, null));
        verify(() -> groupValidator.validate(systemGroup, null));

        // Group with a rule should succeed
        final Rule rule = createDummyRule();
        verify(() -> groupValidator.validate(userGroup, rule));
        userGroup.addRule(rule);

        // Same rule different group, should still succeed
        final Rule rule2 = createDummyRule();
        verify(() -> groupValidator.validate(systemGroup, rule2));
        systemGroup.addRule(rule2);

        // Adding the same rule (even with different name) should fail
        Rule rule3 = createDummyRule();
        rule3.setName("xxx");
        verify(() -> groupValidator.validate(userGroup, rule3), Errors.GROUP_DUPLICATE_RULE);
    }

    private static Rule createDummyRule() {
        return new RuleBuilder().withDstAddress("127.0.0.1").withName("dummy").build();
    }
}
