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
package org.opennms.netmgt.flows.classification.internal.validation;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
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
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml"})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
@Transactional
public class GroupValidatorIT {

    @Autowired
    private ClassificationRuleDao ruleDao;

    @Autowired
    private ClassificationGroupDao groupDao;

    private GroupValidator groupValidator;

    @Before
    public void setUp() {
        assertThat(groupDao.countAll(), is(2)); // out of the box groups

        groupValidator = new GroupValidator(ruleDao);

        // Nothing of ours created yet (only default database schema)
        int initialCount = ruleDao.findAllEnabledRules().size();
        assertTrue("initial enabled classification rules should be populated (count should be non-zero)", initialCount > 0);
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
