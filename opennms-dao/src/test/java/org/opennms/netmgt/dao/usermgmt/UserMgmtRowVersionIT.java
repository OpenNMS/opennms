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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.OnCallRoleDao;
import org.opennms.netmgt.dao.api.SecurityRoleDao;
import org.opennms.netmgt.dao.api.UserAccountDao;
import org.opennms.netmgt.dao.api.UserGroupDao;
import org.opennms.netmgt.dao.api.UserMgmtVersionDao;
import org.opennms.netmgt.dao.api.UserMgmtVersionDao.VersionedTable;
import org.opennms.netmgt.model.usermgmt.ContactType;
import org.opennms.netmgt.model.usermgmt.OnCallRole;
import org.opennms.netmgt.model.usermgmt.OnCallSchedule;
import org.opennms.netmgt.model.usermgmt.OnCallScheduleTime;
import org.opennms.netmgt.model.usermgmt.SecurityRole;
import org.opennms.netmgt.model.usermgmt.UserAccount;
import org.opennms.netmgt.model.usermgmt.UserGroup;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionOperations;

/**
 * The row version triggers of the 37.0.0-usermgmt-row-versions changeset. No test is @Transactional: the triggers
 * must run in committed transactions, as they do for the managers.
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
@JUnitTemporaryDatabase(reuseDatabase = false)
public class UserMgmtRowVersionIT implements InitializingBean {

    @Autowired
    private UserMgmtVersionDao m_versionDao;

    @Autowired
    private UserAccountDao m_userDao;

    @Autowired
    private UserGroupDao m_groupDao;

    @Autowired
    private OnCallRoleDao m_roleDao;

    @Autowired
    private SecurityRoleDao m_securityRoleDao;

    @Autowired
    private SessionFactory m_sessionFactory;

    @Autowired
    private TransactionOperations m_tx;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    private int createUser(final String username) {
        return m_tx.execute(status -> {
            final UserAccount user = new UserAccount(username);
            m_userDao.save(user);
            return user.getId();
        });
    }

    private int createGroup(final String name, final String... members) {
        return m_tx.execute(status -> {
            final UserGroup group = new UserGroup(name);
            for (final String member : members) {
                group.getMembers().add(m_userDao.findByUsername(member));
            }
            m_groupDao.save(group);
            return group.getId();
        });
    }

    private int createRole(final String name, final String group, final String supervisor, final String... scheduled) {
        return m_tx.execute(status -> {
            final OnCallRole role = new OnCallRole(name, m_groupDao.findByName(group), m_userDao.findByUsername(supervisor));
            for (final String username : scheduled) {
                final OnCallSchedule schedule = new OnCallSchedule(m_userDao.findByUsername(username), OnCallSchedule.Type.daily);
                schedule.getTimes().add(new OnCallScheduleTime(null, "08:00:00", "09:00:00"));
                role.getSchedules().add(schedule);
            }
            m_roleDao.save(role);
            return role.getId();
        });
    }

    /** Runs the update on the connection of a new transaction and returns the number of rows changed. */
    private int update(final String sql, final Object... params) {
        return m_tx.execute(status -> {
            final int[] updated = new int[1];
            m_sessionFactory.getCurrentSession().doWork(connection -> {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    for (int i = 0; i < params.length; i++) {
                        statement.setObject(i + 1, params[i]);
                    }
                    updated[0] = statement.executeUpdate();
                }
            });
            return updated[0];
        });
    }

    private int version(final VersionedTable table, final int id) {
        return m_versionDao.getRowVersion(table, id);
    }

    private void assertBumped(final String what, final int before, final VersionedTable table, final int id) {
        assertTrue(what + " should have a new version, still " + before, version(table, id) > before);
    }

    @Test
    public void testVersionsAreUniqueAcrossObjects() {
        final int jane = createUser("jane");
        final int john = createUser("john");
        final int group = createGroup("ops", "john");
        final int role = createRole("night", "ops", "john");
        final int admin = m_tx.execute(status -> m_securityRoleDao.findByName("ROLE_ADMIN").getId());
        final int reader = m_tx.execute(status -> m_securityRoleDao.findByName("ROLE_USER").getId());

        // two users created the same way, a user and a group, and the built-in security roles: all different
        final Set<Integer> versions = new HashSet<>(Arrays.asList(
                version(VersionedTable.users, jane), version(VersionedTable.users, john),
                version(VersionedTable.groups, group), version(VersionedTable.oncall_roles, role),
                version(VersionedTable.security_roles, admin), version(VersionedTable.security_roles, reader)));
        assertEquals(6, versions.size());

        // a user deleted and created again with the same name and children is not the user it was
        final int oldVersion = version(VersionedTable.users, jane);
        m_tx.execute(status -> m_userDao.findByUsername("jane").setContact(ContactType.email, "jane@example.org", null));
        final int highest = version(VersionedTable.users, jane);
        assertTrue(highest > oldVersion);
        m_tx.execute(status -> {
            m_userDao.delete(m_userDao.findByUsername("jane"));
            return null;
        });
        final int recreated = m_tx.execute(status -> {
            final UserAccount account = new UserAccount("jane");
            account.setContact(ContactType.email, "jane@example.org", null);
            m_userDao.save(account);
            return account.getId();
        });
        final int newVersion = version(VersionedTable.users, recreated);
        assertTrue(newVersion > oldVersion);
        assertTrue(newVersion > version(VersionedTable.users, john));
        assertTrue(newVersion > highest);
    }

    @Test
    public void testExplicitVersionWriteGetsAFreshValue() {
        final int user = createUser("jane");
        update("UPDATE users SET full_name = 'Jane' WHERE id = ?", user);
        final int before = version(VersionedTable.users, user);
        assertTrue(before > 0);

        assertEquals(1, update("UPDATE users SET version = 0 WHERE id = ?", user));
        assertTrue(version(VersionedTable.users, user) > before);

        // not even the version it has can be set again to keep an old ETag valid
        final int current = version(VersionedTable.users, user);
        assertEquals(1, update("UPDATE users SET version = ? WHERE id = ?", before, user));
        assertTrue(version(VersionedTable.users, user) > current);
    }

    @Test
    public void testGetRowVersionOfAMissingRowFails() {
        try {
            m_versionDao.getRowVersion(VersionedTable.users, 123456);
            fail("expected an IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testUpdatingAUserColumnBumpsTheUser() {
        final int user = createUser("jane");
        final int before = version(VersionedTable.users, user);
        m_tx.execute(status -> {
            m_userDao.findByUsername("jane").setFullName("Jane Doe");
            return null;
        });
        assertBumped("user", before, VersionedTable.users, user);
        assertEquals(version(VersionedTable.users, user), m_tx.execute(status -> m_userDao.findByUsername("jane").getVersion()).intValue());
    }

    @Test
    public void testUnchangedUpdateDoesNotBump() {
        final int user = createUser("jane");
        update("UPDATE users SET full_name = 'Jane' WHERE id = ?", user);
        final int before = version(VersionedTable.users, user);
        assertEquals(1, update("UPDATE users SET full_name = full_name WHERE id = ?", user));
        assertEquals(before, version(VersionedTable.users, user));
    }

    @Test
    public void testContactInsertUpdateDeleteEachBumpTheUser() {
        final int user = createUser("jane");

        int before = version(VersionedTable.users, user);
        m_tx.execute(status -> m_userDao.findByUsername("jane").setContact(ContactType.email, "jane@example.org", null));
        assertBumped("insert", before, VersionedTable.users, user);

        before = version(VersionedTable.users, user);
        m_tx.execute(status -> {
            m_userDao.findByUsername("jane").getContact(ContactType.email).setInfo("jane@example.com");
            return null;
        });
        assertBumped("in-place edit", before, VersionedTable.users, user);

        before = version(VersionedTable.users, user);
        m_tx.execute(status -> m_userDao.findByUsername("jane").removeContact(ContactType.email));
        assertBumped("delete", before, VersionedTable.users, user);
    }

    @Test
    public void testNoOpUpdateOfAContactKeepsTheUserVersion() {
        final int user = createUser("jane");
        m_tx.execute(status -> m_userDao.findByUsername("jane").setContact(ContactType.email, "jane@example.org", null));
        final int before = version(VersionedTable.users, user);

        assertEquals(1, update("UPDATE user_contacts SET info = info WHERE user_id = ?", user));
        assertEquals(before, version(VersionedTable.users, user));

        assertEquals(1, update("UPDATE user_contacts SET info = 'jane@example.com' WHERE user_id = ?", user));
        assertBumped("a real change", before, VersionedTable.users, user);
    }

    @Test
    public void testDutyScheduleAndSecurityRoleChangesBumpTheUser() {
        final int user = createUser("jane");

        int before = version(VersionedTable.users, user);
        m_tx.execute(status -> m_userDao.findByUsername("jane").getDutySchedules().add("MoTuWeThFr800-1700"));
        assertBumped("duty schedule", before, VersionedTable.users, user);

        before = version(VersionedTable.users, user);
        m_tx.execute(status -> {
            final SecurityRole admin = m_securityRoleDao.findByName("ROLE_ADMIN");
            return m_userDao.findByUsername("jane").getSecurityRoles().add(admin);
        });
        assertBumped("security role", before, VersionedTable.users, user);
    }

    @Test
    public void testSecurityRoleAssignmentDoesNotBumpTheRole() {
        createUser("jane");
        final int role = m_tx.execute(status -> m_securityRoleDao.findByName("ROLE_ADMIN").getId());
        final int before = version(VersionedTable.security_roles, role);
        m_tx.execute(status -> m_userDao.findByUsername("jane").getSecurityRoles().add(m_securityRoleDao.findByName("ROLE_ADMIN")));
        assertEquals(before, version(VersionedTable.security_roles, role));

        // a change of the role itself does
        update("UPDATE security_roles SET description = 'changed' WHERE id = ?", role);
        assertBumped("role", before, VersionedTable.security_roles, role);
    }

    @Test
    public void testMemberAndDutyScheduleChangesBumpTheGroup() {
        createUser("jane");
        createUser("john");
        final int group = createGroup("ops", "jane");

        int before = version(VersionedTable.groups, group);
        m_tx.execute(status -> m_groupDao.findByName("ops").getMembers().add(m_userDao.findByUsername("john")));
        assertBumped("member added", before, VersionedTable.groups, group);

        before = version(VersionedTable.groups, group);
        m_tx.execute(status -> m_groupDao.findByName("ops").getDutySchedules().add("SaSu0-2359"));
        assertBumped("duty schedule", before, VersionedTable.groups, group);

        before = version(VersionedTable.groups, group);
        m_tx.execute(status -> m_groupDao.findByName("ops").getMembers().remove(0));
        assertBumped("member removed", before, VersionedTable.groups, group);
    }

    @Test
    public void testScheduleAndTimeChangesBumpTheOnCallRole() {
        createUser("jane");
        createUser("john");
        createGroup("ops", "jane", "john");
        final int role = createRole("night", "ops", "jane", "jane");

        // oncall_schedule_times
        int before = version(VersionedTable.oncall_roles, role);
        m_tx.execute(status -> m_roleDao.findByName("night").getSchedules().get(0).getTimes()
                .add(new OnCallScheduleTime(null, "10:00:00", "11:00:00")));
        assertBumped("time added", before, VersionedTable.oncall_roles, role);

        // oncall_schedules
        before = version(VersionedTable.oncall_roles, role);
        m_tx.execute(status -> m_roleDao.findByName("night").getSchedules()
                .add(new OnCallSchedule(m_userDao.findByUsername("john"), OnCallSchedule.Type.daily)));
        assertBumped("schedule added", before, VersionedTable.oncall_roles, role);

        // the time of a schedule that is being deleted: no failure, the role is bumped through the schedule
        before = version(VersionedTable.oncall_roles, role);
        m_tx.execute(status -> m_roleDao.findByName("night").getSchedules().remove(0));
        assertBumped("schedule removed", before, VersionedTable.oncall_roles, role);
    }

    @Test
    public void testUsernameChangeBumpsItsGroupsAndRoles() {
        final int user = createUser("jane");
        createUser("boss");
        createUser("other");
        createUser("night-user");
        final int member = createGroup("member-of", "jane");
        final int unrelated = createGroup("unrelated", "other");
        final int supervised = createRole("r1", "unrelated", "jane");
        final int scheduled = createRole("r2", "unrelated", "boss", "jane");
        final int unrelatedRole = createRole("r3", "unrelated", "boss", "night-user");

        final int memberBefore = version(VersionedTable.groups, member);
        final int unrelatedBefore = version(VersionedTable.groups, unrelated);
        final int supervisedBefore = version(VersionedTable.oncall_roles, supervised);
        final int scheduledBefore = version(VersionedTable.oncall_roles, scheduled);
        final int unrelatedRoleBefore = version(VersionedTable.oncall_roles, unrelatedRole);

        assertEquals(1, update("UPDATE users SET username = 'jane2' WHERE id = ?", user));

        assertBumped("group of the user", memberBefore, VersionedTable.groups, member);
        assertBumped("role the user supervises", supervisedBefore, VersionedTable.oncall_roles, supervised);
        assertBumped("role the user is scheduled in", scheduledBefore, VersionedTable.oncall_roles, scheduled);
        assertEquals(unrelatedBefore, version(VersionedTable.groups, unrelated));
        assertEquals(unrelatedRoleBefore, version(VersionedTable.oncall_roles, unrelatedRole));
    }

    @Test
    public void testUnrelatedUserUpdateDoesNotBumpItsGroups() {
        final int user = createUser("jane");
        final int group = createGroup("ops", "jane");
        final int role = createRole("night", "ops", "jane", "jane");
        final int groupBefore = version(VersionedTable.groups, group);
        final int roleBefore = version(VersionedTable.oncall_roles, role);
        final int userBefore = version(VersionedTable.users, user);

        // Hibernate lists every column in its UPDATE, the username included
        update("UPDATE users SET full_name = 'Jane Doe', username = username WHERE id = ?", user);
        m_tx.execute(status -> {
            m_userDao.findByUsername("jane").setComments("changed");
            return null;
        });

        assertEquals(groupBefore, version(VersionedTable.groups, group));
        assertEquals(roleBefore, version(VersionedTable.oncall_roles, role));
        assertBumped("user", userBefore, VersionedTable.users, user);
    }

    @Test
    public void testGroupNameChangeBumpsRolesUsingIt() {
        createUser("jane");
        final int group = createGroup("ops", "jane");
        createGroup("other", "jane");
        final int role = createRole("night", "ops", "jane");
        final int otherRole = createRole("day", "other", "jane");
        final int before = version(VersionedTable.oncall_roles, role);
        final int otherBefore = version(VersionedTable.oncall_roles, otherRole);

        update("UPDATE groups SET name = 'ops2' WHERE id = ?", group);

        assertBumped("role", before, VersionedTable.oncall_roles, role);
        assertEquals(otherBefore, version(VersionedTable.oncall_roles, otherRole));
    }

    @Test
    public void testPasswordUpdateOverJdbcBumps() {
        final int user = createUser("jane");
        final int before = version(VersionedTable.users, user);
        // as in DatabasePasswords
        assertEquals(1, update("UPDATE users SET password_hash = ?, password_salted = true, last_modified = NOW() WHERE username = ?", "hash", "jane"));
        assertBumped("user", before, VersionedTable.users, user);
    }

    @Test
    public void testDeletingAUserWithContactsSucceeds() {
        final int user = createUser("jane");
        m_tx.execute(status -> {
            final UserAccount account = m_userDao.findByUsername("jane");
            account.setContact(ContactType.email, "jane@example.org", null);
            account.setContact(ContactType.workPhone, "555-0100", null);
            account.getDutySchedules().add("MoTuWeThFr800-1700");
            return null;
        });
        m_tx.execute(status -> {
            m_userDao.delete(m_userDao.findByUsername("jane"));
            return null;
        });
        assertNull(m_tx.execute(status -> m_userDao.findByUsername("jane")));
        try {
            m_versionDao.getRowVersion(VersionedTable.users, user);
            fail("the user should be gone");
        } catch (final IllegalArgumentException expected) {
            // gone
        }
    }

    @Test
    public void testLockAllTakesTheUsersLockBeforeTheGroupsLock() throws Exception {
        // B holds the groups row; A's lockAll() must take users and then wait for groups. While A waits, A must
        // hold the users row, so C cannot lock it. With the order groups-then-users, A would hold nothing and C would succeed.
        final ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            final AtomicReference<String> sqlState = new AtomicReference<>();
            final Future<?> lockAll = m_tx.execute(b -> {
                m_sessionFactory.getCurrentSession().doWork(connection -> {
                    try (java.sql.Statement statement = connection.createStatement()) {
                        statement.executeQuery("SELECT version FROM usermgmt_version WHERE name = 'groups' FOR UPDATE").close();
                    } catch (final java.sql.SQLException e) {
                        throw new IllegalStateException(e);
                    }
                });
                final Future<?> a = executor.submit(() -> m_tx.execute(other -> {
                    m_versionDao.lockAll();
                    return null;
                }));
                try {
                    waitUntilAnotherBackendWaitsForALock();
                    executor.submit(() -> m_tx.execute(c -> {
                        m_sessionFactory.getCurrentSession().doWork(connection -> {
                            try (java.sql.Statement statement = connection.createStatement()) {
                                statement.execute("SET LOCAL lock_timeout = '500ms'");
                                statement.executeQuery("SELECT version FROM usermgmt_version WHERE name = 'users' FOR UPDATE").close();
                                sqlState.set("not blocked");
                            } catch (final java.sql.SQLException e) {
                                sqlState.set(e.getSQLState());
                            }
                        });
                        return null;
                    })).get(30, TimeUnit.SECONDS);
                } catch (final Exception e) {
                    a.cancel(true);
                    throw new IllegalStateException(e);
                }
                return a; // B commits on return and releases the groups row
            });
            lockAll.get(30, TimeUnit.SECONDS);
            assertEquals("users row while lockAll waits for groups", "55P03", sqlState.get());
        } finally {
            executor.shutdownNow();
        }
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
}
