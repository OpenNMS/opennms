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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.config.DbGroupManager;
import org.opennms.netmgt.config.UserDataChangedException;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.groups.Role;
import org.opennms.netmgt.config.groups.Schedule;
import org.opennms.netmgt.config.groups.Time;
import org.opennms.netmgt.dao.api.OnCallRoleDao;
import org.opennms.netmgt.dao.api.UserAccountDao;
import org.opennms.netmgt.dao.api.UserGroupDao;
import org.opennms.netmgt.dao.api.UserMgmtVersionDao;
import org.opennms.netmgt.model.usermgmt.UserAccount;
import org.opennms.netmgt.model.usermgmt.UserGroup;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionOperations;

/** DbGroupManager on a real database. The manager runs its own transactions, so no test is @Transactional. */
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
public class DbGroupManagerIT {

    @Autowired
    private UserAccountDao m_userDao;

    @Autowired
    private UserGroupDao m_groupDao;

    @Autowired
    private OnCallRoleDao m_onCallRoleDao;

    @Autowired
    private UserMgmtVersionDao m_versionDao;

    @Autowired
    private TransactionOperations m_tx;

    @Autowired
    private PlatformTransactionManager m_transactionManager;

    private DbGroupManager m_manager;

    @Before
    public void setUp() {
        BeanUtils.assertAutowiring(this);
        m_manager = newManager();
        m_tx.execute(status -> {
            m_userDao.save(new UserAccount("jane"));
            m_userDao.save(new UserAccount("joe"));
            return null;
        });
    }

    private DbGroupManager newManager() {
        return new DbGroupManager(m_userDao, m_groupDao, m_onCallRoleDao, m_versionDao, m_transactionManager);
    }

    private static Group group(final String name, final String... members) {
        final Group group = new Group();
        group.setName(name);
        Arrays.stream(members).forEach(group::addUser);
        return group;
    }

    private static Role role(final String name, final String group, final String supervisor) {
        final Role role = new Role();
        role.setName(name);
        role.setMembershipGroup(group);
        role.setSupervisor(supervisor);
        return role;
    }

    @Test
    public void testSaveAndLoad() throws Exception {
        m_manager.saveGroup("Ops", group("Ops", "jane", "joe"));
        assertEquals(Arrays.asList("jane", "joe"), newManager().getGroup("Ops").getUsers());
    }

    @Test
    public void testSaveRole() throws Exception {
        m_manager.saveGroup("Ops", group("Ops", "jane"));
        m_manager.saveRole(role("Night", "Ops", "jane"));
        assertEquals("jane", m_tx.execute(s -> m_onCallRoleDao.findByName("Night").getSupervisor().getUsername()));
    }

    @Test
    public void testExternalChangeIsLoaded() throws Exception {
        assertFalse(m_manager.hasGroup("Ops"));
        m_tx.execute(s -> m_groupDao.save(new UserGroup("Ops")));
        Thread.sleep(1100); // the version check runs at most once a second
        assertTrue(m_manager.hasGroup("Ops"));
    }

    @Test
    public void testStaleSaveIsRejected() throws Exception {
        m_manager.getGroupNames(); // load
        m_tx.execute(s -> m_groupDao.save(new UserGroup("Other")));
        // within the check interval, the cache does not know "Other"
        assertThrows(UserDataChangedException.class, () -> m_manager.saveGroup("Ops", group("Ops", "jane")));
        assertNull(m_tx.execute(s -> m_groupDao.findByName("Ops")));
        assertTrue(m_manager.hasGroup("Other")); // reloaded
    }

