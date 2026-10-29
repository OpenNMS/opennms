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
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.config.DbGroupManager;
import org.opennms.netmgt.config.DbUserManager;
import org.opennms.netmgt.config.WebRole;
import org.opennms.netmgt.config.WebRoleManagerImpl;
import org.opennms.netmgt.config.WebRolesException;
import org.opennms.netmgt.config.UserDataChangedException;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.groups.Role;
import org.opennms.netmgt.config.groups.Schedule;
import org.opennms.netmgt.config.groups.Time;
import org.opennms.netmgt.dao.api.OnCallRoleDao;
import org.opennms.netmgt.dao.api.SecurityRoleDao;
import org.opennms.netmgt.dao.api.UserAccountDao;
import org.opennms.netmgt.dao.api.UserGroupDao;
import org.opennms.netmgt.dao.api.UserMgmtVersionDao;
import org.opennms.netmgt.dao.api.UserMgmtVersionDao.VersionedTable;
import org.opennms.netmgt.model.usermgmt.OnCallRole;
import org.opennms.netmgt.model.usermgmt.OnCallSchedule;
import org.opennms.netmgt.model.usermgmt.OnCallScheduleTime;
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
    private SecurityRoleDao m_securityRoleDao;

    @Autowired
    private SessionFactory m_sessionFactory;

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
    public void testExpireMakesTheNextReadCheckAtOnce() throws Exception {
        m_manager.getGroupNames(); // load
        m_tx.execute(s -> m_groupDao.save(new UserGroup("Ops")));
        assertFalse(m_manager.hasGroup("Ops")); // within the check interval
        m_manager.expire();
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
        assertThrows(UserDataChangedException.class, () -> m_manager.saveGroup("Ops", ops));
        assertEquals(Arrays.asList("janet"), m_manager.getGroup("Ops").getUsers());
    }

    @Test
    public void testSaveDoesNotBindAMemberToANewUserWithTheOldName() throws Exception {
        // another process renames jane to janet and creates a new jane: the cached "jane" is janet
        m_manager.saveGroup("Ops", group("Ops", "jane"));
        final Integer janetId = m_tx.execute(s -> {
            final UserAccount jane = m_userDao.findByUsername("jane");
            jane.setUsername("janet");
            m_userDao.flush();
            m_userDao.save(new UserAccount("jane"));
            return jane.getId();
        });
        final Group ops = m_manager.getGroup("Ops");
        assertThrows(UserDataChangedException.class, () -> m_manager.saveGroup("Ops", ops));
        assertEquals(Arrays.asList(janetId), m_tx.execute(s -> m_groupDao.findByName("Ops").getMembers().stream()
                .map(UserAccount::getId).collect(Collectors.toList())));
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

    private WebRoleManagerImpl webRoles() {
        final DbUserManager users = new DbUserManager(m_manager, m_userDao, m_securityRoleDao, m_onCallRoleDao, m_versionDao, m_transactionManager);
        return new WebRoleManagerImpl(m_manager, users);
    }

    @Test
    public void testWebRoleRenameAndEdit() throws Exception {
        // the on-call role admin page (AdminRoleServlet) renames and edits through WebRoleManagerImpl
        m_manager.saveGroup("Ops", group("Ops", "jane"));
        m_manager.saveRole(role("Night", "Ops", "jane"));
        final WebRoleManagerImpl webRoles = webRoles();
        final WebRole role = webRoles.getRole("Night");
        role.setName("Late");
        role.setDescription("after hours");
        webRoles.saveRole(role);
        assertEquals(Arrays.asList("Late"), m_tx.execute(s -> m_onCallRoleDao.findAll().stream().map(r -> r.getName()).collect(Collectors.toList())));
        assertEquals("after hours", newManager().getRole("Late").getDescription().get());
    }

    @Test
    public void testWebRoleEditSurvivesAReload() throws Exception {
        // any user change reloads the group cache; a reload between getRole() and saveRole() must not lose the edit
        m_manager.saveGroup("Ops", group("Ops", "jane"));
        m_manager.saveRole(role("Night", "Ops", "jane"));
        final WebRoleManagerImpl webRoles = webRoles();
        final WebRole role = webRoles.getRole("Night");
        role.setDescription("after hours");
        m_manager.reload();
        webRoles.saveRole(role);
        assertEquals("after hours", newManager().getRole("Night").getDescription().get());
    }

    @Test
    public void testWebRoleRenameToAnExistingNameFails() throws Exception {
        m_manager.saveGroup("Ops", group("Ops", "jane"));
        m_manager.saveRole(role("Night", "Ops", "jane"));
        m_manager.saveRole(role("Day", "Ops", "jane"));
        final WebRoleManagerImpl webRoles = webRoles();
        final WebRole role = webRoles.getRole("Night");
        role.setName("Day");
        assertThrows(WebRolesException.class, () -> webRoles.saveRole(role));
        assertEquals(2, m_tx.execute(s -> m_onCallRoleDao.countAll()).intValue());
    }

    private int createFullGroup(final String name, final String... members) {
        return m_tx.execute(status -> {
            final UserGroup group = new UserGroup(name);
            group.setComments("Comment of " + name);
            group.setDefaultMap("map-" + name);
            group.getDutySchedules().add("MoTuWeThFr800-1700");
            for (final String member : members) {
                group.getMembers().add(m_userDao.findByUsername(member));
            }
            m_groupDao.save(group);
            return group.getId();
        });
    }

    private int createFullRole(final String name, final String group, final String supervisor, final String scheduled) {
        return m_tx.execute(status -> {
            final OnCallRole role = new OnCallRole(name, m_groupDao.findByName(group), m_userDao.findByUsername(supervisor));
            role.setDescription("Description of " + name);
            final OnCallSchedule daily = new OnCallSchedule(m_userDao.findByUsername(scheduled), OnCallSchedule.Type.daily);
            daily.getTimes().add(new OnCallScheduleTime(null, "08:00:00", "17:00:00"));
            role.getSchedules().add(daily);
            final OnCallSchedule weekly = new OnCallSchedule(m_userDao.findByUsername(supervisor), OnCallSchedule.Type.weekly);
            weekly.getTimes().add(new OnCallScheduleTime("monday", "00:00:00", "23:59:59"));
            role.getSchedules().add(weekly);
            m_onCallRoleDao.save(role);
            return role.getId();
        });
    }

    @Test
    public void testNoOpSaveGroupsKeepsRowVersions() throws Exception {
        // a save writes every cached group and role back: only what changed may get a new version
        final int ops = createFullGroup("Ops", "jane", "joe");
        final int dev = createFullGroup("Dev", "jane");
        final int night = createFullRole("Night", "Ops", "jane", "joe");
        final int day = createFullRole("Day", "Dev", "joe", "jane");
        final int opsBefore = m_versionDao.getRowVersion(VersionedTable.groups, ops);
        final int devBefore = m_versionDao.getRowVersion(VersionedTable.groups, dev);
        final int nightBefore = m_versionDao.getRowVersion(VersionedTable.oncall_roles, night);
        final int dayBefore = m_versionDao.getRowVersion(VersionedTable.oncall_roles, day);

        final Group changed = m_manager.getGroup("Dev");
        changed.setComments("Changed");
        m_manager.saveGroup("Dev", changed);
        assertEquals(opsBefore, m_versionDao.getRowVersion(VersionedTable.groups, ops));
        assertTrue(m_versionDao.getRowVersion(VersionedTable.groups, dev) > devBefore);
        assertEquals(nightBefore, m_versionDao.getRowVersion(VersionedTable.oncall_roles, night));
        assertEquals(dayBefore, m_versionDao.getRowVersion(VersionedTable.oncall_roles, day));

        final int dayBefore2 = m_versionDao.getRowVersion(VersionedTable.oncall_roles, day);
        final int nightBefore2 = m_versionDao.getRowVersion(VersionedTable.oncall_roles, night);
        final Role changedRole = m_manager.getRole("Day");
        changedRole.setDescription("Changed");
        m_manager.saveRole(changedRole);
        assertEquals(nightBefore2, m_versionDao.getRowVersion(VersionedTable.oncall_roles, night));
        assertTrue(m_versionDao.getRowVersion(VersionedTable.oncall_roles, day) > dayBefore2);
    }

    @Test
    public void testSaveRoleKeepsGroupVersions() throws Exception {
        // a role save writes every cached group back too: a group, even the role's membership group, keeps its version
        final int ops = createFullGroup("Ops", "jane", "joe");
        final int dev = createFullGroup("Dev", "jane");
        final int night = createFullRole("Night", "Ops", "jane", "joe");
        final int opsBefore = m_versionDao.getRowVersion(VersionedTable.groups, ops);
        final int devBefore = m_versionDao.getRowVersion(VersionedTable.groups, dev);
        final int nightBefore = m_versionDao.getRowVersion(VersionedTable.oncall_roles, night);

        final Role changed = m_manager.getRole("Night");
        changed.setDescription("Changed");
        m_manager.saveRole(changed);
        m_manager.saveRole(role("Day", "Dev", "jane"));

        assertTrue(m_versionDao.getRowVersion(VersionedTable.oncall_roles, night) > nightBefore);
        assertEquals(opsBefore, m_versionDao.getRowVersion(VersionedTable.groups, ops));
        assertEquals(devBefore, m_versionDao.getRowVersion(VersionedTable.groups, dev));
    }

    private void lockVersionRow(final String domain) {
        m_sessionFactory.getCurrentSession().doWork(connection -> {
            try (java.sql.PreparedStatement statement = connection.prepareStatement("SELECT version FROM usermgmt_version WHERE name = ? FOR UPDATE")) {
                statement.setString(1, domain);
                statement.executeQuery().close();
            }
        });
    }

    /** Polls pg_stat_activity, on the current transaction's connection, until another backend waits for a lock. */
    private void waitUntilAnotherBackendWaitsForALock() throws InterruptedException {
        final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            final boolean[] waiting = new boolean[1];
            m_sessionFactory.getCurrentSession().doWork(connection -> {
                try (java.sql.Statement statement = connection.createStatement()) {
                    // pg_stat_activity is a snapshot per transaction: drop it, or the poll never sees a change
                    statement.execute("SELECT pg_stat_clear_snapshot()");
                    try (java.sql.ResultSet rs = statement.executeQuery("SELECT count(*) FROM pg_stat_activity"
                             + " WHERE datname = current_database() AND pid <> pg_backend_pid() AND wait_event_type = 'Lock'")) {
                        rs.next();
                        waiting[0] = rs.getInt(1) > 0;
                    }
                }
            });
            if (waiting[0]) {
                return;
            }
            Thread.sleep(20);
        }
        fail("no backend started waiting for a lock within 10 s");
    }

    @Test
    public void testEmptyRoleDescriptionSurvivesAnUnrelatedSave() throws Exception {
        // a description migrated as '' must stay '': writing NULL would bump the version and cause a false 412
        createFullGroup("Ops", "jane", "joe");
        final int night = createFullRole("Night", "Ops", "jane", "joe");
        m_tx.execute(status -> {
            m_onCallRoleDao.get(night).setDescription("");
            return null;
        });
        final int before = m_versionDao.getRowVersion(VersionedTable.oncall_roles, night);
        createFullGroup("Dev", "jane");
        final Group changed = m_manager.getGroup("Dev");
        changed.setComments("Changed");
        m_manager.saveGroup("Dev", changed);
        assertEquals(before, m_versionDao.getRowVersion(VersionedTable.oncall_roles, night));
        assertEquals("", m_tx.execute(status -> m_onCallRoleDao.get(night).getDescription()));
    }

    @Test
    public void testManagerDeleteTakesUsersLockBeforeGroups() throws Exception {
        // a writer that took the groups lock before the users lock would deadlock against one that follows the order
        final DbUserManager users = new DbUserManager(m_manager, m_userDao, m_securityRoleDao, m_onCallRoleDao, m_versionDao, m_transactionManager);
        m_manager.saveGroup("Ops", group("Ops", "jane", "joe"));
        users.getUserNames(); // load

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            final CountDownLatch holding = new CountDownLatch(1);
            final Future<?> writer = m_tx.execute(status -> {
                lockVersionRow("users");
                // the manager's transaction now waits for the users lock
                final Future<?> future = executor.submit(() -> {
                    holding.countDown();
                    users.deleteUser("joe");
                    return null;
                });
                try {
                    assertTrue(holding.await(5, TimeUnit.SECONDS));
                    waitUntilAnotherBackendWaitsForALock();
                } catch (final InterruptedException e) {
                    throw new IllegalStateException(e);
                }
                // must not wait for (or time out on) a groups lock that the blocked writer already holds
                m_sessionFactory.getCurrentSession().doWork(connection -> {
                    try (java.sql.Statement statement = connection.createStatement()) {
                        statement.execute("SET LOCAL lock_timeout = '1s'");
                    }
                });
                lockVersionRow("groups");
                return future;
            });
            writer.get(10, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }
        assertFalse(users.hasUser("joe"));
    }
}
