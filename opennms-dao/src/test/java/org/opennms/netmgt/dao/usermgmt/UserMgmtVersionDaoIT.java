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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.UserAccountDao;
import org.opennms.netmgt.dao.api.UserGroupDao;
import org.opennms.netmgt.dao.api.UserMgmtVersionDao;
import org.opennms.netmgt.dao.api.UserMgmtVersionDao.Domain;
import org.opennms.netmgt.model.usermgmt.UserAccount;
import org.opennms.netmgt.model.usermgmt.UserGroup;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionOperations;

/**
 * The triggers of the usermgmt_version changeset. No test is @Transactional: the triggers must run in committed
 * transactions, as they do for the managers.
 */
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
public class UserMgmtVersionDaoIT implements InitializingBean {

    @Autowired
    private UserMgmtVersionDao m_versionDao;

    @Autowired
    private UserAccountDao m_userDao;

    @Autowired
    private UserGroupDao m_groupDao;

    @Autowired
    private TransactionOperations m_tx;

    @Autowired
    private org.hibernate.SessionFactory m_sessionFactory;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Test
    public void testUserWritesIncrementOnlyTheUsersVersion() {
        final long users = m_versionDao.getVersion(Domain.users);
        final long groups = m_versionDao.getVersion(Domain.groups);
        m_tx.execute(status -> m_userDao.save(new UserAccount("jane")));
        assertTrue(m_versionDao.getVersion(Domain.users) > users);
        assertEquals(groups, m_versionDao.getVersion(Domain.groups));
    }

    @Test
    public void testGroupWritesIncrementOnlyTheGroupsVersion() {
        final long users = m_versionDao.getVersion(Domain.users);
        final long groups = m_versionDao.getVersion(Domain.groups);
        m_tx.execute(status -> m_groupDao.save(new UserGroup("Ops")));
        assertTrue(m_versionDao.getVersion(Domain.groups) > groups);
        assertEquals(users, m_versionDao.getVersion(Domain.users));
    }

    @Test
    public void testDeleteIncrementsTheVersion() {
        m_tx.execute(status -> m_userDao.save(new UserAccount("gone")));
        final long before = m_versionDao.getVersion(Domain.users);
        m_tx.execute(status -> {
            m_userDao.delete(m_userDao.findByUsername("gone"));
            return null;
        });
        assertTrue(m_versionDao.getVersion(Domain.users) > before);
    }

    @Test
    public void testLockAllDoesNotWriteOutPendingChanges() {
        final int[] visibleBeforeFlush = new int[1];
        m_tx.execute(status -> {
            m_groupDao.save(new UserGroup("LockProbe"));
            m_versionDao.lockAll();
            // a flush here would fire the triggers before the second lock is held
            m_sessionFactory.getCurrentSession().doWork(connection -> {
                try (java.sql.ResultSet rs = connection.createStatement().executeQuery("SELECT count(*) FROM groups WHERE name = 'LockProbe'")) {
                    rs.next();
                    visibleBeforeFlush[0] = rs.getInt(1);
                }
            });
            return null;
        });
        assertEquals(0, visibleBeforeFlush[0]);
        assertNotNull(m_tx.execute(status -> m_groupDao.findByName("LockProbe")));
    }
}