    @Test
    public void testSaveWithRenamedMemberFailsAndReloads() throws Exception {
        m_manager.saveGroup("Ops", group("Ops", "jane"));
        m_tx.execute(s -> {
            m_userDao.findByUsername("jane").setUsername("janet");
            return null;
        });
        final Group ops = m_manager.getGroup("Ops");
        final IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> m_manager.saveGroup("Ops", ops));
        assertEquals("Group \"Ops\" has the member \"jane\", which is not a user.", e.getMessage());
        assertEquals(Arrays.asList("janet"), m_manager.getGroup("Ops").getUsers());
    }

    @Test
    public void testRenameGroupKeepsTheRole() throws Exception {
        m_manager.saveGroup("Ops", group("Ops", "jane"));
        m_manager.saveRole(role("Night", "Ops", "jane"));
        m_manager.renameGroup("Ops", "Operations");
        assertEquals("Operations", m_manager.getRole("Night").getMembershipGroup());
    }

    @Test
    public void testRenameGroupToExistingNameFails() throws Exception {
        m_manager.saveGroup("Ops", group("Ops"));
        m_manager.saveGroup("Dev", group("Dev"));
        final Exception e = assertThrows(Exception.class, () -> m_manager.renameGroup("Ops", "Dev"));
        assertEquals("Group \"Dev\" already exists.", e.getMessage());
    }

    @Test
    public void testDeleteMembershipGroupIsBlocked() throws Exception {
        m_manager.saveGroup("Ops", group("Ops", "jane"));
        m_manager.saveRole(role("Night", "Ops", "jane"));
        final IllegalStateException e = assertThrows(IllegalStateException.class, () -> m_manager.deleteGroup("Ops"));
        assertEquals("Group \"Ops\" is the membership group of the on-call role \"Night\". Change the membership group first.", e.getMessage());
        assertTrue(m_manager.hasGroup("Ops"));
    }

    @Test
    public void testDeleteGroupAndRole() throws Exception {
        m_manager.saveGroup("Ops", group("Ops", "jane"));
        m_manager.saveRole(role("Night", "Ops", "jane"));
        m_manager.deleteRole("Night");
        m_manager.deleteGroup("Ops");
        assertFalse(m_manager.hasGroup("Ops"));
        assertNull(m_manager.getRole("Night"));
    }

    @Test
    public void testDeleteUserRemovesMembershipAndSchedulesOnly() throws Exception {
        m_manager.saveGroup("Ops", group("Ops", "jane", "joe"));
        final Role night = role("Night", "Ops", "jane");
        final Schedule schedule = new Schedule();
        schedule.setName("joe");
        schedule.setType("daily");
        final Time time = new Time();
        time.setBegins("18:00:00");
        time.setEnds("23:00:00");
        schedule.addTime(time);
        night.addSchedule(schedule);
        m_manager.saveRole(night);

        m_manager.deleteUser("joe");
        assertEquals(Arrays.asList("jane"), m_manager.getGroup("Ops").getUsers());
        assertTrue(m_manager.getRole("Night").getSchedules().isEmpty());
        assertNotNull(m_tx.execute(s -> m_userDao.findByUsername("joe")));
    }

    @Test
    public void testDirectSaveGroupsOnANewManager() throws Exception {
        // saveGroups() without a load first: the stale check must not reject it
        final DbGroupManager fresh = newManager();
        m_tx.execute(s -> m_groupDao.save(new UserGroup("Existing")));
        fresh.saveGroups();
        assertTrue(fresh.hasGroup("Existing"));
    }

    @Test
    public void testSetGroupsIsNotSupported() {
        assertThrows(UnsupportedOperationException.class, () -> m_manager.setGroups(new HashMap<>()));
    }

    @Test
    public void testAFreshManagerLoadsBeforeRoleAndMembershipReads() throws Exception {
        // getRoles, getRoleNames, getRole and findGroupsForUser do not call update() in GroupManager
        m_manager.saveGroup("Ops", group("Ops", "jane"));
        m_manager.saveRole(role("Night", "Ops", "jane"));
        assertEquals(1, newManager().getRoles().size());
        assertEquals(Arrays.asList("Night"), Arrays.asList(newManager().getRoleNames()));
        assertNotNull(newManager().getRole("Night"));
        assertEquals(1, newManager().findGroupsForUser("jane").size());
    }

    @Test
    public void testRoleRenamedInPlaceIsRenamed() throws Exception {
        // WebRoleManagerImpl renames the cached Role object and then calls saveGroups()
        m_manager.saveGroup("Ops", group("Ops", "jane"));
        m_manager.saveRole(role("Night", "Ops", "jane"));
        m_manager.getRole("Night").setName("Late");
        m_manager.saveGroups();
        assertEquals(Arrays.asList("Late"), m_tx.execute(s -> m_onCallRoleDao.findAll().stream().map(r -> r.getName()).collect(Collectors.toList())));
        assertEquals(Arrays.asList("Late"), Arrays.asList(m_manager.getRoleNames()));
    }

    @Test
    public void testRoleRenamedInPlaceToAnExistingNameFails() throws Exception {
        m_manager.saveGroup("Ops", group("Ops", "jane"));
        m_manager.saveRole(role("Night", "Ops", "jane"));
        m_manager.saveRole(role("Day", "Ops", "jane"));
        m_manager.getRole("Night").setName("Day");
        final IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> m_manager.saveGroups());
        assertEquals("On-call role \"Day\" already exists.", e.getMessage());
        assertEquals(2, m_tx.execute(s -> m_onCallRoleDao.countAll()).intValue());
    }
}
