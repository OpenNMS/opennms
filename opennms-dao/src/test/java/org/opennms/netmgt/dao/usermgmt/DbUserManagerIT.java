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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.config.DbGroupManager;
import org.opennms.netmgt.config.DbUserManager;
import org.opennms.netmgt.config.UserDataChangedException;
import org.opennms.netmgt.config.api.UserConfig.ContactType;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.groups.Role;
import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.dao.api.OnCallRoleDao;
import org.opennms.netmgt.dao.api.SecurityRoleDao;
import org.opennms.netmgt.dao.api.UserAccountDao;
import org.opennms.netmgt.dao.api.UserGroupDao;
import org.opennms.netmgt.dao.api.UserMgmtVersionDao;
import org.opennms.netmgt.model.usermgmt.UserAccount;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionOperations;

/** DbUserManager on a real database. The manager runs its own transactions, so no test is @Transactional. */
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
@JUnitTemporaryDatabase(reuseDatabase = false)
public class DbUserManagerIT {

    @Autowired
    private UserAccountDao m_userDao;

    @Autowired
    private UserGroupDao m_groupDao;

    @Autowired
    private OnCallRoleDao m_onCallRoleDao;

    @Autowired
    private SecurityRoleDao m_securityRoleDao;

    @Autowired
    private UserMgmtVersionDao m_versionDao;

    @Autowired
    private TransactionOperations m_tx;

    @Autowired
    private PlatformTransactionManager m_transactionManager;

    private DbGroupManager m_groups;
    private DbUserManager m_users;

    @Before
    public void setUp() {
        BeanUtils.assertAutowiring(this);
        m_groups = new DbGroupManager(m_userDao, m_groupDao, m_onCallRoleDao, m_versionDao, m_transactionManager);
        m_users = newUserManager();
    }

    private DbUserManager newUserManager() {
        return new DbUserManager(m_groups, m_userDao, m_securityRoleDao, m_onCallRoleDao, m_versionDao, m_transactionManager);
    }

    private static User user(final String id, final String... roles) {
        final User user = new User();
        user.setUserId(id);
        user.setPassword("hash", true);
        Arrays.stream(roles).forEach(user::addRole);
        return user;
    }

    private static Group group(final String name, final String... members) {
        final Group group = new Group();
        group.setName(name);
        Arrays.stream(members).forEach(group::addUser);
        return group;
    }

    @Test
    public void testSaveAndLoad() throws Exception {
        m_users.saveUser("jane", user("jane", "ROLE_USER"));
        assertEquals(Arrays.asList("ROLE_USER"), newUserManager().getUser("jane").getRoles());
    }

    @Test
    public void testPasswordAndContact() throws Exception {
        m_users.saveUser("jane", user("jane"));
        m_users.setUnencryptedPassword("jane", "s3cret");
        m_users.setContactInfo("jane", ContactType.email, "jane@example.org");
        final DbUserManager other = newUserManager();
        assertTrue(other.comparePasswords("jane", "s3cret"));
        assertEquals("jane@example.org", other.getEmail("jane"));
    }

    @Test
    public void testGetLastModifiedFollowsTheVersion() throws Exception {
        m_users.getUserNames();
        final long before = m_users.getLastModified();
        m_tx.execute(s -> m_userDao.save(new UserAccount("external")));
        Thread.sleep(1100); // the version check runs at most once a second
        m_users.update();
        assertTrue(m_users.getLastModified() > before);
        assertTrue(m_users.hasUser("external"));
    }

    @Test
    public void testRenameUserKeepsGroupMembership() throws Exception {
        m_users.saveUser("jane", user("jane"));
        m_groups.saveGroup("Ops", group("Ops", "jane"));
        m_users.renameUser("jane", "janet");
        assertTrue(m_users.hasUser("janet"));
        assertFalse(m_users.hasUser("jane"));
        assertEquals(Arrays.asList("janet"), m_groups.getGroup("Ops").getUsers());
    }

    @Test
    public void testDeleteUserRemovesMembership() throws Exception {
        m_users.saveUser("jane", user("jane"));
        m_users.saveUser("joe", user("joe"));
        m_groups.saveGroup("Ops", group("Ops", "jane", "joe"));
        m_users.deleteUser("joe");
        assertFalse(m_users.hasUser("joe"));
        assertEquals(Arrays.asList("jane"), m_groups.getGroup("Ops").getUsers());
    }

    @Test
    public void testDeleteSupervisorIsBlocked() throws Exception {
        m_users.saveUser("jane", user("jane"));
        m_groups.saveGroup("Ops", group("Ops"));
        final Role night = new Role();
        night.setName("Night");
        night.setMembershipGroup("Ops");
        night.setSupervisor("jane");
        m_groups.saveRole(night);

        final IllegalStateException e = assertThrows(IllegalStateException.class, () -> m_users.deleteUser("jane"));
        assertEquals("User \"jane\" is the supervisor of the on-call role \"Night\". Change the supervisor first.", e.getMessage());
        assertTrue(m_users.hasUser("jane"));
    }

    @Test
    public void testStaleSaveIsRejected() throws Exception {
        m_users.saveUser("jane", user("jane"));
        m_tx.execute(s -> m_userDao.save(new UserAccount("external")));
        // within the check interval, the cache does not know "external"
        assertThrows(UserDataChangedException.class, () -> m_users.setContactInfo("jane", ContactType.email, "x@example.org"));
        assertTrue(m_users.hasUser("external"));
    }

    @Test
    public void testSaveUsersReplacesAll() throws Exception {
        m_users.saveUser("jane", user("jane"));
        m_users.saveUsers(Arrays.asList(user("joe")));
        assertEquals(Arrays.asList("joe"), m_users.getUserNames());
    }

    @Test
    public void testUnknownRoleFailsAndCacheMatchesDatabase() throws Exception {
        final IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> m_users.saveUser("jane", user("jane", "ROLE_WIZARD")));
        assertEquals("User \"jane\" has the unknown security role \"ROLE_WIZARD\".", e.getMessage());
        assertFalse(m_users.hasUser("jane"));
    }

    @Test
    public void testWritesDoNotJoinTheCallersTransaction() throws Exception {
        // like the users.xml write, a manager change is committed on its own: a REST service's @Transactional
        // must not roll it back or hold its locks while the manager reloads
        m_users.saveUser("jane", user("jane"));
        m_tx.execute(status -> {
            try {
                m_users.deleteUser("jane");
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
            status.setRollbackOnly();
            return null;
        });
        assertFalse(newUserManager().hasUser("jane"));
    }
}
