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
package org.opennms.netmgt.dao.usermgmt;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.soa.support.DefaultServiceRegistry;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.config.DbGroupManager;
import org.opennms.netmgt.config.DbUserManager;
import org.opennms.netmgt.config.GroupDao;
import org.opennms.netmgt.config.GroupFactory;
import org.opennms.netmgt.config.GroupManager;
import org.opennms.netmgt.config.GroupManagerGroupDao;
import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.api.GroupConfig;
import org.opennms.netmgt.config.api.UserConfig;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/** The default store (org.opennms.usermgmt.store unset) wires the database managers. */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-mockSnmpPeerFactory.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class UserMgmtWiringIT {
    @Autowired
    private UserManager m_userManager;

    @Autowired
    private GroupManager m_groupManager;

    @Autowired
    private GroupDao m_groupDao;

    @Test
    public void testDatabaseIsTheDefault() {
        assertTrue(m_userManager instanceof DbUserManager);
        assertTrue(m_groupManager instanceof DbGroupManager);
        assertSame(m_userManager, UserFactory.getInstance());
        assertSame(m_groupManager, GroupFactory.getInstance());
        assertSame(m_groupManager, ((GroupManagerGroupDao) m_groupDao).getGroupManager());
    }

    @Test
    public void testServicesAreExported() {
        assertTrue(DefaultServiceRegistry.INSTANCE.findProviders(UserConfig.class).contains(m_userManager));
        assertTrue(DefaultServiceRegistry.INSTANCE.findProviders(GroupConfig.class).contains(m_groupManager));
    }
}
